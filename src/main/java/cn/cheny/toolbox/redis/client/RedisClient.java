package cn.cheny.toolbox.redis.client;

import redis.clients.jedis.JedisPubSub;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * 定义通用的redis api接口
 *
 * @author cheney
 * @date 2020-11-06
 */
public interface RedisClient<V> {

    Boolean exists(String key);

    boolean expire(String key, long time, TimeUnit timeUnit);

    Long expireAt(String key, long unixTime);

    Long pexpireAt(String key, long millisecondsTimestamp);

    void del(String... key);

    void set(V key, V val);

    V get(String key);

    Long decrBy(String key, long decrement);

    Long decr(String key);

    Long incrBy(String key, long increment);

    Double incrByFloat(String key, double increment);

    Long incr(String key);

    void hset(String key, String hkey, V hval);

    void hset(String key, Map<String, V> map);

    V hget(String key, String hkey);

    String hmset(String key, Map<String, V> hash);

    List<V> hmget(String key, String... fields);

    Map<String, V> hgetall(String key);

    Long hincrBy(String key, String field, long value);

    boolean hexists(String key, String field);

    Long hdel(String key, String... field);

    Long hlen(String key);

    Set<String> hkeys(String key);

    List<V> hvals(String key);

    Object eval(String script, List<String> Keys, List<String> Args);

    void publish(String channel, String msg);

    void psubscribe(JedisPubSub jedisPubSub, String channel);

    Object getSource();

    void close();

    boolean isClose();

}
