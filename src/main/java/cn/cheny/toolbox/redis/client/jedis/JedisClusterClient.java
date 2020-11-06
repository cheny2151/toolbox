package cn.cheny.toolbox.redis.client.jedis;

import cn.cheny.toolbox.redis.client.RedisClient;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisPubSub;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * jedis客户端多节点模式
 *
 * @author cheney
 * @date 2020-10-26
 */
public class JedisClusterClient implements RedisClient<String> {

    private final Set<HostAndPort> hostAndPorts;

    private final Integer timeout;

    private final Integer maxAttempts;

    private JedisCluster jedisCluster;

    private JedisPoolConfig jedisPoolConfig;

    private volatile boolean closed;

    public JedisClusterClient(Set<HostAndPort> hostAndPorts, Integer timeout, Integer maxAttempts, String password, JedisPoolConfig jedisPoolConfig) {
        this.hostAndPorts = hostAndPorts;
        this.timeout = timeout;
        this.maxAttempts = maxAttempts;
        this.jedisPoolConfig = jedisPoolConfig;
        initClient(password);
    }

    /**
     * 初始化JedisCluster
     */
    private void initClient(String password) {
        jedisCluster = new JedisCluster(hostAndPorts, timeout, timeout, maxAttempts, password, jedisPoolConfig);
    }

    @Override
    public Boolean exists(String key) {
        return jedisCluster.exists(key);
    }

    @Override
    public void del(String... key) {
        jedisCluster.del(key);
    }

    @Override
    public String get(String key) {
        return jedisCluster.get(key);
    }

    @Override
    public Long decrBy(String key, long decrement) {
        return jedisCluster.decrBy(key, decrement);

    }

    @Override
    public Long decr(String key) {
        return jedisCluster.decr(key);
    }

    @Override
    public Long incrBy(String key, long increment) {
        return jedisCluster.incrBy(key, increment);
    }

    @Override
    public Double incrByFloat(String key, double increment) {
        return jedisCluster.incrByFloat(key, increment);
    }

    @Override
    public Long incr(String key) {
        return jedisCluster.incr(key);
    }

    @Override
    public void hset(String key, String hkey, String hval) {
        jedisCluster.hset(key, hkey, hval);
    }

    @Override
    public void hset(String key, Map<String, String> map) {
        jedisCluster.hset(key, map);
    }

    @Override
    public String hget(String key, String hkey) {
        return jedisCluster.hget(key, hkey);
    }

    @Override
    public String hmset(String key, Map<String, String> hash) {
        return jedisCluster.hmset(key, hash);
    }

    @Override
    public List<String> hmget(String key, String... fields) {
        return jedisCluster.hmget(key, fields);
    }

    @Override
    public Map<String, String> hgetall(String key) {
        return jedisCluster.hgetAll(key);
    }

    @Override
    public Long hincrBy(String key, String field, long value) {
        return jedisCluster.hincrBy(key, field, value);
    }

    @Override
    public boolean hexists(String key, String field) {
        return jedisCluster.hexists(key, field);
    }

    @Override
    public Long hdel(String key, String... field) {
        return jedisCluster.hdel(key, field);
    }

    @Override
    public Long hlen(String key) {
        return jedisCluster.hlen(key);
    }

    @Override
    public Set<String> hkeys(String key) {
        return jedisCluster.hkeys(key);
    }

    @Override
    public List<String> hvals(String key) {
        return jedisCluster.hvals(key);
    }

    @Override
    public void expire(String key, long time, TimeUnit timeUnit) {
        jedisCluster.pexpire(key, timeUnit.toMillis(time));
    }

    @Override
    public Long expireAt(String key, long unixTime) {
        return jedisCluster.expireAt(key, unixTime);
    }

    @Override
    public Long pexpireAt(String key, long millisecondsTimestamp) {
        return jedisCluster.pexpireAt(key, millisecondsTimestamp);
    }

    @Override
    public Object eval(String script, List<String> finalKeys, List<String> finalArgs) {
        return jedisCluster.eval(script, finalKeys, finalArgs);
    }

    @Override
    public void publish(String channel, String msg) {
        jedisCluster.publish(channel, msg);
    }

    @Override
    public void psubscribe(JedisPubSub jedisPubSub, String channel) {
        jedisCluster.psubscribe(jedisPubSub, channel);
    }

    @Override
    public Object getSource() {
        return jedisCluster;
    }

    @Override
    public void close() {
        jedisCluster.close();
        closed = true;
    }

    @Override
    public boolean isClose() {
        return closed;
    }

    public Set<HostAndPort> getHostAndPorts() {
        return hostAndPorts;
    }

    public Integer getTimeout() {
        return timeout;
    }

    public Integer getMaxAttempts() {
        return maxAttempts;
    }

    public JedisCluster getJedisCluster() {
        return jedisCluster;
    }

    public JedisPoolConfig getJedisPoolConfig() {
        return jedisPoolConfig;
    }

}
