package cn.cheny.toolbox.entityCache.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 实体缓存字段，标记需要缓存的字段
 *
 * @author cheney
 * @date 2020-08-31
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface CacheField {

}
