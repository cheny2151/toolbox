package cn.cheny.toolbox.redis.annotation;

import org.springframework.context.annotation.DependsOn;

import java.lang.annotation.*;

/**
 * redis lock依赖
 *
 * @author by chenyi
 * @date 2021/7/20
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@DependsOn("toolbox:redisConfiguration")
public @interface DependRedisLock {
}
