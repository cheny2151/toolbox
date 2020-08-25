package cn.cheny.toolbox.redis.factory;

import cn.cheny.toolbox.redis.lock.awaken.listener.SubLockManager;
import cn.cheny.toolbox.redis.lock.executor.RedisExecutor;

/**
 * 默认redis lock依赖类工厂
 *
 * @author cheney
 * @date 2020-08-25
 */
public class DefaultRedisLockFactory implements RedisLockFactory {

    /**
     * 默认实现类
     */
    private RedisLockFactory defaultInstance = new SpringRedisLockFactory();

    @Override
    public SubLockManager getSubLockManager() {
        return defaultInstance.getSubLockManager();
    }

    @Override
    public RedisExecutor getRedisExecutor() {
        return defaultInstance.getRedisExecutor();
    }

    public RedisLockFactory getDefaultInstance() {
        return defaultInstance;
    }

    public void setDefaultInstance(RedisLockFactory defaultInstance) {
        if (defaultInstance == null) {
            throw new IllegalArgumentException("redisLockFactory can not be null");
        }
        this.defaultInstance = defaultInstance;
    }
}
