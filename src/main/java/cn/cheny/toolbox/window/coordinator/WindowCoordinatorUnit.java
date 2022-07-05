package cn.cheny.toolbox.window.coordinator;

import cn.cheny.toolbox.exception.WindowElementEmptyException;
import cn.cheny.toolbox.window.BatchConfiguration;
import cn.cheny.toolbox.window.BatchMethod;
import cn.cheny.toolbox.window.CollectedParams;
import cn.cheny.toolbox.window.WindowElement;
import cn.cheny.toolbox.window.output.BatchResultSplitter;
import lombok.extern.slf4j.Slf4j;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 批处理窗口协调单元
 *
 * @author by chenyi
 * @date 2021/9/22
 */
@Slf4j
public class WindowCoordinatorUnit implements Closeable {

    private final static int QUEUE_SIZE = 200;

    private final AtomicInteger status = new AtomicInteger(0);
    private final CollectedParams collectedParams;
    private final BatchMethod batchMethod;
    private final BatchResultSplitter splitter;
    private final int threshold;
    private final long winTime;
    private Object target;
    private final ExecutorService producer;
    private final ExecutorService workers;
    private final ArrayBlockingQueue<WindowElement> elementQueue;

    public WindowCoordinatorUnit(CollectedParams collectedParams, BatchConfiguration batchConfiguration, Object target, ExecutorService workers) {
        this.collectedParams = collectedParams;
        this.batchMethod = batchConfiguration.getBatchMethod();
        this.winTime = batchConfiguration.getWinTime();
        this.splitter = batchConfiguration.getBatchResultSplitter();
        this.threshold = batchConfiguration.getThreshold();
        this.target = target;
        this.producer = Executors.newSingleThreadScheduledExecutor();
        this.workers = workers;
        this.elementQueue = new ArrayBlockingQueue<>(QUEUE_SIZE);
    }

    public WindowElement addElement(Object[] args) throws WindowElementEmptyException, InterruptedException {
        WindowElement element = collectedParams.buildElement(args);
        int size = element.size();
        if (size == 0) {
            throw new WindowElementEmptyException();
        }
        elementQueue.put(element);
        start();
        return element;
    }

    private void start() {
        if (status.get() == 0 && status.compareAndSet(0, 1)) {
            startCollectBatch();
        }
    }

    private void startCollectBatch() {
        long winTime = this.winTime;
        int threshold = this.threshold;
        producer.execute(() -> {
            while (true) {
                long endTime = System.currentTimeMillis() + winTime;
                List<WindowElement> batch = new ArrayList<>(threshold);
                int size = 0;
                long timeout;
                while (size < threshold && (timeout = (endTime - System.currentTimeMillis())) > 0) {
                    WindowElement element = null;
                    try {
                        element = elementQueue.poll(timeout, TimeUnit.MILLISECONDS);
                    } catch (InterruptedException e) {
                        log.error("Fail to poll window element", e);
                    }
                    if (element != null) {
                        batch.add(element);
                        size++;
                    }
                }
                if (size > 0) {
                    submitBatch(batch);
                }
            }
        });
    }

    private void submitBatch(List<WindowElement> batch) {
        workers.execute(() -> {
            try {
                this.doWindow(batch);
            } catch (Exception e) {
                log.error("Execute window task exception", e);
            }
        });
    }

    private void doWindow(List<WindowElement> batch) {
        List<Object> inputs = new ArrayList<>();
        batch.forEach(e -> e.collectInput(inputs));
        long l = System.currentTimeMillis();
        Object outputs;
        try {
            outputs = doBatch(inputs);
        } catch (Exception e) {
            batch.forEach(element -> element.setError(e));
            return;
        }
        if (log.isDebugEnabled()) {
            log.debug("Do batch size:{}, use time:{}", inputs.size(), System.currentTimeMillis() - l);
        }
        for (int i = 0, index = 0; i < batch.size(); i++) {
            WindowElement element = batch.get(i);
            if (outputs == null) {
                element.setOutput(null);
            } else {
                try {
                    Object output = splitter.split(outputs, element, index);
                    element.setOutput(output);
                } catch (Exception e) {
                    element.setError(e);
                }
                index += element.size();
            }
        }
    }

    private Object doBatch(List<Object> inputs) throws Exception {
        Object[] args = collectedParams.buildArgs(inputs);
        return batchMethod.doBatch(target, args);
    }

    public Object getTarget() {
        return target;
    }

    public void setTarget(Object target) {
        this.target = target;
    }

    @Override
    public void close() throws IOException {
        this.producer.shutdown();
        this.elementQueue.clear();
    }
}
