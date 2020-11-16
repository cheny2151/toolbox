package cn.cheny.toolbox.redis.lock.executor;

import cn.cheny.toolbox.redis.exception.RedisRuntimeException;
import cn.cheny.toolbox.redis.exception.RedisScriptException;
import io.lettuce.core.ScriptOutputType;
import io.lettuce.core.api.async.RedisScriptingAsyncCommands;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisCluster;

import java.util.Collections;
import java.util.List;

/**
 * 整合spring执行lua脚本
 *
 * @author cheney
 * @date 2020-08-17
 */
@Slf4j
public class SpringRedisExecutor implements RedisExecutor {

    private RedisTemplate redisTemplate;

    public SpringRedisExecutor(RedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public Object execute(String script, List<String> keys, List<String> args) {
        return execute(redisTemplate, script, keys, args);
    }

    private Object execute(RedisTemplate<String, String> redisTemplate, String script, List<String> keys, List<String> args) {
        final List<String> finalKeys = keys == null ? Collections.emptyList() : keys;
        final List<String> finalArgs = args == null ? Collections.emptyList() : args;

        try {
            return redisTemplate.execute((RedisCallback<Object>) (redisConnection) -> {

                Object nativeConnection = redisConnection.getNativeConnection();
                Object result = null;
                // jedis单机模式
                try {
                    if (nativeConnection instanceof Jedis) {
                        result = ((Jedis) nativeConnection).eval(script, finalKeys, finalArgs);
                    }
                } catch (NoClassDefFoundError e) {
                    // do nothing
                }
                // jedis集群模式
                try {
                    if (nativeConnection instanceof JedisCluster) {
                        result = ((JedisCluster) nativeConnection).eval(script, finalKeys, finalArgs);
                    }
                } catch (NoClassDefFoundError e) {
                    // do nothing
                }
                // lettuce
                try {
                    if (nativeConnection instanceof RedisScriptingAsyncCommands) {
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
                } catch (NoClassDefFoundError e) {
                    throw new RedisScriptException("nativeConnection [" + nativeConnection.getClass()
                            + "] is not Jedis/JedisCluster/RedisScriptingAsyncCommands instance");
                }

                return result;
            });
        } catch (RedisRuntimeException rre) {
            throw rre;
        } catch (Throwable e) {
            throw new RedisScriptException("Error running redis lua script", e);
        }

    }

    private Object[] toBytes(List<String> list) {
        return list.stream().map(String::getBytes).toArray();
    }
}
