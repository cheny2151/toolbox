package cn.cheny.toolbox.entityCache.spring;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotatedTypeMetadata;

/**
 * @author cheney
 * @date 2020-09-03
 */
public class ConditionalOnEntityBufferEnable implements Condition {

    @Override
    public boolean matches(ConditionContext conditionContext, AnnotatedTypeMetadata annotatedTypeMetadata) {
        Environment env = conditionContext.getEnvironment();
        Boolean property = env.getProperty(EntityBufferProperties.CACHE_PREFIX + ".enable", Boolean.class);
        return property != null && property;
    }

}
