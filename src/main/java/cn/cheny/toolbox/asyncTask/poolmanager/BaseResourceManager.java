package cn.cheny.toolbox.asyncTask.poolmanager;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author by chenyi
 * @date 2021/8/13
 */
public abstract class BaseResourceManager<R> implements ResourceManager<R> {

    protected final static long DEFAULT_DELAY_SECONDS = 60;

    protected ScheduledExecutorService checkWorker = Executors.newSingleThreadScheduledExecutor();

    public BaseResourceManager() {
        this(DEFAULT_DELAY_SECONDS);
    }

    public BaseResourceManager(long checkPeriod) {
        checkWorker.scheduleAtFixedRate(this.checkTask(), DEFAULT_DELAY_SECONDS, checkPeriod, TimeUnit.SECONDS);
    }

    private Runnable checkTask() {
        return this::clear;
    }

}
