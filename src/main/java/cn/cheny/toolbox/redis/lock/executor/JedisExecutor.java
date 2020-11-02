package cn.cheny.toolbox.redis.lock.executor;

import cn.cheny.toolbox.redis.client.jedis.JedisClient;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * jedis实现Lua脚本执行器
 *
 * @author cheney
 * @date 2020-10-26
 */
public class JedisExecutor implements RedisExecutor {

    private JedisClient jedisClient;

    public JedisExecutor(JedisClient jedisClient) {
        this.jedisClient = jedisClient;
    }

    @Override
    public Object execute(String script, List<String> keys, List<String> args) {
        final List<String> finalKeys = keys == null ? Collections.emptyList() : keys;
        final List<String> finalArgs = args == null ? Collections.emptyList() : args;
        return jedisClient.eval(script, finalKeys, finalArgs);
    }

    @Override
    public void del(String key) {
        jedisClient.del(key);
    }

    @Override
    public Map<String, String> hgetall(String key) {
        return jedisClient.hgetall(key);
    }

    @Override
    public void expire(String key, long time, TimeUnit timeUnit) {
        jedisClient.expire(key, time, timeUnit);
    }

    @Override
    public boolean hasKey(String key) {
        Boolean hasKey = jedisClient.hasKey(key);
        return hasKey != null && hasKey;
    }

    @Override
    public void hset(String key, Map<String, String> map) {
        jedisClient.hset(key, map);
    }

    @Override
    public void publish(String channel, String msg) {
        jedisClient.publish(channel, msg);
    }
}
