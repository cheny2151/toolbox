package cn.cheny.toolbox.redis.lock.awaken;

import cn.cheny.toolbox.redis.lock.RedisLockAdaptor;

import java.util.concurrent.TimeUnit;

/**
 * @author cheney
 * @date 2020-05-19
 */
public class ReadWriteRedisLock extends RedisLockAdaptor {

    public ReadWriteRedisLock(String path) {
        super(path);
    }

    @Override
    protected Object LockScript(long leaseTime) {
        return null;
    }

    @Override
    protected Object unLockScript() {
        return null;
    }

    @Override
    public boolean tryLock(long waitTime, long leaseTime, TimeUnit timeUnit) {
        return false;
    }
}
