package cn.cheny.toolbox.redis.client.spring;

import cn.cheny.toolbox.redis.client.MapRedisApi;

import java.util.List;
import java.util.Map;

/**
 * @author cheney
 */
public abstract class AbstractMapRedisClient<V> extends AbstractRedisClient<V> implements MapRedisApi<V> {

    @Override
    public void hSetMap(String k, Map<String, V> kv, int days) {
        hSetMap(k, kv);
        expire(k, days);
    }

    @Override
    public void hSetMap(String k, Map<String, V> kv) {
        getHashOperationForMap().putAll(k, kv);
    }

    @Override
    public void hSet(String k, String hk, V v) {
        getHashOperationForMap().put(k, hk, v);
    }

    @Override
    public V hGet(String k, String hk) {
        return getHashOperationForMap().get(k, hk);
    }

    @Override
    public Map<String, V> hGetMap(String k) {
        return getHashOperationForMap().entries(k);
    }

    @Override
    public long hLen(String key) {
        return getHashOperationForMap().size(key);
    }

}
