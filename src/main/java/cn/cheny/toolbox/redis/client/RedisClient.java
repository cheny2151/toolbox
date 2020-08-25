package cn.cheny.toolbox.redis.client;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public interface RedisClient<V> {

    //------------------------------ common ------------------------------

    void expire(String k, int days);

    void expire(String k, long timeout, TimeUnit timeUnit);

    void delete(String k);

    void removeKey(String k);

    boolean exists(String k);

    long getExpire(String k, TimeUnit timeUnit);

    //------------------------------ value ------------------------------

    void setValue(String k, V v, int days);

    void setValue(String k, V v);

    V getValue(String k);

    //------------------------------ list ------------------------------

    void addList(String k, List<V> values);

    void addList(String k, V v);

    void rightPushList(String k, List<V> values);

    void rightPush(String k, V v);

    void leftPushList(String k, List<V> values);

    void leftPush(String k, V v);

    List<V> getList(String k);

    V rightPop(String k);

    V leftPop(String k);

    Long listSize(String k);

    //------------------------------ hash common------------------------------

    boolean HHasKey(String k, String hk);

    Set<String> HKeys(String k);

    long HDel(String k, String hk);

    List<V> HMGet(String k,Collection<String> hks);

    List<V> HVals(String k);


}
