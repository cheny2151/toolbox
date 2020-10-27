package cn.cheny.toolbox.mybatis.cache;

import cn.cheny.toolbox.redis.client.impl.JsonRedisClient;
import cn.cheny.toolbox.spring.SpringUtils;
import org.apache.ibatis.cache.Cache;

/**
 * mybatis缓存拓展--redis实现
 *
 * @author cheney
 * @date 2020-10-14
 */
public class RedisMybatisCache implements Cache {

    private final static String KEY_PRE = "MYBATIS_CACHE:";

    private final String id;

    public RedisMybatisCache(String id) {
        this.id = KEY_PRE + id;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void putObject(Object key, Object o) {
        JsonRedisClient<Object> redisClient = getRedisClient();
        redisClient.hSet(id, key.toString(), o);
    }

    @Override
    public Object getObject(Object key) {
        JsonRedisClient<Object> redisClient = getRedisClient();
        return redisClient.hGet(id, key.toString());
    }

    @Override
    public Object removeObject(Object key) {
        JsonRedisClient<Object> redisClient = getRedisClient();
        redisClient.hDel(id, key.toString());
        return null;
    }

    @Override
    public void clear() {
        JsonRedisClient<Object> redisClient = getRedisClient();
        redisClient.delete(id);
    }

    @Override
    public int getSize() {
        JsonRedisClient<Object> redisClient = getRedisClient();
        return (int) redisClient.hLen(id);
    }

    @SuppressWarnings("unchecked")
    private JsonRedisClient<Object> getRedisClient() {
        return SpringUtils.getBean("jsonRedisClient", JsonRedisClient.class);
    }

}
