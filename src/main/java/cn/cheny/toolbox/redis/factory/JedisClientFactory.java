package cn.cheny.toolbox.redis.factory;

import cn.cheny.toolbox.redis.client.jedis.JedisClient;
import redis.clients.jedis.JedisPoolConfig;

/**
 * jedis client工厂
 *
 * @author cheney
 * @date 2020-10-26
 */
public class JedisClientFactory {

    private volatile JedisClient instance;

    private String nodes;

    private Integer timeout;

    private Integer maxAttempts;

    private String password;

    private JedisPoolConfig jedisPoolConfig;

    public JedisClientFactory(String nodes) {
        this.nodes = nodes;
    }

    public JedisClientFactory(String nodes, String password) {
        this.nodes = nodes;
        this.password = password;
    }

    public JedisClientFactory(String nodes, Integer timeout, Integer maxAttempts, JedisPoolConfig jedisPoolConfig) {
        this.nodes = nodes;
        this.timeout = timeout;
        this.maxAttempts = maxAttempts;
        this.jedisPoolConfig = jedisPoolConfig;
    }

    public JedisClientFactory(String nodes, Integer timeout, Integer maxAttempts, String password, JedisPoolConfig jedisPoolConfig) {
        this.nodes = nodes;
        this.timeout = timeout;
        this.maxAttempts = maxAttempts;
        this.password = password;
        this.jedisPoolConfig = jedisPoolConfig;
    }

    public JedisClient newJedisClient() {
        return new JedisClient(nodes, timeout, maxAttempts, password, jedisPoolConfig);
    }

    public JedisClient cacheClient() {
        if (instance == null) {
            synchronized (this) {
                if (instance == null) {
                    instance = newJedisClient();
                }
            }
        }
        return instance;
    }


    public String getNodes() {
        return nodes;
    }

    public JedisClientFactory nodes(String nodes) {
        this.nodes = nodes;
        return this;
    }

    public Integer getTimeout() {
        return timeout;
    }

    public JedisClientFactory timeout(Integer timeout) {
        this.timeout = timeout;
        return this;
    }

    public Integer getMaxAttempts() {
        return maxAttempts;
    }

    public JedisClientFactory maxAttempts(Integer maxAttempts) {
        this.maxAttempts = maxAttempts;
        return this;
    }

    public String getPassword() {
        return password;
    }

    public JedisClientFactory password(String password) {
        this.password = password;
        return this;
    }

    public JedisPoolConfig getJedisPoolConfig() {
        return jedisPoolConfig;
    }

    public JedisClientFactory jedisPoolConfig(JedisPoolConfig jedisPoolConfig) {
        this.jedisPoolConfig = jedisPoolConfig;
        return this;
    }
}
