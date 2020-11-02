package cn.cheny.toolbox.redis.client.impl;

import cn.cheny.toolbox.redis.client.spring.AbstractMapRedisClient;
import cn.cheny.toolbox.redis.client.MapRedisApi;
import cn.cheny.toolbox.redis.client.ObjectRedisApi;
import com.alibaba.fastjson.JSON;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.Map;

public class JdkRedisClient<V> extends AbstractMapRedisClient<V> implements MapRedisApi<V>, ObjectRedisApi {

    private RedisTemplate<String, V> redis;

    public JdkRedisClient(RedisTemplate<String, V> redis) {
        this.redis = redis;
        super.setRedis(redis);
    }

    private HashOperations<String, String, Object> opsForObject;

    protected HashOperations<String, String, Object> getHashOperationForObject() {
        return opsForObject == null ? (opsForObject = redis.opsForHash()) : opsForObject;
    }

    @Override
    public <HV> void HMSetObject(String k, HV kv, int days) {
        getHashOperationForObject().putAll(k, JSON.parseObject(JSON.toJSONString(kv)));
        expire(k, days);
    }

    @Override
    public <HV> void HMSetObject(String k, HV kv) {
        getHashOperationForObject().putAll(k, JSON.parseObject(JSON.toJSONString(kv)));
    }

    @Override
    public void HSetField(String k, String hk, Object v) {
        getHashOperationForObject().put(k, hk, v);
    }

    @Override
    public Object HGetField(String k, String hk) {
        return getHashOperationForObject().get(k, hk);
    }

    @Override
    public <HV> HV HMGetObject(String k, Class<HV> clazz) {
        if (!exists(k)) {
            return null;
        }
        Map<String, Object> map = getHashOperationForObject().entries(k);
        return JSON.parseObject(JSON.toJSONString(map), clazz);
    }

}
