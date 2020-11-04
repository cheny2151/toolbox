package cn.cheny.toolbox.redis.factory;

import cn.cheny.toolbox.redis.lock.autolease.AutoLeaseHolder;
import cn.cheny.toolbox.redis.lock.awaken.listener.SubLockManager;
import cn.cheny.toolbox.redis.lock.executor.RedisExecutor;

/**
 * 为RedisLockFactory提供缓存
 *
 * @author cheney
 * @date 2020-08-17
 */
public abstract class CacheManagerFactory implements RedisManagerFactory {

    /**
     * 锁订阅Manager
     */
    private volatile SubLockManager subLockManagerCache;

    /**
     * redis脚本执行器
     */
    private volatile RedisExecutor redisExecutorCache;

    @Override
    public SubLockManager getSubLockManager() {
        if (subLockManagerCache == null) {
            synchronized (this) {
                if (subLockManagerCache == null) {
                    subLockManagerCache = newSubLockManager();
                }
            }
        }
        return subLockManagerCache;
    }

    @Override
    public RedisExecutor getRedisExecutor() {
        if (redisExecutorCache == null) {
            synchronized (this) {
                if (redisExecutorCache == null) {
                    redisExecutorCache = newRedisExecutor();
                }
            }
        }
        return redisExecutorCache;
    }

    @Override
    public AutoLeaseHolder getAutoLeaseHolder() {
        return new AutoLeaseHolder(getRedisExecutor());
    }


    protected abstract SubLockManager newSubLockManager();

    protected abstract RedisExecutor newRedisExecutor();
}
