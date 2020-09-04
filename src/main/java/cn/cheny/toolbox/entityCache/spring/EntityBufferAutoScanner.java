package cn.cheny.toolbox.entityCache.spring;

import cn.cheny.toolbox.entityCache.annotation.CacheEntity;
import cn.cheny.toolbox.entityCache.holder.EntityBufferHolder;
import cn.cheny.toolbox.scan.PathScanner;
import cn.cheny.toolbox.scan.filter.ScanFilter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.InitializingBean;

import java.util.List;

/**
 * 实体缓存扫描器
 *
 * @author cheney
 * @date 2020-09-02
 */
@Slf4j
public class EntityBufferAutoScanner implements InitializingBean {

    /**
     * 实体缓存配置
     */
    private final EntityBufferProperties entityBufferProperties;

    /**
     * 实体缓存持有者
     */
    private final EntityBufferHolder entityBufferHolder;

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
            if (log.isDebugEnabled()) {
                log.debug("scan buffer entity,total:{}", classes.size());
            }
            new Thread(() -> {
                classes.parallelStream().forEach(entityBufferHolder::refreshCache);
            }).start();
        }
    }
}
