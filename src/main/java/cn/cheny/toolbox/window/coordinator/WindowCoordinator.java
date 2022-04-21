package cn.cheny.toolbox.window.coordinator;

import cn.cheny.toolbox.exception.WindowElementEmptyException;
import cn.cheny.toolbox.other.NamePrefixThreadFactory;
import cn.cheny.toolbox.window.BatchConfiguration;
import cn.cheny.toolbox.window.CollectedStaticParams;
import cn.cheny.toolbox.window.WindowElement;
import lombok.extern.slf4j.Slf4j;

import java.io.Closeable;
import java.io.IOException;
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

    private final AtomicInteger status;

    private final ExecutorService workers;

    private ScheduledExecutorService scheduled;

    private final Object target;

    private final BatchConfiguration batchConfiguration;

    private final ConcurrentHashMap<CollectedStaticParams, WindowCoordinatorUnit> units;

    public WindowCoordinator(Object target, BatchConfiguration batchConfiguration) {
        this.target = target;
        this.batchConfiguration = batchConfiguration;
        this.units = new ConcurrentHashMap<>();
        this.status = new AtomicInteger(0);
        this.workers = Executors.newFixedThreadPool(batchConfiguration.getThreadPoolSize(), new NamePrefixThreadFactory("BatchHandler"));
    }

    public WindowElement addElement(Object[] args) throws WindowElementEmptyException {
        if (status.get() == 0) {
            this.start();
        }
        CollectedStaticParams staticParams = batchConfiguration.buildStaticParams(args, batchConfiguration);
        WindowCoordinatorUnit unit = units.computeIfAbsent(staticParams,
                k -> new WindowCoordinatorUnit(batchConfiguration.buildParams(args), batchConfiguration, target, workers));
        return unit.addElement(args);
    }

    private void start() {
        if (status.compareAndSet(0, 1)) {
            ScheduledExecutorService scheduled = Executors.newSingleThreadScheduledExecutor();
            this.scheduled = scheduled;
            scheduled.scheduleAtFixedRate(this::startWork, 0, batchConfiguration.getWinTime(), TimeUnit.MILLISECONDS);
        }
    }

    private void startWork() {
        units.values().forEach(WindowCoordinatorUnit::startWork);
    }

    public Object getTarget() {
        return target;
    }

    @Override
    public void close() throws IOException {
        this.scheduled.shutdown();
        this.workers.shutdown();
    }
}
