package cn.cheny.toolbox.redis;

import cn.cheny.toolbox.entityCache.spring.EntityBufferProperties;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotatedTypeMetadata;

/**
 * config property:toolbox.redis.enable
 * 配置并且为true
 *
 * @author cheney
 * @date 2020-09-03
 */
public class ConditionalOnToolBoxRedisEnable implements Condition {

    @Override
    public boolean matches(ConditionContext conditionContext, AnnotatedTypeMetadata annotatedTypeMetadata) {
        Environment env = conditionContext.getEnvironment();
        Boolean property = env.getProperty(EntityBufferProperties.CACHE_PREFIX + ".enable", Boolean.class);
        return property != null && property;
    }

}
