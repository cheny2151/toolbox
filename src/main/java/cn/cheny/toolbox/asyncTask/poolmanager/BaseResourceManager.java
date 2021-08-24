package cn.cheny.toolbox.asyncTask.poolmanager;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author by chenyi
 * @date 2021/8/13
 */
@Slf4j
public abstract class BaseResourceManager<R> implements ResourceManager<R> {

    protected final static long DEFAULT_DELAY_SECONDS = 60;

    protected ScheduledExecutorService checkWorker = Executors.newSingleThreadScheduledExecutor();

    public BaseResourceManager() {
        this(DEFAULT_DELAY_SECONDS, TimeUnit.SECONDS);
    }

    public BaseResourceManager(long checkPeriod, TimeUnit timeUnit) {
        checkWorker.scheduleAtFixedRate(this.checkTask(), checkPeriod, checkPeriod, timeUnit);
    }

    private Runnable checkTask() {
        return () -> {
            try {
                this.clear();
            } catch (Throwable t) {
                log.error("invoke clear error", t);
            }
        };
    }

}
