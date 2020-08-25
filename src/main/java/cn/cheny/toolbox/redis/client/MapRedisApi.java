package cn.cheny.toolbox.redis.client;

import java.util.List;
import java.util.Map;

/**
 * @author cheney
 */
public interface MapRedisApi<V> {

    //------------------------------ hash for map------------------------------

    void HMSetForMap(String k, Map<String, V> kv, int days);

    void HMSetForMap(String k, Map<String, V> kv);

    void HSetForMap(String k, String hk, V v);

    V HGetForMap(String k, String hk);

    Map<String, V> HMGetForMap(String k);

    List<V> HValuesForMap(String k);

}
