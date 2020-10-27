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
     * 删除key
     *
     * @param key key
     */
    void del(String key);

    /**
     * 获取hash所有值
     *
     * @param key key
     * @return value
     */
    Map<String, String> hgetall(String key);

    /**
     * 延长过期时间
     *
     * @param key      key
     * @param time     时长（毫秒）
     * @param timeUnit 单位
     */
    void expire(String key, long time, TimeUnit timeUnit);

    /**
     * 查询key是否存在
     *
     * @param key key
     * @return 是否存在
     */
    boolean hasKey(String key);

    /**
     * 设置hash值
     *
     * @param key key
     * @param map 集合
     */
    void hset(String key, Map<String, String> map);

    /**
     * 发送广播信息
     *
     * @param channel 渠道
     * @param msg     信息
     */
    void publish(String channel, String msg);
}
