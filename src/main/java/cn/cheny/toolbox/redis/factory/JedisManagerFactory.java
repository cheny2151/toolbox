package cn.cheny.toolbox.redis.factory;

import cn.cheny.toolbox.redis.client.jedis.JedisClient;
import cn.cheny.toolbox.redis.lock.awaken.listener.JedisSubLockManager;
import cn.cheny.toolbox.redis.lock.awaken.listener.SubLockManager;
import cn.cheny.toolbox.redis.lock.executor.JedisExecutor;
import cn.cheny.toolbox.redis.lock.executor.RedisExecutor;

/**
 * jedis实现锁依赖工具的工厂
 *
 * @author cheney
 * @date 2020-10-26
 */
public class JedisManagerFactory extends CacheManagerFactory {

    private JedisClient jedisClient;

    public JedisManagerFactory(JedisClient jedisClient) {
        this.jedisClient = jedisClient;
    }

    @Override
    protected SubLockManager newSubLockManager() {
        JedisSubLockManager subLockManager = new JedisSubLockManager(jedisClient);
        subLockManager.init();
        return subLockManager;
    }

    @Override
    protected RedisExecutor newRedisExecutor() {
        return new JedisExecutor(jedisClient);
    }

}
