package cn.cheny.toolbox.redis.lock.executor;

import cn.cheny.toolbox.redis.ReturnType;
import cn.cheny.toolbox.redis.client.RedisClient;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * jedis命令执行器
 *
 * @author cheney
 * @date 2020-10-26
 */
public class JedisExecutor implements RedisExecutor {

    private RedisClient<String> jedisClient;

    public JedisExecutor(RedisClient<String> jedisClient) {
        this.jedisClient = jedisClient;
    }

    @Override
    public Object execute(String script, List<String> keys, List<String> args, ReturnType returnType) {
        return this.execute(script, keys, args);
    }

    @Override
    public Object execute(String script, List<String> keys, List<String> args) {
        final List<String> finalKeys = keys == null ? Collections.emptyList() : keys;
        final List<String> finalArgs = args == null ? Collections.emptyList() : args;
        return jedisClient.eval(script, finalKeys, finalArgs);
    }

    @Override
    public void set(String key, String val) {
        jedisClient.set(key, val);
    }

    @Override
    public String get(String key) {
        return jedisClient.get(key);
    }

    @Override
    public void del(String key) {
        jedisClient.del(key);
    }

    @Override
    public boolean hasKey(String key) {
        return jedisClient.exists(key);
    }

    @Override
    public Map<String, String> hgetall(String key) {
        return jedisClient.hgetall(key);
    }

    @Override
    public String hget(String key, String hkey) {
        return jedisClient.hget(key, hkey);
    }

    @Override
    public void hmset(String key, Map<String, String> map) {
        jedisClient.hset(key, map);
    }

    @Override
    public void hset(String key, String hk, String hv) {
        jedisClient.hset(key, hk, hv);
    }

    @Override
    public Long hincrBy(String key, String hk, long val) {
        return jedisClient.hincrBy(key, hk, val);
    }

    @Override
    public void hdel(String key, String... hkey) {
        jedisClient.hdel(key, hkey);
    }

    @Override
    public boolean expire(String key, long time, TimeUnit timeUnit) {
        return jedisClient.expire(key, time, timeUnit);
    }

    @Override
    public Long incr(String key) {
        return jedisClient.incr(key);
    }

    @Override
    public Long incrBy(String key, long val) {
        return jedisClient.incrBy(key, val);
    }

    @Override
    public void publish(String channel, String msg) {
        jedisClient.publish(channel, msg);
    }
}
