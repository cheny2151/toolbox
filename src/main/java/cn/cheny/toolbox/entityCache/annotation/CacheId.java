package cn.cheny.toolbox.entityCache.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 实体缓存主键，添加此注解的字段将作为缓存的主键
 *
 * @author cheney
 * @date 2020-08-31
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface CacheId {
}
