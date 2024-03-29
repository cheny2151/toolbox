package cn.cheny.toolbox.redis.lock.executor;

import cn.cheny.toolbox.redis.exception.RedisRuntimeException;
import cn.cheny.toolbox.redis.exception.RedisScriptException;
import io.lettuce.core.ScriptOutputType;
import io.lettuce.core.api.async.RedisScriptingAsyncCommands;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisCluster;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 整合spring执行lua脚本
 *
 * @author cheney
 * @date 2020-08-17
 */
@Slf4j
public class SpringRedisExecutor implements RedisExecutor {

    /**
     * 连接模式
     * 1:jedis;2:JedisCluster;3:lettuce
     */
    private final static int CONNECTION_UNCHECK = 0;
    private final static int CONNECTION_JEDIS = 1;
    private final static int CONNECTION_JEDISCLUSTER = 2;
    private final static int CONNECTION_LETTUCE = 3;

    private int connectionModel = CONNECTION_UNCHECK;

    private final RedisTemplate<String, String> redisTemplate;


    public SpringRedisExecutor(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public Object execute(String script, List<String> keys, List<String> args) {
        return execute(redisTemplate, script, keys, args);
    }

    @Override
    public void set(String key, String val) {
        redisTemplate.opsForValue().set(key, val);
    }

    @Override
    public String get(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    @Override
    public String hget(String key, String hkey) {
        HashOperations<String, String, String> hashOperations = redisTemplate.opsForHash();
        return hashOperations.get(key, hkey);
    }

    @Override
    public boolean hasKey(String key) {
        Boolean hasKey = redisTemplate.hasKey(key);
        return hasKey != null && hasKey;
    }

    @Override
    public void del(String key) {
        byte[] rawKey = key.getBytes(StandardCharsets.UTF_8);
        redisTemplate.execute(connection -> connection.del(new byte[][]{rawKey}), true);
    }

    @Override
    public boolean expire(String key, long time, TimeUnit timeUnit) {
        Boolean expire = redisTemplate.expire(key, time, timeUnit);
        return expire != null && expire;
    }

    @Override
    public Long incr(String key) {
        return redisTemplate.opsForValue().increment(key);
    }

    @Override
    public Long incrBy(String key, long val) {
        return redisTemplate.opsForValue().increment(key, val);
    }

    @Override
    public Map<String, String> hgetall(String key) {
        HashOperations<String, String, String> hashOperations = redisTemplate.opsForHash();
        return hashOperations.entries(key);
    }

    @Override
    public void hmset(String key, Map<String, String> map) {
        redisTemplate.opsForHash().putAll(key, map);
    }

    @Override
    public void hset(String key, String hk, String hv) {
        redisTemplate.opsForHash().put(key, hk, hv);
    }

    @Override
    public Long hincrBy(String key, String hk, long val) {
        return redisTemplate.opsForHash().increment(key, hk, val);
    }

    @Override
    public void hdel(String key, String... hkey) {
        HashOperations<String, String, String> hashOperations = redisTemplate.opsForHash();
        hashOperations.delete(key, (Object[]) hkey);
    }

    @Override
    public void publish(String channel, String msg) {
        redisTemplate.convertAndSend(channel, msg);
    }

    private Object execute(RedisTemplate<String, String> redisTemplate, String script, List<String> keys, List<String> args) {
        final List<String> finalKeys = keys == null ? Collections.emptyList() : keys;
        final List<String> finalArgs = args == null ? Collections.emptyList() : args;

        try {
            return redisTemplate.execute((RedisCallback<Object>) (redisConnection) -> {

                Object nativeConnection = redisConnection.getNativeConnection();
                Object result;
                if (connectionModel == CONNECTION_UNCHECK &&
                        !tryJedis(nativeConnection) &&
                        !tryJedisCluster(nativeConnection) &&
                        !tryLettuce(nativeConnection)) {
                    throw new RedisScriptException("nativeConnection [" + nativeConnection.getClass()
                            + "] is not Jedis/JedisCluster/RedisScriptingAsyncCommands instance");
                }
                if (connectionModel == CONNECTION_JEDIS) {
                    // jedis单机模式
                    result = ((Jedis) nativeConnection).eval(script, finalKeys, finalArgs);
                } else if (connectionModel == CONNECTION_JEDISCLUSTER) {
                    // jedis集群模式
                    result = ((JedisCluster) nativeConnection).eval(script, finalKeys, finalArgs);
                } else {
                    // lettuce
                    try {
                        @SuppressWarnings("unchecked")
                        RedisScriptingAsyncCommands<Object, Object> commands = (RedisScriptingAsyncCommands<Object, Object>) nativeConnection;
                        result = commands
                                .eval(script, ScriptOutputType.INTEGER,
                                        toBytes(finalKeys),
                                        toBytes(finalArgs))
                                .get();
                    } catch (Exception e) {
                        if (log.isDebugEnabled()) {
                            log.debug("Error running redis lua script", e);
                        }
                        throw new RedisScriptException("Error running redis lua script", e);
                    }
                }
                return result;
            });
        } catch (
                RedisRuntimeException rre) {
            throw rre;
        } catch (
                Throwable e) {
            throw new RedisScriptException("Error running redis lua script", e);
        }

    }

    private Object[] toBytes(List<String> list) {
        return list.stream().map(String::getBytes).toArray();
    }

    private boolean tryJedis(Object nativeConnection) {
        try {
            if (nativeConnection instanceof Jedis) {
                this.connectionModel = CONNECTION_JEDIS;
                return true;
            }
        } catch (NoClassDefFoundError e) {
            // do nothing
        }
        return false;
    }

    private boolean tryJedisCluster(Object nativeConnection) {
        try {
            if (nativeConnection instanceof JedisCluster) {
                this.connectionModel = CONNECTION_JEDISCLUSTER;
                return true;
            }
        } catch (NoClassDefFoundError e) {
            // do nothing
        }
        return false;
    }

    private boolean tryLettuce(Object nativeConnection) {
        try {
            if (nativeConnection instanceof RedisScriptingAsyncCommands) {
                this.connectionModel = CONNECTION_LETTUCE;
                return true;
            }
        } catch (NoClassDefFoundError e) {
            // do nothing
        }
        return false;
    }
}
