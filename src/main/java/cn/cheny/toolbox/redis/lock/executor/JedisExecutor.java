package cn.cheny.toolbox.redis.lock.executor;

import cn.cheny.toolbox.redis.client.jedis.JedisClient;

import java.util.Collections;
import java.util.List;

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
}
