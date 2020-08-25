package cn.cheny.toolbox.redis;

import cn.cheny.toolbox.redis.client.impl.JdkRedisClient;
import cn.cheny.toolbox.redis.client.impl.JsonRedisClient;
import cn.cheny.toolbox.redis.factory.SpringRedisLockFactory;
import cn.cheny.toolbox.spring.SpringToolAutoConfig;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * redis lock相关自动配置
 *
 * @author cheney
 * @date 2020-08-25
 */
@Configuration
@AutoConfigureAfter({SpringToolAutoConfig.class, RedisAutoConfiguration.class})
public class SpringRedisLockAutoConfig {

    @Bean(name = "toolbox:jdkRedisClient")
    public <V> JdkRedisClient<V> jdkRedisClient(RedisTemplate<String, V> redisTemplate) {
        return new JdkRedisClient<>(redisTemplate);
    }

    @Bean(name = "toolbox:jsonRedisClient")
    @ConditionalOnBean(name = "redisTemplate")
    public <V> JsonRedisClient<V> jsonRedisClient(RedisTemplate<String, V> redisTemplate) {
        return new JsonRedisClient<>(redisTemplate);
    }

    @Bean(name = "toolbox:redisConfiguration")
    @DependsOn("toolbox:springUtils")
    public RedisConfiguration redisConfiguration() {
        SpringRedisLockFactory springRedisLockFactory = new SpringRedisLockFactory();
        RedisConfiguration.DEFAULT.setRedisLockFactory(springRedisLockFactory);
        return RedisConfiguration.DEFAULT;
    }

}
