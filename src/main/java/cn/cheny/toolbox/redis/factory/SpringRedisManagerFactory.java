package cn.cheny.toolbox.redis.factory;

import cn.cheny.toolbox.redis.exception.RedisRuntimeException;
import cn.cheny.toolbox.redis.lock.awaken.listener.SpringSubLockManager;
import cn.cheny.toolbox.redis.lock.awaken.listener.SubLockManager;
import cn.cheny.toolbox.redis.lock.executor.RedisExecutor;
import cn.cheny.toolbox.redis.lock.executor.SpringRedisExecutor;
import cn.cheny.toolbox.spring.SpringUtils;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.Collection;

/**
 * spring整合redis锁的工厂
 *
 * @author cheney
 * @date 2020-08-17
 */
public class SpringRedisManagerFactory extends CacheManagerFactory {

    @Override
    protected SubLockManager newSubLockManager() {
        SubLockManager subLockManager = null;
        try {
            subLockManager = SpringUtils.getBean("springSubLockManager", SubLockManager.class);
        } catch (Exception e) {
            // try next
        }
        if (subLockManager == null) {
            Collection<SubLockManager> lockManagers = SpringUtils.getBeansOfType(SubLockManager.class);
            subLockManager = lockManagers.stream()
                    .filter(instance -> SpringSubLockManager.class.isAssignableFrom(instance.getClass()))
                    .findFirst()
                    .orElseThrow(() -> new RedisRuntimeException("can not find any bean of SubLockManager"));
        }
        return subLockManager;
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    protected RedisExecutor newRedisExecutor() {
        RedisTemplate<String,String> redisTemplate = null;
        try {
            redisTemplate = SpringUtils.getBean("stringRedisTemplate", RedisTemplate.class);
        } catch (Exception e) {
            // try next
        }
        if (redisTemplate == null) {
            Collection<RedisTemplate> redisTemplates = SpringUtils.getBeansOfType(RedisTemplate.class);
            redisTemplate = redisTemplates.stream().findFirst()
                    .orElseThrow(() -> new RedisRuntimeException("can not find any bean of RedisTemplate"));
        }
        return new SpringRedisExecutor(redisTemplate);
    }
}
