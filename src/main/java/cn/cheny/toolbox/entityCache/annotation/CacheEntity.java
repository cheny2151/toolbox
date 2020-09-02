package cn.cheny.toolbox.entityCache.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标记缓存实体
 *
 * @author cheney
 * @date 2020-08-31
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface CacheEntity {

    String tableName();

    CacheFilter[] sqlFilter() default {};


}
