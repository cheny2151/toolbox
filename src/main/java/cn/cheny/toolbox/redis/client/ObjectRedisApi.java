package cn.cheny.toolbox.redis.client;

/**
 * 用于泛型继承树
 *
 */
public interface ObjectRedisApi {

    //------------------------------ hash for object------------------------------

    <V> void HMSetObject(String k, V kv, int days);

    <V> void HMSetObject(String k, V kv);

    void HSetField(String k, String hk, Object v);

    Object HGetField(String k, String hk);

    <V> V HMGetObject(String k, Class<V> clazz);

}
