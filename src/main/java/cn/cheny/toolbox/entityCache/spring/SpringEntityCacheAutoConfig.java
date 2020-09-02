package cn.cheny.toolbox.entityCache.spring;

import cn.cheny.toolbox.entityCache.BufferType;
import cn.cheny.toolbox.entityCache.factory.DefaultEntityBufferFactory;
import cn.cheny.toolbox.entityCache.factory.EntityBufferFactory;
import cn.cheny.toolbox.entityCache.factory.RedisEntityBufferFactory;
import cn.cheny.toolbox.entityCache.holder.DefaultEntityBufferHolder;
import cn.cheny.toolbox.entityCache.holder.EntityBufferHolder;
import cn.cheny.toolbox.redis.client.impl.JsonRedisClient;
import cn.cheny.toolbox.spring.SpringToolAutoConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

/**
 * 实体缓存自动配置器
 *
 * @author cheney
 * @date 2020-09-02
 */
@Configuration
@ConditionalOnProperty(prefix = EntityBufferProperties.CACHE_PREFIX, name = "type")
@EnableConfigurationProperties({EntityBufferProperties.class})
@AutoConfigureAfter(SpringToolAutoConfig.class)
public class SpringEntityCacheAutoConfig {

    private final EntityBufferProperties entityBufferProperties;

    public SpringEntityCacheAutoConfig(EntityBufferProperties entityBufferProperties) {
        this.entityBufferProperties = entityBufferProperties;
    }

    @Bean
    public EntityBufferFactory entityBufferFactory(@Autowired(required = false) JsonRedisClient jsonRedisClient) {
        if (BufferType.REDIS.equals(entityBufferProperties.getType()) &&
                jsonRedisClient != null) {
            return new RedisEntityBufferFactory(jsonRedisClient, entityBufferProperties.isUnderline());
        } else {
            return new DefaultEntityBufferFactory(entityBufferProperties.isUnderline());
        }
    }

    @Bean
    @DependsOn("toolbox:springUtils")
    public EntityBufferHolder entityBufferHolder(EntityBufferFactory entityBufferFactory) {
        return new DefaultEntityBufferHolder(entityBufferFactory);
    }

}
