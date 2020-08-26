package cn.cheny.toolbox.redis.proxy;

import cn.cheny.toolbox.redis.client.RedisClient;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * redis日志代理类
 *
 * @author cheney
 * @date 2019-08-24
 */
@Slf4j
public class RedisLogProxy implements InvocationHandler {

    /**
     * jdk代理的对象必须存在接口
     */
    private RedisClient redisClient;

    public RedisLogProxy(RedisClient redisClient) {
        this.redisClient = redisClient;
    }

    public static RedisClient newProxyInstance(RedisClient redisClient) {
        RedisLogProxy redisLogProxy = new RedisLogProxy(redisClient);
        return (RedisClient) Proxy.newProxyInstance(RedisClient.class.getClassLoader(), new Class[]{RedisClient.class}, redisLogProxy);
    }

    @Override
    public Object invoke(Object o, Method method, Object[] objects) throws Throwable {
        log.info("jdk代理...");
        try {
            return method.invoke(redisClient, objects);
        } catch (Exception e) {
            log.error("jdk代理...:{}", e.getMessage());
            throw e;
        }
    }
}
