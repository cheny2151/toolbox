package cn.cheny.toolbox.window.coordinator;

import cn.cheny.toolbox.window.BatchConfiguration;
import cn.cheny.toolbox.window.BatchMethod;
import cn.cheny.toolbox.window.Params;
import cn.cheny.toolbox.window.WindowElement;
import cn.cheny.toolbox.window.output.BatchResultSplitter;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 批处理窗口协调单元
 *
 * @author by chenyi
 * @date 2021/9/22
 */
@Slf4j
public class WindowCoordinatorUnit {

    private final Params params;
    private final BatchMethod batchMethod;
    private final BatchResultSplitter splitter;
    private final int threshold;
    private final AtomicInteger cursize;
    private Object target;
    private ConcurrentHashMap<Integer, WindowElement> content;
    private final ExecutorService workers;
    private final AtomicInteger lock;

    public WindowCoordinatorUnit(Params params, BatchConfiguration batchConfiguration, Object target, ExecutorService workers) {
        this.params = params;
        this.batchMethod = batchConfiguration.getBatchMethod();
        this.splitter = batchConfiguration.getBatchResultSplitter();
        this.threshold = batchConfiguration.getThreshold();
        this.target = target;
        this.cursize = new AtomicInteger(0);
        this.content = new ConcurrentHashMap<>(threshold);
        this.workers = workers;
        this.lock = new AtomicInteger(0);
    }

    public WindowElement addElement(Object[] args) {
        int curSize;
        WindowElement element = params.buildElement(args);
        int size = element.size();
        while (true) {
            curSize = cursize.get();
            if (curSize == -1 || curSize >= threshold) {
                Thread.yield();
            } else if (this.cursize.compareAndSet(curSize, curSize + size)) {
                content.put(curSize, element);
                if (curSize + size >= threshold) {
                    this.startWork();
                }
                return element;
            }
        }
    }

    public void startWork() {
        if (tryLock()) {
            workers.execute(this::doWindow);
        }
    }

    private boolean tryLock() {
        return lock.compareAndSet(0, 1);
    }

    private void unlock() {
        lock.compareAndSet(1, 0);
    }

    private void doWindow() {
        int curSize;
        while ((curSize = this.cursize.get()) > 0) {
            if (this.cursize.compareAndSet(curSize, -1)) {
                ConcurrentHashMap<Integer, WindowElement> curContent;
                // 自旋等待put element
                while ((curContent = this.content).size() != curSize) {
                    if (log.isDebugEnabled()) {
                        log.debug("wait put element");
                    }
                }
                this.content = new ConcurrentHashMap<>(threshold);
                this.cursize.set(0);
                unlock();
                List<WindowElement> elements = new ArrayList<>(curContent.values());
                List<Object> inputs = new ArrayList<>();
                elements.forEach(e -> e.collectInput(inputs));
                long l = System.currentTimeMillis();
                Object outputs;
                try {
                    outputs = doBatch(inputs);
                    curContent.clear();
                } catch (Exception e) {
                    elements.forEach(element -> element.setError(e));
                    return;
                }
                if (log.isDebugEnabled()) {
                    log.debug("size:{},use time:{}", inputs.size(), System.currentTimeMillis() - l);
                }
                for (int i = 0; i < elements.size(); i++) {
                    WindowElement element = elements.get(i);
                    Object output = splitter.split(outputs, element, i);
                    element.setOutput(output);
                }
                break;
            }
        }
        if (curSize == 0) {
            unlock();
        }
    }

    private Object doBatch(List<Object> inputs) {
        Object[] args = params.buildArgs(inputs);
        return batchMethod.doBatch(target, args);
    }


    public Object getTarget() {
        return target;
    }

    public void setTarget(Object target) {
        this.target = target;
    }
}