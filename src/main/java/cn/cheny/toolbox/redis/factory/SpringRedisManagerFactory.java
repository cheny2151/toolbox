package cn.cheny.toolbox.redis.factory;

import cn.cheny.toolbox.redis.lock.awaken.listener.SpringSubLockManager;
import cn.cheny.toolbox.redis.lock.awaken.listener.SubLockManager;
import cn.cheny.toolbox.redis.lock.executor.RedisExecutor;
import cn.cheny.toolbox.redis.lock.executor.SpringRedisExecutor;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * spring整合redis锁的工厂
 *
 * @author cheney
 * @date 2020-08-17
 */
public class SpringRedisManagerFactory extends CacheManagerFactory {

    private final SpringSubLockManager springSubLockManager;

    private final RedisTemplate<String, String> redisTemplate;

    public SpringRedisManagerFactory(SpringSubLockManager springSubLockManager, RedisTemplate<String, String> redisTemplate) {
        this.springSubLockManager = springSubLockManager;
        this.redisTemplate = redisTemplate;
    }

    @Override
    protected SubLockManager newSubLockManager() {
        return springSubLockManager;
    }

    @Override
    protected RedisExecutor newRedisExecutor() {
        return new SpringRedisExecutor(redisTemplate);
    }
}
