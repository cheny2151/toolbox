package cn.cheny.toolbox.redis.lock;

import java.util.concurrent.TimeUnit;

/**
 * redis锁接口
 *
 * @author cheney
 */
public interface RedisLock extends AutoCloseable {

    /**
     * 构建redis key，计算key的hash只会用到{}内的内容
     *
     * @param hashPath 作为hash的部分path
     * @param path     业务path
     * @return key
     */
    static String buildKey(String hashPath, String path) {
        return "{" + hashPath + "}:" + path;
    }

    /**
     * 锁资源的标识path
     */
    String getPath();

    /**
     * 尝试获取redis同步锁
     *
     * @param waitTime  最长等待时间
     * @param leaseTime 超时自动释放锁时间(小于等于0则永久)
     * @param timeUnit  时间单位
     * @return 是否成功获取锁
     */
    boolean tryLock(long waitTime, long leaseTime, TimeUnit timeUnit);

    /**
     * 尝试获取redis同步锁(无过期时间)
     *
     * @param waitTime 最长等待时间
     * @param timeUnit 时间单位
     * @return 是否成功获取锁
     */
    boolean tryLock(long waitTime, TimeUnit timeUnit);

    /**
     * 尝试释放锁
     */
    void unLock();

    /**
     * 锁path前标识,默认为空
     *
     * @return 锁path前标识
     */
    default String pathPreLabel() {
        return "LOCK";
    }

    /**
     * 重写close，去除throw
     */
    @Override
    void close();
}
