package cn.cheny.toolbox.redis.lock.autolease;

import static cn.cheny.toolbox.redis.lock.autolease.LeaseConstant.DURATION;
import static cn.cheny.toolbox.redis.lock.autolease.LeaseConstant.USE_LEASE_THRESHOLD;

/**
 * redis key续租实体
 *
 * @author cheney
 * @date 2020-11-03
 */
public class Lease {

    /**
     * 到期时间戳
     */
    private long expire;

    /**
     * 续租的redis key
     */
    private String key;

    /**
     * 是否已完成续租
     */
    private boolean finish;

    /**
     * 构造函数
     * 调用此构造函数时，redis key已经上锁并设置了一段过期时间
     * 入参currentTimeMillis，即为上锁的大致时间戳
     *
     * @param lockTime  首次加锁成功时间戳（非准确值）
     * @param leaseTime 持有时间
     * @param key       key
     */
    public Lease(long lockTime, long leaseTime, String key) {
        this.expire = lockTime + leaseTime;
        this.key = key;
    }

    /**
     * 获取下次续租时间
     *
     * @return 续租时间
     */
    public long getLeaseTime() {
        long currentTime = System.currentTimeMillis();
        long remainingExpire = expire - currentTime;
        if (remainingExpire <= 0) {
            // key已过期，强制返回0
            finish = true;
            return 0;
        }
        if (remainingExpire >= USE_LEASE_THRESHOLD) {
            // 大于阀值，则按默认值续期
            return DURATION;
        } else {
            // 小于阀值，则直接按剩余过期时间续期
            finish = true;
            return remainingExpire;
        }
    }

    public boolean hasNext() {
        return !finish;
    }

    public long getExpire() {
        return expire;
    }

    public String getKey() {
        return key;
    }

}
