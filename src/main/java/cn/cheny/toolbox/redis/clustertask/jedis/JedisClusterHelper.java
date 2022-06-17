package cn.cheny.toolbox.redis.clustertask.jedis;

import cn.cheny.toolbox.redis.RedisConfiguration;
import cn.cheny.toolbox.redis.client.RedisClient;
import cn.cheny.toolbox.redis.clustertask.pub.ClusterTaskPublisher;
import cn.cheny.toolbox.redis.clustertask.pub.DefaultClusterTaskPublisher;
import cn.cheny.toolbox.redis.clustertask.sub.*;
import cn.cheny.toolbox.redis.factory.JedisManagerFactory;
import cn.cheny.toolbox.redis.factory.RedisClientFactory;
import cn.cheny.toolbox.redis.factory.RedisManagerFactory;
import cn.cheny.toolbox.redis.lock.executor.RedisExecutor;
import cn.cheny.toolbox.scan.PathImplementationClassBuilder;
import cn.cheny.toolbox.scan.ScanException;
import lombok.extern.slf4j.Slf4j;
import redis.clients.jedis.JedisPubSub;

import java.util.Collection;
import java.util.concurrent.Executors;

import static cn.cheny.toolbox.redis.clustertask.pub.ClusterTaskPublisher.CLUSTER_TASK_CHANNEL_PRE_KEY;

/**
 * 帮助创建jedis实现集群发布订阅者
 *
 * @author cheney
 * @date 2020-10-27
 */
@Slf4j
public class JedisClusterHelper {

    /**
     * 集群任务线程池线程数
     */
    private static int nThreads = 30;

    /**
     * 初始化一套发布订阅
     *
     * @param jedisClientFactory jedis客户端工厂
     * @return 集群任务发布者
     */
    public static ClusterTaskPublisher initJedisCluster(RedisClientFactory<String> jedisClientFactory) {
        RedisManagerFactory redisManagerFactory = RedisConfiguration.DEFAULT.getRedisManagerFactory();
        if (!(redisManagerFactory instanceof JedisManagerFactory)) {
            throw new ClassCastException("RedisConfiguration.DEFAULT redisManagerFactory instance is not JedisManagerFactory");
        }
        RedisExecutor redisExecutor = redisManagerFactory.getRedisExecutor();
        DefaultClusterTaskPublisher publisher = new DefaultClusterTaskPublisher(redisExecutor);
        ClusterTaskDealer clusterTaskDealer = new ClusterTaskDealer(redisExecutor, Executors.newFixedThreadPool(nThreads));
        Collection<ClusterTaskSubscriber> subscriberInstances = createSubscriberInstances();
        JedisClusterTaskSubscriberHolder subscriberHolder = new JedisClusterTaskSubscriberHolder(clusterTaskDealer, subscriberInstances);
        JedisClusterTaskRedisSub redisSub = new JedisClusterTaskRedisSub(subscriberHolder);
        RedisClient<String> jedisClient = jedisClientFactory.cacheClient();
        startSubClusterTask(jedisClient, redisSub);
        return publisher;
    }

    /**
     * 扫描ClusterTaskSubscriber实现类
     *
     * @return ClusterTaskSubscriber实现类集合
     */
    public static Collection<ClusterTaskSubscriber> createSubscriberInstances() {
        try {
            return PathImplementationClassBuilder.createInstances(ClusterTaskSubscriber.class, SubTask.class);
        } catch (ScanException e) {
            log.error("集群订阅类扫描异常", e);
            throw new RuntimeException(e);
        }
    }

    public static int getThreads() {
        return nThreads;
    }

    public static void setThreads(int nThreads) {
        JedisClusterHelper.nThreads = nThreads;
    }

    /**
     * 开启订阅集群任务线程
     *
     * @param jedisClient jedis客户端
     * @param jedisPubSub 订阅实例
     */
    private static void startSubClusterTask(RedisClient<String> jedisClient, JedisPubSub jedisPubSub) {
        // 开启订阅线程
        new Thread(() -> {
            try {
                String channel = CLUSTER_TASK_CHANNEL_PRE_KEY + "*";
                jedisClient.psubscribe(jedisPubSub, channel);
            } catch (Exception e) {
                log.error("订阅线程异常,执行重新订阅", e);
                startSubClusterTask(jedisClient, jedisPubSub);
            }
        }).start();
    }

}
