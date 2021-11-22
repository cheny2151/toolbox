package cn.cheny.toolbox.redis.lock.executor;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * redis命令执行器
 *
 * @author cheney
 * @date 2020-08-17
 */
public interface RedisExecutor {

    /**
     * 执行lua脚本并返回结果
     *
     * @param script 脚本内容
     * @param keys   key
     * @param args   arg
     * @return 脚本返回值
     */
    Object execute(String script, List<String> keys, List<String> args);

    /**
     * set
     *
     * @param key key
     * @param val val
     */
    void set(String key, String val);

    /**
     * get
     *
     * @param key key
     * @return val
     */
    String get(String key);

    /**
     * 删除key
     *
     * @param key key
     */
    void del(String key);

    /**
     * 查询key是否存在
     *
     * @param key key
     * @return 是否存在
     */
    boolean hasKey(String key);

    /**
     * 延长过期时间
     *
     * @param key      key
     * @param time     时长（毫秒）
     * @param timeUnit 单位
     */
    boolean expire(String key, long time, TimeUnit timeUnit);

    /**
     * 递增1
     *
     * @param key key
     * @return 递增后的值
     */
    Long incr(String key);

    /**
     * 递增n
     *
     * @param key key
     * @param val n
     * @return 递增后的值
     */
    Long incrBy(String key, long val);

    /**
     * 获取hash所有值
     *
     * @param key key
     * @return value
     */
    Map<String, String> hgetall(String key);

    /**
     * hget
     *
     * @param key  key
     * @param hkey hkey
     * @return hval
     */
    String hget(String key, String hkey);

    /**
     * hdel
     *
     * @param key  key
     * @param hkey hkey
     */
    void hdel(String key, String... hkey);

    /**
     * 设置多个hash值
     *
     * @param key key
     * @param map 集合
     */
    void hmset(String key, Map<String, String> map);

    /**
     * 设置hash值
     *
     * @param key key
     * @param hk  hkey
     * @param hv  hval
     */
    void hset(String key, String hk, String hv);

    /**
     * hash字段递增
     *
     * @param key key
     * @param hk  hkey
     * @param val 递增值
     * @return 递增后的值
     */
    Long hincrBy(String key, String hk, long val);

    /**
     * 发送广播信息
     *
     * @param channel 渠道
     * @param msg     信息
     */
    void publish(String channel, String msg);

}
