package cn.cheny.toolbox.redis;

import cn.cheny.toolbox.redis.exception.RedisRuntimeException;
import cn.cheny.toolbox.redis.factory.RedisLockFactory;

/**
 * redis全局配置
 *
 * @author cheney
 * @date 2020-08-25
 */
public class RedisConfiguration {

    public static final RedisConfiguration DEFAULT = new RedisConfiguration();

    private RedisLockFactory redisLockFactory;

    private RedisConfiguration() {
    }

    public RedisLockFactory getRedisLockFactory() {
        if (redisLockFactory == null) {
            throw new RedisRuntimeException("please check RedisConfiguration.DEFAULT,it has not config RedisLockFactory");
        }
        return redisLockFactory;
    }

    public void setRedisLockFactory(RedisLockFactory redisLockFactory) {
        this.redisLockFactory = redisLockFactory;
    }
}
