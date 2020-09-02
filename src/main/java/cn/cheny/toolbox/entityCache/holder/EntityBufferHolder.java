package cn.cheny.toolbox.entityCache.holder;

import cn.cheny.toolbox.entityCache.annotation.CacheEntity;

import java.util.List;
import java.util.Optional;

/**
 * 实体缓存持有者
 *
 * @author cheney
 * @date 2020-08-31
 */
public interface EntityBufferHolder {

    /**
     * 刷新所有缓存
     *
     * @param clazz 缓存的实体类，需要带有注解{@link CacheEntity}
     */
    <T> void refreshCache(Class<T> clazz);

    /**
     * 获取所有缓存
     *
     * @param clazz 缓存的实体类，需要带有注解{@link CacheEntity}
     */
    <T> List<T> getAllCache(Class<T> clazz);

    /**
     * 根据主键获取指定缓存
     *
     * @param id    唯一键值 字段上带有{@link cache.annotation.CacheId}注解则为该实体唯一键值
     * @param clazz 缓存的实体类，需要带有注解{@link CacheEntity}
     */
    <T> Optional<T> getById(Object id, Class<T> clazz);


    /**
     * 根据带有主键的实体获取缓存
     *
     * @param entityWithId 带有缓存的主键的实体
     * @return 缓存
     */
    <T> Optional<T> get(T entityWithId);

}
