package cn.cheny.toolbox.entityCache.buffer;

import java.util.List;
import java.util.Optional;

/**
 * entity buffer -- redis jedis缓存实现
 *
 * @author cheney
 * @date 2020-09-01
 */
public class RedisEntityBuffer<T> extends BaseEntityBuffer<T> {

    public RedisEntityBuffer(Class<T> entityClass, boolean underline) {
        super(entityClass, underline);
    }

    @Override
    public void cache(T entity) {

    }

    @Override
    public void refresh(List<T> entities) {

    }

    @Override
    public List<T> getAllCache() {
        return null;
    }

    @Override
    public void remove(T entityWithId) {

    }

    @Override
    public Optional<T> get(T entityWithId) {
        return Optional.empty();
    }

    @Override
    public Optional<T> getById(Object key) {
        return Optional.empty();
    }
}
