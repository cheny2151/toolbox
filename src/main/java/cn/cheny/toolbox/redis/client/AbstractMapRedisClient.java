package cn.cheny.toolbox.redis.client;

import java.util.List;
import java.util.Map;

/**
 * @author cheney
 */
public abstract class AbstractMapRedisClient<V> extends AbstractRedisClient<V> implements MapRedisApi<V> {

    @Override
    public void HMSetForMap(String k, Map<String, V> kv, int days) {
        HMSetForMap(k, kv);
        expire(k, days);
    }

    @Override
    public void HMSetForMap(String k, Map<String, V> kv) {
        getHashOperationForMap().putAll(k, kv);
    }

    @Override
    public void HSetForMap(String k, String hk, V v) {
        getHashOperationForMap().put(k, hk, v);
    }

    @Override
    public V HGetForMap(String k, String hk) {
        return getHashOperationForMap().get(k, hk);
    }

    @Override
    public Map<String, V> HMGetForMap(String k) {
        return getHashOperationForMap().entries(k);
    }

    @Override
    public List<V> HValuesForMap(String k) {
        return getHashOperationForMap().values(k);
    }

    @Override
    public long Hlen(String key) {
        return getHashOperationForMap().size(key);
    }

}
