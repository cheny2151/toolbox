package cn.cheny.toolbox.redis;

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

    public static final String CACHE_PREFIX = "toolbox.redis.enable";

    @Override
    public boolean matches(ConditionContext conditionContext, AnnotatedTypeMetadata annotatedTypeMetadata) {
        Environment env = conditionContext.getEnvironment();
        // 默认开启
        return env.getProperty(CACHE_PREFIX, Boolean.class, true);
    }

}
