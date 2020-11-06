package cn.cheny.toolbox.redis.factory;

import cn.cheny.toolbox.redis.client.RedisClient;
import cn.cheny.toolbox.redis.client.jedis.JedisClient;
import cn.cheny.toolbox.redis.client.jedis.JedisClusterClient;
import cn.cheny.toolbox.redis.exception.RedisRuntimeException;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisPoolConfig;

import java.util.HashSet;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * jedis client工厂
 *
 * @author cheney
 * @date 2020-10-26
 */
public class JedisClientFactory implements RedisClientFactory<String> {

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

    /**
     * 实例缓存
     */
    private volatile RedisClient<String> instance;

    /**
     * 是否已存在实例缓存
     */
    private final AtomicBoolean cached = new AtomicBoolean(false);

    /**
     * redis节点信息
     */
    private String nodes;

    /**
     * 连接超时时间
     */
    private Integer timeout;

    /**
     * 最大尝试次数
     */
    private Integer maxAttempts;

    /**
     * 密码
     */
    private String password;

    /**
     * 连接池配置
     */
    private JedisPoolConfig jedisPoolConfig;

    public JedisClientFactory(String nodes) {
        this(nodes, null, null, null, null);
    }

    public JedisClientFactory(String nodes, String password) {
        this(nodes, null, null, password, null);

    }

    public JedisClientFactory(String nodes, Integer timeout, Integer maxAttempts, JedisPoolConfig jedisPoolConfig) {
        this(nodes, timeout, maxAttempts, null, jedisPoolConfig);
    }

    public JedisClientFactory(String nodes, Integer timeout, Integer maxAttempts, String password, JedisPoolConfig jedisPoolConfig) {
        if (nodes == null) {
            throw new RedisRuntimeException("nodes can not be null");
        }
        this.nodes = nodes;
        this.timeout = timeout == null ? DEFAULT_WAIT_TIMEOUT : timeout;
        this.maxAttempts = maxAttempts == null ? DEFAULT_MAX_ATTEMPTS : maxAttempts;
        this.password = password;
        if (jedisPoolConfig == null) {
            jedisPoolConfig = new JedisPoolConfig();
            jedisPoolConfig.setMaxIdle(DEFAULT_MAX_IDLE);
            jedisPoolConfig.setMaxTotal(DEFAULT_MAX_TOTAL);
            jedisPoolConfig.setMaxWaitMillis(DEFAULT_WAIT_MILLIS);
        }
        this.jedisPoolConfig = jedisPoolConfig;
    }

    @Override
    public RedisClient<String> newJedisClient() {
        if (nodes.contains(",")) {
            // 集群
            String[] nodeArray = nodes.split(",");
            HashSet<HostAndPort> hostAndPorts = new HashSet<>(nodeArray.length);
            for (String nodeAndPort : nodeArray) {
                String[] split = nodeAndPort.split(":");
                hostAndPorts.add(new HostAndPort(split[0], Integer.parseInt(split[1])));
            }
            return new JedisClusterClient(hostAndPorts, timeout, maxAttempts, password, jedisPoolConfig);
        } else {
            // 单机
            String host = nodes;
            int port = DEFAULT_WAIT_PORT;
            if (nodes.contains(":")) {
                String[] split = nodes.split(":");
                port = Integer.parseInt(split[1]);
                host = split[0];
            }
            return new JedisClient(host, port, timeout, maxAttempts, password, jedisPoolConfig);
        }
    }

    @Override
    public RedisClient<String> cacheClient() {
        for (; ; ) {
            if (instance != null) {
                return instance;
            } else if (cached.compareAndSet(false, true)) {
                instance = newJedisClient();
                return instance;
            }
        }
    }

    @Override
    public void clearCache() {
        if (cached.compareAndSet(true, false)) {
            instance = null;
        }
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
