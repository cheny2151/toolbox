package cn.cheny.toolbox.window.coordinator;

import cn.cheny.toolbox.other.NamePrefixThreadFactory;
import cn.cheny.toolbox.window.BatchConfiguration;
import cn.cheny.toolbox.window.Params;
import cn.cheny.toolbox.window.WindowElement;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 窗口协调类
 *
 * @author by chenyi
 * @date 2021/9/22
 */
@Slf4j
public class WindowCoordinator {

    private final AtomicInteger status;

    private final ExecutorService workers;

    private final Object target;

    private final BatchConfiguration batchConfiguration;

    private ConcurrentHashMap<Params, WindowCoordinatorUnit> units;

    public WindowCoordinator(Object target, BatchConfiguration batchConfiguration) {
        this.target = target;
        this.batchConfiguration = batchConfiguration;
        this.units = new ConcurrentHashMap<>();
        this.status = new AtomicInteger(0);
        this.workers = Executors.newFixedThreadPool(batchConfiguration.getThreadPoolSize(), new NamePrefixThreadFactory("Model-Inference"));
    }

    public WindowElement addElement(Object[] args) {
        if (status.get() == 0) {
            this.start();
        }
        Params params = batchConfiguration.buildParams(args);
        WindowCoordinatorUnit unit = units.computeIfAbsent(params, k -> new WindowCoordinatorUnit(params, batchConfiguration, target, workers));
        return unit.addElement(args);
    }

    private void start() {
        if (status.compareAndSet(0, 1)) {
            Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(this::startWork, 0, batchConfiguration.getWinTime(), TimeUnit.MILLISECONDS);
        }
    }

    private void startWork() {
        workers.execute(this::doWindow);
    }

    private void doWindow() {
        units.values().forEach(WindowCoordinatorUnit::startWork);
    }

    public Object getTarget() {
        return target;
    }

}
