package cn.cheny.toolbox.window.coordinator;

import cn.cheny.toolbox.exception.WindowElementEmptyException;
import cn.cheny.toolbox.other.NamePrefixThreadFactory;
import cn.cheny.toolbox.window.BatchConfiguration;
import cn.cheny.toolbox.window.CollectedStaticParams;
import cn.cheny.toolbox.window.WindowElement;
import lombok.extern.slf4j.Slf4j;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 窗口协调类
 *
 * @author by chenyi
 * @date 2021/9/22
 */
@Slf4j
public class WindowCoordinator implements Closeable {

    private final static int QUEUE_SIZE = 200;
    private final AtomicInteger status = new AtomicInteger(0);

    private final Object target;
    private final ExecutorService producer;
    private final ExecutorService workers;
    private final BatchConfiguration batchConfiguration;
    private final long winTime;
    private final int threshold;
    private final ArrayBlockingQueue<WindowElement> elementQueue;
    private final ConcurrentHashMap<CollectedStaticParams, WindowInvoker> invokers;

    public WindowCoordinator(Object target, BatchConfiguration batchConfiguration) {
        this.target = target;
        this.batchConfiguration = batchConfiguration;
        this.winTime = batchConfiguration.getWinTime();
        this.threshold = batchConfiguration.getThreshold();
        this.invokers = new ConcurrentHashMap<>();
        this.producer = Executors.newSingleThreadScheduledExecutor();
        this.workers = Executors.newFixedThreadPool(batchConfiguration.getThreadPoolSize(), new NamePrefixThreadFactory("BatchHandler"));
        this.elementQueue = new ArrayBlockingQueue<>(QUEUE_SIZE);
    }

    public WindowElement addElement(Object[] args) throws WindowElementEmptyException, InterruptedException {
        CollectedStaticParams staticParams = batchConfiguration.buildStaticParams(args, batchConfiguration);
        WindowInvoker invoker = invokers.computeIfAbsent(staticParams,
                k -> new WindowInvoker(batchConfiguration.buildParams(args), batchConfiguration, target));
        WindowElement element = invoker.buildElement(args);
        element.setStaticParams(staticParams);
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
                Map<CollectedStaticParams, List<WindowElement>> batchMap = new HashMap<>(threshold);
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
                        List<WindowElement> batch = batchMap.computeIfAbsent(element.getStaticParams(), k -> new ArrayList<>());
                        batch.add(element);
                        size++;
                    }
                }
                if (size > 0) {
                    submitBatch(batchMap);
                }
            }
        });
    }

    private void submitBatch(Map<CollectedStaticParams, List<WindowElement>> batchMap) {
        batchMap.forEach((params, elements) ->
                workers.execute(() -> {
                    WindowInvoker windowInvoker = invokers.get(params);
                    try {
                        windowInvoker.doWindow(elements);
                    } catch (Exception e) {
                        log.error("Execute window task exception", e);
                    }
                })
        );
    }

    public Object getTarget() {
        return target;
    }

    @Override
    public void close() throws IOException {
        this.producer.shutdown();
        this.workers.shutdown();
        this.elementQueue.clear();
    }
}
