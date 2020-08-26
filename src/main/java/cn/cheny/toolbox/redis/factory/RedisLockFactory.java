package cn.cheny.toolbox.redis.factory;

import cn.cheny.toolbox.redis.lock.awaken.listener.SubLockManager;
import cn.cheny.toolbox.redis.lock.executor.RedisExecutor;

/**
 * redis lock依赖类工厂接口
 *
 * @author cheney
 * @date 2019/6/6
 */
public interface RedisLockFactory {

    SubLockManager getSubLockManager();

    RedisExecutor getRedisExecutor();
}
