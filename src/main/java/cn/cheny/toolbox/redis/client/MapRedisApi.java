package cn.cheny.toolbox.redis.client;

import java.util.List;
import java.util.Map;

/**
 * @author cheney
 */
public interface MapRedisApi<V> {

    //------------------------------ hash for map------------------------------

    void hSetMap(String k, Map<String, V> kv, int days);

    void hSetMap(String k, Map<String, V> kv);

    void hSet(String k, String hk, V v);

    V hGet(String k, String hk);

    Map<String, V> hGetMap(String k);

    long hLen(String key);

}
