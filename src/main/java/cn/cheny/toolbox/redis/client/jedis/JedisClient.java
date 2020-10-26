package cn.cheny.toolbox.redis.client.jedis;

import redis.clients.jedis.*;

import java.util.HashSet;
import java.util.List;

/**
 * jedis客户端单节点/多节点封装
 *
 * @author cheney
 * @date 2020-10-26
 */
public class JedisClient {

    private final String nodes;

    private final Integer timeout;

    private final Integer maxAttempts;

    private JedisPool jedisPool;

    private JedisCluster jedisCluster;

    private boolean isCluster;

    private JedisPoolConfig jedisPoolConfig;

    public JedisClient(String nodes) {
        this(nodes, null, null, null);
    }

    public JedisClient(String nodes, Integer timeout, Integer maxAttempts, JedisPoolConfig jedisPoolConfig) {
        this.nodes = nodes;
        this.timeout = timeout;
        this.maxAttempts = maxAttempts;
        if (jedisPoolConfig == null) {
            jedisPoolConfig = new JedisPoolConfig();
        }
        this.jedisPoolConfig = jedisPoolConfig;
        initClient();
    }

    /**
     * 初始化
     * 单点->JedisPool
     * 集群->JedisCluster
     */
    private void initClient() {
        if (nodes.contains(",")) {
            String[] nodeArray = nodes.split(",");
            HashSet<HostAndPort> nodeAndPorts = new HashSet<>(nodeArray.length);
            for (String nodeAndPort : nodeArray) {
                String[] split = nodeAndPort.split(":");
                nodeAndPorts.add(new HostAndPort(split[0], Integer.parseInt(split[1])));
            }
            if (timeout != null) {
                if (maxAttempts != null) {
                    jedisCluster = new JedisCluster(nodeAndPorts, timeout, maxAttempts, jedisPoolConfig);
                } else {
                    jedisCluster = new JedisCluster(nodeAndPorts, timeout, jedisPoolConfig);
                }
            } else {
                jedisCluster = new JedisCluster(nodeAndPorts, jedisPoolConfig);
            }
            isCluster = true;
        } else {
            if (nodes.contains(":")) {
                String[] split = nodes.split(":");
                jedisPool = new JedisPool(jedisPoolConfig, split[0], Integer.parseInt(split[1]));
            } else {
                jedisPool = new JedisPool(jedisPoolConfig, nodes);
            }
            isCluster = false;
        }
    }

    public Object eval(String script, List<String> finalKeys, List<String> finalArgs) {
        Object result;
        if (isCluster) {
            // 集群模式
            result = jedisCluster.eval(script, finalKeys, finalArgs);
        } else {
            // 单机模式
            Jedis jedis = jedisPool.getResource();
            result = jedis.eval(script, finalKeys, finalArgs);
        }
        return result;
    }

    public void psubscribe(JedisPubSub jedisPubSub,String channel) {
        if (isCluster) {
            jedisCluster.psubscribe(jedisPubSub, channel);
        } else {
            jedisPool.getResource().psubscribe(jedisPubSub, channel);
        }
    }

    public String getNodes() {
        return nodes;
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

    public JedisCluster getJedisCluster() {
        return jedisCluster;
    }

    public boolean isCluster() {
        return isCluster;
    }

    public JedisPoolConfig getJedisPoolConfig() {
        return jedisPoolConfig;
    }
}
