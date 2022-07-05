package cn.cheny.toolbox.window.coordinator;

import cn.cheny.toolbox.exception.WindowElementEmptyException;
import cn.cheny.toolbox.other.NamePrefixThreadFactory;
import cn.cheny.toolbox.window.BatchConfiguration;
import cn.cheny.toolbox.window.CollectedStaticParams;
import cn.cheny.toolbox.window.WindowElement;
import lombok.extern.slf4j.Slf4j;

import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 窗口协调类
 *
 * @author by chenyi
 * @date 2021/9/22
 */
@Slf4j
public class WindowCoordinator implements Closeable {

    private final ExecutorService workers;

    private final Object target;

    private final BatchConfiguration batchConfiguration;

    private final ConcurrentHashMap<CollectedStaticParams, WindowCoordinatorUnit> units;

    public WindowCoordinator(Object target, BatchConfiguration batchConfiguration) {
        this.target = target;
        this.batchConfiguration = batchConfiguration;
        this.units = new ConcurrentHashMap<>();
        this.workers = Executors.newFixedThreadPool(batchConfiguration.getThreadPoolSize(), new NamePrefixThreadFactory("BatchHandler"));
    }

    public WindowElement addElement(Object[] args) throws WindowElementEmptyException, InterruptedException {
        CollectedStaticParams staticParams = batchConfiguration.buildStaticParams(args, batchConfiguration);
        WindowCoordinatorUnit unit = units.computeIfAbsent(staticParams,
                k -> new WindowCoordinatorUnit(batchConfiguration.buildParams(args), batchConfiguration, target, workers));
        return unit.addElement(args);
    }

    public Object getTarget() {
        return target;
    }

    @Override
    public void close() throws IOException {
        for (WindowCoordinatorUnit unit : this.units.values()) {
            try {
                unit.close();
            } catch (Exception e) {
                // do nothing
            }
        }
        this.workers.shutdown();
    }
}
