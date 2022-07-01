package cn.cheny.toolbox.redis;

import cn.cheny.toolbox.exception.ToolboxRuntimeException;
import cn.cheny.toolbox.redis.exception.RedisRuntimeException;
import cn.cheny.toolbox.redis.factory.RedisManagerFactory;
import cn.cheny.toolbox.redis.lock.executor.RedisExecutor;
import org.apache.commons.lang3.StringUtils;

/**
 * Toolbox redis全局配置
 *
 * @author cheney
 * @date 2020-08-25
 */
public class RedisConfiguration {

    public static final RedisConfiguration DEFAULT = new RedisConfiguration();

    private RedisManagerFactory redisManagerFactory;

    private ToolboxRedisProperties toolboxRedisProperties;

    private RedisConfiguration() {
    }

    public RedisManagerFactory getRedisManagerFactory() {
        if (redisManagerFactory == null) {
            throw new RedisRuntimeException("Please check the RedisConfiguration.DEFAULT,it has not configured RedisLockFactory");
        }
        return redisManagerFactory;
    }

    public ToolboxRedisProperties getToolboxRedisProperties() {
        if (toolboxRedisProperties == null) {
            throw new RedisRuntimeException("Please check the RedisConfiguration.DEFAULT,it has not configured ToolboxRedisProperties");
        }
        return toolboxRedisProperties;
    }

    public RedisExecutor getRedisExecutor() {
        return getRedisManagerFactory().getRedisExecutor();
    }

    public void setRedisManagerFactory(RedisManagerFactory redisManagerFactory) {
        this.redisManagerFactory = redisManagerFactory;
    }

    public void setToolboxRedisProperties(ToolboxRedisProperties toolboxRedisProperties) {
        if (StringUtils.isEmpty(toolboxRedisProperties.getReentryLockPrePath()) ||
                StringUtils.isEmpty(toolboxRedisProperties.getReentryLockPrePath()) ||
                StringUtils.isEmpty(toolboxRedisProperties.getReentryLockPrePath())) {
            throw new ToolboxRuntimeException("Lock pre path can not be empty");
        }
        this.toolboxRedisProperties = toolboxRedisProperties;
    }

}
