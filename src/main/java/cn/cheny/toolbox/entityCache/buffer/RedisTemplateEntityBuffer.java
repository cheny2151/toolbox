package cn.cheny.toolbox.entityCache.buffer;

import cn.cheny.toolbox.redis.client.impl.JsonRedisClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * entity buffer -- spring redis缓存实现
 *
 * @author cheney
 * @date 2020-09-01
 */
public class RedisTemplateEntityBuffer<T> extends BaseEntityBuffer<T> {

    /**
     * redis key前缀
     */
    private final static String KEY_PRE = "TOOLBOX:ENTITY_CACHE:";

    /**
     * redisTemplate封装类
     */
    private JsonRedisClient<T> jsonRedisClient;

    public RedisTemplateEntityBuffer(JsonRedisClient<T> jsonRedisClient, Class<T> entityClass, boolean underline) {
        super(entityClass, underline);
        this.jsonRedisClient = jsonRedisClient;
    }

    @Override
    public void cache(T entity) {
        String key = getBufferKey();
        String id = extractId(entity);
        jsonRedisClient.hSet(key, id, entity);
    }

    @Override
    public void refresh(List<T> entities) {
        String key = getBufferKey();
        jsonRedisClient.delete(key);
        jsonRedisClient.hSetMap(key, toMap(entities));
    }

    @Override
    public List<T> getAllCache() {
        String key = getBufferKey();
        return new ArrayList<>(jsonRedisClient.hGetMap(key).values());
    }

    @Override
    public void remove(T entityWithId) {
        String key = getBufferKey();
        String id = extractId(entityWithId);
        jsonRedisClient.hDel(key, id);
    }

    @Override
    public Optional<T> get(T entityWithId) {
        String key = getBufferKey();
        String id = extractId(entityWithId);
        return Optional.ofNullable(jsonRedisClient.hGet(key, id));
    }

    @Override
    public Optional<T> getById(Object key) {
        String bufferKey = getBufferKey();
        return Optional.ofNullable(jsonRedisClient.hGet(bufferKey, key == null ? "null" : key.toString()));
    }

    /**
     * 获取实体对象的缓存key：KEY_PRE + class名
     *
     * @return 缓存key
     */
    private String getBufferKey() {
        Class<T> entityClass = getEntityClass();
        return KEY_PRE + entityClass.getName();
    }
}
