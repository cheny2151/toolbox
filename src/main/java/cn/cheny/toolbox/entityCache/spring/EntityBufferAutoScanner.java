package cn.cheny.toolbox.entityCache.spring;

import cn.cheny.toolbox.entityCache.annotation.CacheEntity;
import cn.cheny.toolbox.entityCache.holder.EntityBufferHolder;
import cn.cheny.toolbox.scan.PathScanner;
import cn.cheny.toolbox.scan.filter.ScanFilter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 实体缓存扫描器
 *
 * @author cheney
 * @date 2020-09-02
 */
@Slf4j
public class EntityBufferAutoScanner implements InitializingBean, DisposableBean {

    /**
     * 自动刷新默认间隔时间：60分钟
     */
    private final static Long DEFAULT_PERIOD = 60L;

    /**
     * 实体缓存配置
     */
    private final EntityBufferProperties entityBufferProperties;

    /**
     * 实体缓存持有者
     */
    private final EntityBufferHolder entityBufferHolder;

    /**
     * 定时任务线程池
     */
    private ScheduledExecutorService scheduledExecutor;

    public EntityBufferAutoScanner(EntityBufferProperties entityBufferProperties, EntityBufferHolder entityBufferHolder) {
        this.entityBufferProperties = entityBufferProperties;
        this.entityBufferHolder = entityBufferHolder;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        String entityPath = entityBufferProperties.getEntityPath();
        if (StringUtils.isEmpty(entityPath)) {
            return;
        }
        ScanFilter scanFilter = new ScanFilter();
        scanFilter.addAnnotation(CacheEntity.class);
        List<Class<?>> classes = new PathScanner(scanFilter).scanClass(entityPath);
        if (classes.size() > 0) {
            scheduled(classes);
        }
    }

    /**
     * 执行任务,缓存实体
     *
     * @param classes 缓存实体class集合
     */
    private void scheduled(List<Class<?>> classes) {
        Runnable task = () -> {
            try {
                classes.parallelStream().forEach(entityBufferHolder::refreshCache);
            } catch (Throwable e) {
                log.error("Execute refresh entity buffer error,cause:", e);
            }
        };
        if (entityBufferProperties.isAutoRefresh()) {
            ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
            this.scheduledExecutor = scheduledExecutorService;
            Long period = entityBufferProperties.getPeriod() == null ? DEFAULT_PERIOD : entityBufferProperties.getPeriod();
            scheduledExecutorService.scheduleAtFixedRate(task, 0, period, TimeUnit.MINUTES);
        } else {
            new Thread(task).start();
        }
    }

    @Override
    public void destroy() throws Exception {
        if (scheduledExecutor != null) {
            scheduledExecutor.shutdownNow();
        }
    }
}
