package cn.cheny.toolbox.redis.client.jedis;

import cn.cheny.toolbox.redis.client.RedisClient;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisPubSub;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * jedis客户端单节点模式
 *
 * @author cheney
 * @date 2020-10-26
 */
public class JedisClient implements RedisClient<String> {

    private final String host;

    private final int port;

    private final Integer timeout;

    private final Integer maxAttempts;

    private JedisPool jedisPool;

    private JedisPoolConfig jedisPoolConfig;

    public JedisClient(String host, int port, Integer timeout, Integer maxAttempts, String password, JedisPoolConfig jedisPoolConfig) {
        this.host = host;
        this.port = port;
        this.timeout = timeout;
        this.maxAttempts = maxAttempts;
        this.jedisPoolConfig = jedisPoolConfig;
        initClient(password);
    }

    /**
     * 初始化JedisPool
     */
    private void initClient(String password) {
        jedisPool = new JedisPool(jedisPoolConfig, host, port, timeout, password);
    }

    @Override
    public Boolean exists(String key) {
        return execAndClose(jedis -> jedis.exists(key));
    }

    @Override
    public void del(String... key) {
        execAndClose(jedis -> jedis.del(key));
    }

    @Override
    public String get(String key) {
        return execAndClose(jedis -> jedis.get(key));
    }

    @Override
    public Long decrBy(String key, long decrement) {
        return execAndClose(jedis -> jedis.decrBy(key, decrement));
    }

    @Override
    public Long decr(String key) {
        return execAndClose(jedis -> jedis.decr(key));
    }

    @Override
    public Long incrBy(String key, long increment) {
        return execAndClose(jedis -> jedis.incrBy(key, increment));
    }

    @Override
    public Double incrByFloat(String key, double increment) {
        return execAndClose(jedis -> jedis.incrByFloat(key, increment));
    }

    @Override
    public Long incr(String key) {
        return execAndClose(jedis -> jedis.incr(key));
    }

    @Override
    public void hset(String key, String hkey, String hval) {
        execAndClose(jedis -> jedis.hset(key, hkey, hval));
    }

    @Override
    public void hset(String key, Map<String, String> map) {
        execAndClose(jedis -> jedis.hset(key, map));
    }

    @Override
    public String hget(String key, String hkey) {
        return execAndClose(jedis -> jedis.hget(key, hkey));
    }

    @Override
    public String hmset(String key, Map<String, String> hash) {
        return execAndClose(jedis -> jedis.hmset(key, hash));
    }

    @Override
    public List<String> hmget(String key, String... fields) {
        return execAndClose(jedis -> jedis.hmget(key, fields));
    }

    @Override
    public Map<String, String> hgetall(String key) {
        return execAndClose(jedis -> jedis.hgetAll(key));
    }

    @Override
    public Long hincrBy(String key, String field, long value) {
        return execAndClose(jedis -> jedis.hincrBy(key, field, value));
    }

    @Override
    public boolean hexists(String key, String field) {
        Boolean exists = execAndClose(jedis -> jedis.hexists(key, field));
        return exists != null && exists;
    }

    @Override
    public Long hdel(String key, String... field) {
        return execAndClose(jedis -> jedis.hdel(key, field));
    }

    @Override
    public Long hlen(String key) {
        return execAndClose(jedis -> jedis.hlen(key));
    }

    @Override
    public Set<String> hkeys(String key) {
        return execAndClose(jedis -> jedis.hkeys(key));
    }

    @Override
    public List<String> hvals(String key) {
        return execAndClose(jedis -> jedis.hvals(key));
    }

    @Override
    public void expire(String key, long time, TimeUnit timeUnit) {
        execAndClose(jedis -> jedis.pexpire(key, timeUnit.toMillis(time)));
    }

    @Override
    public Long expireAt(String key, long unixTime) {
        return execAndClose(jedis -> jedis.expireAt(key, unixTime));
    }

    @Override
    public Long pexpireAt(String key, long millisecondsTimestamp) {
        return execAndClose(jedis -> jedis.pexpireAt(key, millisecondsTimestamp));
    }

    @Override
    public Object eval(String script, List<String> finalKeys, List<String> finalArgs) {
        return execAndClose(jedis -> jedis.eval(script, finalKeys, finalArgs));
    }

    @Override
    public void publish(String channel, String msg) {
        execAndClose(jedis -> jedis.publish(channel, msg));
    }

    @Override
    public void psubscribe(JedisPubSub jedisPubSub, String channel) {
        execAndClose(jedis -> {
            jedis.psubscribe(jedisPubSub, channel);
            return null;
        });
    }

    @Override
    public Object getSource() {
        return jedisPool;
    }

    @Override
    public void close() {
        jedisPool.destroy();
    }

    @Override
    public boolean isClose() {
        return jedisPool.isClosed();
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public Integer getTimeout() {
        return timeout;
    }

    public Integer getMaxAttempts() {
        return maxAttempts;
    }

    public JedisPool getJedisPool() {
        return jedisPool;
    }

    public JedisPoolConfig getJedisPoolConfig() {
        return jedisPoolConfig;
    }

    private <T> T execAndClose(Function<Jedis, T> jedisCommand) {
        Jedis jedis = jedisPool.getResource();
        T result;
        try {
            result = jedisCommand.apply(jedis);
        } finally {
            jedis.close();
        }
        return result;
    }

}
