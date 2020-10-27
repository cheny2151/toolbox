package cn.cheny.toolbox.redis;

import cn.cheny.toolbox.redis.exception.RedisRuntimeException;
import cn.cheny.toolbox.redis.factory.RedisManagerFactory;

/**
 * redis全局配置
 *
 * @author cheney
 * @date 2020-08-25
 */
public class RedisConfiguration {

    public static final RedisConfiguration DEFAULT = new RedisConfiguration();

    private RedisManagerFactory redisManagerFactory;

    private RedisConfiguration() {
    }

    public RedisManagerFactory getRedisManagerFactory() {
        if (redisManagerFactory == null) {
            throw new RedisRuntimeException("please check RedisConfiguration.DEFAULT,it has not config RedisLockFactory");
        }
        return redisManagerFactory;
    }

    public void setRedisManagerFactory(RedisManagerFactory redisManagerFactory) {
        this.redisManagerFactory = redisManagerFactory;
    }
}
