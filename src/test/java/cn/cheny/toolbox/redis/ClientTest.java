package cn.cheny.toolbox.redis;

import cn.cheny.toolbox.redis.client.RedisClient;
import cn.cheny.toolbox.redis.clustertask.jedis.JedisClusterHelper;
import cn.cheny.toolbox.redis.clustertask.pub.ClusterTaskPublisher;
import cn.cheny.toolbox.redis.factory.JedisClientFactory;
import cn.cheny.toolbox.redis.factory.JedisManagerFactory;
import cn.cheny.toolbox.redis.lock.RedisLock;
import cn.cheny.toolbox.redis.lock.awaken.ReentrantRedisLock;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * @author cheney
 * @date 2020-10-26
 */
public class ClientTest {

    /**
     * 依赖jedis的锁用例
     */
    @Test
    public void testForRedisKey() throws InterruptedException {
        JedisClientFactory factory = new JedisClientFactory("localhost");
        RedisClient<String> jedisClient = factory.cacheClient();
        JedisManagerFactory jedisLockFactory = new JedisManagerFactory(jedisClient);
        RedisConfiguration.setDefaultRedisManagerFactory(jedisLockFactory);
        new Thread(() -> {
            try (RedisLock redisLock = new ReentrantRedisLock("test")) {
                if (redisLock.tryLock(30000, 1000 * 60 * 3, TimeUnit.MILLISECONDS)) {
                    System.out.println("获取锁成功:A");
                    Thread.sleep(1000 * 60 * 3);
                    System.out.println("执行任务完毕");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
        new Thread(() -> {
            try (RedisLock redisLock = new ReentrantRedisLock("test2")) {
                if (redisLock.tryLock(30000, 12000, TimeUnit.MILLISECONDS)) {
                    System.out.println("获取锁成功:B");
                    Thread.sleep(10000);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
        Thread.sleep(10000000);
    }

    @Test
    public void testForConcurrent() throws InterruptedException {
        JedisClientFactory factory = new JedisClientFactory("localhost");
        RedisClient<String> jedisClient = factory.cacheClient();
        JedisManagerFactory jedisLockFactory = new JedisManagerFactory(jedisClient);
        RedisConfiguration.setDefaultRedisManagerFactory(jedisLockFactory);
        int[] num = new int[]{0};
        ExecutorService executorService = Executors.newFixedThreadPool(16);
        List<Callable<Integer>> runnables = new ArrayList<>();
        ReentrantRedisLock lock = new ReentrantRedisLock("Test");
        for (int i = 0; i < 100000; i++) {
            Callable<Integer> test = () -> {
                try {
                    if (lock.tryLock(2000, TimeUnit.MILLISECONDS)) {
                        num[0]++;
                    }
                } finally {
                    lock.unLock();
                }
                return num[0];
            };
            runnables.add(test);
        }
        executorService.invokeAll(runnables);
        System.out.println(num[0]);
        Thread.sleep(10000);
    }

    /**
     * 依赖jedis的集群用例
     */
    @Test
    public void testForCluster() throws InterruptedException {
        JedisClientFactory factory = new JedisClientFactory("localhost", null, null, null);
        RedisClient<String> jedisClient = factory.cacheClient();
        RedisConfiguration.setDefaultRedisManagerFactory(new JedisManagerFactory(jedisClient));
        ClusterTaskPublisher clusterTaskPublisher = JedisClusterHelper.initJedisCluster(factory);
        clusterTaskPublisher.publish("test", 100, 10, 1, true);
        Thread.sleep(10000);
    }

}
