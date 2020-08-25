package cn.cheny.toolbox.redis.client.impl;

import cn.cheny.toolbox.redis.client.AbstractMapRedisClient;
import cn.cheny.toolbox.redis.client.MapRedisApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;

import javax.annotation.Resource;

/**
 * @author cheney
 */
public class JsonRedisClient<V> extends AbstractMapRedisClient<V> implements MapRedisApi<V> {

    @Resource(name = "jsonRedisTemplate")
    private RedisTemplate<String, V> redisTemplate;

    @Autowired
    protected void setRedis(@Qualifier("jsonRedisTemplate") RedisTemplate<String, V> redis) {
        super.setRedis(redis);
    }


}
