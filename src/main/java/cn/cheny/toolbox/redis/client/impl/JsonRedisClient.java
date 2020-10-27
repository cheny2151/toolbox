package cn.cheny.toolbox.redis.client.impl;

import cn.cheny.toolbox.redis.client.spring.AbstractMapRedisClient;
import cn.cheny.toolbox.redis.client.MapRedisApi;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * @author cheney
 */
public class JsonRedisClient<V> extends AbstractMapRedisClient<V> implements MapRedisApi<V> {

    private RedisTemplate<String, V> redisTemplate;

    public JsonRedisClient(RedisTemplate<String, V> redisTemplate) {
        this.redisTemplate = redisTemplate;
        super.setRedis(redisTemplate);
    }

}
