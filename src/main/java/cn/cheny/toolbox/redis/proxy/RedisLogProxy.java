package cn.cheny.toolbox.redis.proxy;

import cn.cheny.toolbox.redis.client.RedisApi;
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
    private RedisApi redisApi;

    public RedisLogProxy(RedisApi redisApi) {
        this.redisApi = redisApi;
    }

    public static RedisApi newProxyInstance(RedisApi redisApi) {
        RedisLogProxy redisLogProxy = new RedisLogProxy(redisApi);
        return (RedisApi) Proxy.newProxyInstance(RedisApi.class.getClassLoader(), new Class[]{RedisApi.class}, redisLogProxy);
    }

    @Override
    public Object invoke(Object o, Method method, Object[] objects) throws Throwable {
        log.info("jdk代理...");
        try {
            return method.invoke(redisApi, objects);
        } catch (Exception e) {
            log.error("jdk代理...:{}", e.getMessage());
            throw e;
        }
    }
}
