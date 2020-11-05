package cn.cheny.toolbox.redis.lock.awaken;

import cn.cheny.toolbox.redis.RedisConfiguration;
import cn.cheny.toolbox.redis.factory.RedisManagerFactory;
import cn.cheny.toolbox.redis.lock.RedisLockAdaptor;
import cn.cheny.toolbox.redis.lock.awaken.listener.LockListener;
import cn.cheny.toolbox.redis.lock.awaken.listener.SubLockManager;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static cn.cheny.toolbox.redis.lock.autolease.LeaseConstant.DURATION;
import static cn.cheny.toolbox.redis.lock.autolease.LeaseConstant.USE_LEASE_THRESHOLD;

/**
 * 发布订阅redis锁基础类
 * 利用redis的发布订阅，在加锁失败时订阅其他线程的redis解锁信息，然后阻塞线程，
 * 等到其他线程解锁时唤醒线程再循环获取该锁，直至获取到锁或者超时时退出
 * <p>
 * 此锁的lua脚本出现操作多个key（path,channel），必须有{}：
 * redis集群共有2^14个slot(槽点)，当key存在{}时，计算key的hash只会用到{}内的内容，
 * 相同的hash对2^14取模后值相同，就可以保证多个key落到同一个槽点，lua脚本执行才可以实现原子性操作
 *
 * @author cheney
 */
@Slf4j
public abstract class AwakenRedisLock extends RedisLockAdaptor {

    /**
     * 订阅锁状态manger
     */
    protected SubLockManager subLockManager;

    public AwakenRedisLock(String path) {
        super(path);
        RedisManagerFactory redisManagerFactory = RedisConfiguration.DEFAULT.getRedisManagerFactory();
        this.subLockManager = redisManagerFactory.getSubLockManager();
    }

    @Override
    public boolean tryLock(long waitTime, TimeUnit timeUnit) {
        return tryLock(waitTime, 0, timeUnit);
    }

    @Override
    public boolean tryLock(long waitTime, long leaseTime, TimeUnit timeUnit) {

        long maxTime = System.currentTimeMillis() + timeUnit.toMillis(waitTime);
        long firstLease = leaseTimeTemp = leaseTime = timeUnit.toMillis(leaseTime);

        boolean useLease = false;
        if (leaseTime <= 0 || leaseTime >= USE_LEASE_THRESHOLD) {
            useLease = true;
            firstLease = DURATION;
        }

        //try lock
        Object result;
        try {
            while ((result = LockScript(firstLease)) != null) {
                long timeout = maxTime - System.currentTimeMillis();
                if (timeout <= 0) {
                    //timeout return false
                    break;
                }
                CountDownLatch countDownLatch = new CountDownLatch(1);
                //add listener
                subLockManager.addMessageListener(
                        new LockListener(getChannelName(), countDownLatch::countDown)
                );
                countDownLatch.await(timeout, TimeUnit.MILLISECONDS);
            }
        } catch (Exception e) {
            log.error("try lock error", e);
            result = 0;
        }

        long currentTimeMillis = System.currentTimeMillis();
        boolean isLock = result == null;
        this.isLock.set(isLock);
        if (isLock && useLease) {
            // 执行续租逻辑
            addLockLease(currentTimeMillis, leaseTime);
        }

        if (!isLock) {
            log.info("Redis try lock fail,lock path:{}", getPath());
        }

        return isLock;
    }

    /**
     * 锁状态改变发布订阅的redis channel
     *
     * @return channel
     */
    protected abstract String getChannelName();
}
