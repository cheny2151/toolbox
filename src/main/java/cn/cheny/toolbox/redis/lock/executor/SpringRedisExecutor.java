package cn.cheny.toolbox.redis.lock.executor;

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

    private Object execute(RedisTemplate redisTemplate, String script, List<String> keys, List<String> args) {
        final List<String> finalKeys = keys == null ? Collections.emptyList() : keys;
        final List<String> finalArgs = args == null ? Collections.emptyList() : args;

        try {
            return redisTemplate.execute((RedisCallback<Object>) (redisConnection) -> {

                Object nativeConnection = redisConnection.getNativeConnection();
                Object result;
                if (nativeConnection instanceof Jedis) {
                    // 单机模式
                    result = ((Jedis) nativeConnection).eval(script, finalKeys, finalArgs);
                } else if (nativeConnection instanceof JedisCluster) {
                    // 集群模式
                    result = ((JedisCluster) nativeConnection).eval(script, finalKeys, finalArgs);
                } else if (nativeConnection instanceof RedisScriptingAsyncCommands) {
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
                } else {
                    throw new RedisScriptException("nativeConnection [" + nativeConnection.getClass()
                            + "] is not Jedis/JedisCluster/RedisScriptingAsyncCommands instance");
                }

                return result;
            });
        } catch (RedisScriptException rse) {
            throw rse;
        } catch (Throwable e) {
            throw new RedisScriptException("Error running redis lua script", e);
        }

    }

    private Object[] toBytes(List<String> list) {
        return list.stream().map(String::getBytes).toArray();
    }
}
