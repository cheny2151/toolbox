package cn.cheny.toolbox.redis.factory;

import cn.cheny.toolbox.redis.client.RedisClient;

/**
 * redis client创建工厂接口
 *
 * @author cheney
 * @date 2020-11-06
 */
public interface RedisClientFactory<T> {

    /**
     * 创建新实例
     *
     * @return redis client实例
     */
    RedisClient<T> newJedisClient();

    /**
     * 创建并缓存唯一实例
     *
     * @return redis client实例，存在缓存则直接返回
     */
    RedisClient<T> cacheClient();

    /**
     * 清除缓存
     */
    void clearCache();

}
