package cn.cheny.toolbox.entityCache.buffer;

import cn.cheny.toolbox.entityCache.buffer.model.BufferInfo;

import java.util.List;
import java.util.Optional;

/**
 * 实体缓存器接口--Entity Buffer
 *
 * @author cheney
 * @date 2020-08-31
 */
public interface EntityBuffer<T> {

    /**
     * 获取缓存信息
     */
    BufferInfo<T> getBufferInfo();

    /**
     * 执行缓存
     *
     * @param entity 被缓存实体
     */
    void cache(T entity);

    /**
     * 刷新所有缓存
     *
     * @param entities 实体集合
     */
    void refresh(List<T> entities);

    /**
     * 获取所有缓存
     *
     * @return 所有缓存
     */
    List<T> getAllCache();

    /**
     * 移除缓存
     *
     * @param entityWithId 带有缓存的主键的实体
     */
    void remove(T entityWithId);

    /**
     * 根据带有主键的实体获取缓存
     *
     * @param entityWithId 带有缓存的主键的实体
     * @return 缓存
     */
    Optional<T> get(T entityWithId);

    /**
     * 根据主键获取缓存
     *
     * @param key 主键
     * @return 缓存
     */
    Optional<T> getById(Object key);
}
