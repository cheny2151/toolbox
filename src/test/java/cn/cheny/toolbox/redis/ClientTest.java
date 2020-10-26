package cn.cheny.toolbox.redis;

import cn.cheny.toolbox.redis.client.jedis.JedisClient;
import cn.cheny.toolbox.redis.factory.JedisClientFactory;
import cn.cheny.toolbox.redis.factory.JedisLockFactory;
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
    public void test() {
        JedisClientFactory factory = new JedisClientFactory("localhost", null, null, null);
        JedisClient jedisClient = factory.cacheClient();
        JedisLockFactory jedisLockFactory = new JedisLockFactory(jedisClient);
        RedisConfiguration.DEFAULT.setRedisLockFactory(jedisLockFactory);
        try (RedisLock redisLock = new ReentrantRedisLock("test")) {
            if (redisLock.tryLock(1000, 1000, TimeUnit.MILLISECONDS)) {
                        System.out.println("获取锁成功");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
