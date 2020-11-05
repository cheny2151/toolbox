package cn.cheny.toolbox.redis.client.jedis;

import redis.clients.jedis.*;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * jedis客户端单节点/多节点封装
 *
 * @author cheney
 * @date 2020-10-26
 */
public class JedisClient {

    /**
     * 默认jedis线程池最大数
     */
    private static final int DEFAULT_MAX_TOTAL = 16;

    /**
     * 默认jedis线程池最大空闲数
     */
    private static final int DEFAULT_MAX_IDLE = 16;

    /**
     * 默认异常最大重试次数
     */
    private static final int DEFAULT_MAX_ATTEMPTS = 5;

    /**
     * 默认等待最长毫秒值
     */
    private static final int DEFAULT_WAIT_MILLIS = 30000;

    /**
     * 默认超时时间
     */
    private static final int DEFAULT_WAIT_TIMEOUT = 2000;

    /**
     * 默认服务器端口
     */
    private static final int DEFAULT_WAIT_PORT = 6379;

    private final String nodes;

    private final Integer timeout;

    private final Integer maxAttempts;

    private JedisPool jedisPool;

    private JedisCluster jedisCluster;

    private boolean isCluster;

    private JedisPoolConfig jedisPoolConfig;

    public JedisClient(String nodes, Integer timeout, Integer maxAttempts, String password, JedisPoolConfig jedisPoolConfig) {
        this.nodes = nodes;
        this.timeout = timeout == null ? DEFAULT_WAIT_TIMEOUT : timeout;
        this.maxAttempts = maxAttempts == null ? DEFAULT_MAX_ATTEMPTS : maxAttempts;
        if (jedisPoolConfig == null) {
            jedisPoolConfig = new JedisPoolConfig();
            jedisPoolConfig.setMaxIdle(DEFAULT_MAX_IDLE);
            jedisPoolConfig.setMaxTotal(DEFAULT_MAX_TOTAL);
            jedisPoolConfig.setMaxWaitMillis(DEFAULT_WAIT_MILLIS);
        }
        this.jedisPoolConfig = jedisPoolConfig;
        initClient(password);
    }

    /**
     * 初始化
     * 单点->JedisPool
     * 集群->JedisCluster
     */
    private void initClient(String password) {
        if (nodes.contains(",")) {
            String[] nodeArray = nodes.split(",");
            HashSet<HostAndPort> nodeAndPorts = new HashSet<>(nodeArray.length);
            for (String nodeAndPort : nodeArray) {
                String[] split = nodeAndPort.split(":");
                nodeAndPorts.add(new HostAndPort(split[0], Integer.parseInt(split[1])));
            }
            jedisCluster = new JedisCluster(nodeAndPorts, timeout, timeout, maxAttempts, password, jedisPoolConfig);
            isCluster = true;
        } else {
            String host = nodes;
            int port = DEFAULT_WAIT_PORT;
            if (nodes.contains(":")) {
                String[] split = nodes.split(":");
                port = Integer.parseInt(split[1]);
                host = split[0];
            }
            jedisPool = new JedisPool(jedisPoolConfig, host, port, timeout, password);
            isCluster = false;
        }
    }

    public Boolean hasKey(String key) {
        if (isCluster) {
            return jedisCluster.exists(key);
        } else {
            return execAndClose(jedis -> jedis.exists(key));
        }
    }

    public void del(String... key) {
        if (isCluster) {
            jedisCluster.del(key);
        } else {
            execAndClose(jedis -> jedis.del(key));
        }
    }

    public String get(String key) {
        String result;
        if (isCluster) {
            result = jedisCluster.get(key);
        } else {
            result = execAndClose(jedis -> jedis.get(key));
        }
        return result;
    }

    public void hset(String key, String hkey, String hval) {
        if (isCluster) {
            jedisCluster.hset(key, hkey, hval);
        } else {
            execAndClose(jedis -> jedis.hset(key, hkey, hval));
        }
    }

    public void hset(String key, Map<String, String> map) {
        if (isCluster) {
            jedisCluster.hset(key, map);
        } else {
            execAndClose(jedis -> jedis.hset(key, map));
        }
    }

    public String hget(String key, String hkey) {
        String result;
        if (isCluster) {
            result = jedisCluster.hget(key, hkey);
        } else {
            result = execAndClose(jedis -> jedis.hget(key, hkey));
        }
        return result;
    }

    public Map<String, String> hgetall(String key) {
        Map<String, String> map;
        if (isCluster) {
            map = jedisCluster.hgetAll(key);
        } else {
            map = execAndClose(jedis -> jedis.hgetAll(key));
        }
        return map;
    }

    public void expire(String key, long time, TimeUnit timeUnit) {
        if (isCluster) {
            jedisCluster.pexpire(key, timeUnit.toMillis(time));
        } else {
            execAndClose(jedis -> jedis.pexpire(key, timeUnit.toMillis(time)));
        }
    }

    public Object eval(String script, List<String> finalKeys, List<String> finalArgs) {
        Object result;
        if (isCluster) {
            result = jedisCluster.eval(script, finalKeys, finalArgs);
        } else {
            result = execAndClose(jedis -> jedis.eval(script, finalKeys, finalArgs));
        }
        return result;
    }

    public void publish(String channel, String msg) {
        if (isCluster) {
            jedisCluster.publish(channel, msg);
        } else {
            execAndClose(jedis -> jedis.publish(channel, msg));
        }
    }

    public void psubscribe(JedisPubSub jedisPubSub, String channel) {
        if (isCluster) {
            jedisCluster.psubscribe(jedisPubSub, channel);
        } else {
            execAndClose(jedis -> {
                jedis.psubscribe(jedisPubSub, channel);
                return null;
            });
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
