package cn.cheny.toolbox.entityCache.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 实体缓存sql过滤条件注解
 *
 * @author cheney
 * @date 2020-08-31
 */
@Target(ElementType.ANNOTATION_TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface CacheFilter {

    String value();

}
