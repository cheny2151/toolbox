package cn.cheny.toolbox.redis.factory;

import cn.cheny.toolbox.redis.lock.autolease.AutoLeaseHolder;
import cn.cheny.toolbox.redis.lock.awaken.listener.SubLockManager;
import cn.cheny.toolbox.redis.lock.executor.RedisExecutor;

/**
 * redis工具依赖类工厂接口
 *
 * @author cheney
 * @date 2019/6/6
 */
public interface RedisManagerFactory {

    SubLockManager getSubLockManager();

    RedisExecutor getRedisExecutor();

    AutoLeaseHolder getAutoLeaseHolder();
}
