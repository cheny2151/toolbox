package cn.cheny.toolbox.redis;

import cn.cheny.toolbox.redis.client.jedis.JedisClient;
import cn.cheny.toolbox.redis.clustertask.jedis.JedisClusterHelper;
import cn.cheny.toolbox.redis.clustertask.pub.ClusterTaskPublisher;
import cn.cheny.toolbox.redis.factory.JedisClientFactory;
import cn.cheny.toolbox.redis.factory.JedisManagerFactory;
import cn.cheny.toolbox.redis.lock.RedisLock;
import cn.cheny.toolbox.redis.lock.awaken.ReentrantRedisLock;
import org.junit.Test;

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
        JedisClientFactory factory = new JedisClientFactory("localhost", null, null, null);
        JedisClient jedisClient = factory.cacheClient();
        JedisManagerFactory jedisLockFactory = new JedisManagerFactory(jedisClient);
        RedisConfiguration.setDefaultRedisManagerFactory(jedisLockFactory);
        new Thread(()->{
            try (RedisLock redisLock = new ReentrantRedisLock("test")) {
                if (redisLock.tryLock(1000, 1, TimeUnit.SECONDS)) {
                    System.out.println("获取锁成功:A");
                    Thread.sleep(3000);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
       /* new Thread(()->{
            try (RedisLock redisLock = new ReentrantRedisLock("test")) {
                if (redisLock.tryLock(5000, 1000, TimeUnit.MILLISECONDS)) {
                    System.out.println("获取锁成功:B");
                    Thread.sleep(3000);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();*/
        Thread.sleep(10000);
    }

    /**
     * 依赖jedis的集群用例
     */
    @Test
    public void testForCluster() throws InterruptedException {
        JedisClientFactory factory = new JedisClientFactory("localhost", null, null, null);
        JedisClient jedisClient = factory.cacheClient();
        RedisConfiguration.setDefaultRedisManagerFactory(new JedisManagerFactory(jedisClient));
        ClusterTaskPublisher clusterTaskPublisher = JedisClusterHelper.initJedisCluster(factory);
        clusterTaskPublisher.publish("test", 100, 10, 1, true);
        Thread.sleep(10000);
    }

}
