package cn.cheny.toolbox.entityCache.factory;

import cn.cheny.toolbox.entityCache.buffer.EntityBuffer;
import cn.cheny.toolbox.entityCache.buffer.RedisTemplateEntityBuffer;
import cn.cheny.toolbox.redis.client.impl.JsonRedisClient;

/**
 * 默认entity buffer工厂实现 -- 内存模式
 *
 * @author cheney
 * @date 2020-09-02
 */
public class RedisEntityBufferFactory extends BaseEntityBufferFactory {

    private JsonRedisClient jsonRedisClient;

    public RedisEntityBufferFactory(JsonRedisClient jsonRedisClient, boolean underline) {
        super(underline);
        this.jsonRedisClient = jsonRedisClient;
    }

    public <T> EntityBuffer<T> createBuffer(Class<T> clazz) {
        return new RedisTemplateEntityBuffer<>(jsonRedisClient, clazz, isUnderline());
    }

}
