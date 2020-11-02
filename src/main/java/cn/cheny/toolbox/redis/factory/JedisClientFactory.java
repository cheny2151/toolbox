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

    private JedisClient instance;

    private String nodes;

    private Integer timeout;

    private Integer maxAttempts;

    private JedisPoolConfig jedisPoolConfig;

    public JedisClientFactory(String nodes) {
        this.nodes = nodes;
    }

    public JedisClientFactory(String nodes, Integer timeout, Integer maxAttempts, JedisPoolConfig jedisPoolConfig) {
        this.nodes = nodes;
        this.timeout = timeout;
        this.maxAttempts = maxAttempts;
        this.jedisPoolConfig = jedisPoolConfig;
    }

    public JedisClient newJedisClient() {
        return new JedisClient(nodes, timeout, maxAttempts, jedisPoolConfig);
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

    public void setNodes(String nodes) {
        this.nodes = nodes;
    }

    public Integer getTimeout() {
        return timeout;
    }

    public void setTimeout(Integer timeout) {
        this.timeout = timeout;
    }

    public Integer getMaxAttempts() {
        return maxAttempts;
    }

    public void setMaxAttempts(Integer maxAttempts) {
        this.maxAttempts = maxAttempts;
    }

    public JedisPoolConfig getJedisPoolConfig() {
        return jedisPoolConfig;
    }

    public void setJedisPoolConfig(JedisPoolConfig jedisPoolConfig) {
        this.jedisPoolConfig = jedisPoolConfig;
    }
}
