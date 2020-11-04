package cn.cheny.toolbox.redis.lock.autolease;

import static cn.cheny.toolbox.redis.lock.autolease.LeaseConstant.DURATION;

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
     * 剩余的续租次数
     */
    private int remainingLeaseTimes;

    /**
     * 零碎续租时间（用于记录取模结果,倍数时为0）
     */
    private long fragmentaryTime;

    /**
     * 构造函数
     * 调用此构造函数时，redis key已经上锁并设置了一段过期时间
     * 入参currentTimeMillis，即为上锁的大致时间戳
     *
     * @param currentTimeMillis 首次加锁成功时间戳（非准确值）
     * @param leaseTime         持有时间
     * @param key               key
     */
    public Lease(long currentTimeMillis, long leaseTime, String key) {
        this.expire = currentTimeMillis + leaseTime;
        this.key = key;
        // -1:减去首次加锁
        this.remainingLeaseTimes = (int) (leaseTime / DURATION) - 1;
        this.fragmentaryTime = (int) (leaseTime % DURATION);
    }

    /**
     * 获取下次续租时间
     *
     * @return 续租时间
     */
    public long getLeaseTime() {
        if (System.currentTimeMillis() > expire) {
            // key已过期，强制返回0
            remainingLeaseTimes = 0;
            fragmentaryTime = 0;
            return 0;
        }
        int nextTime = --remainingLeaseTimes;
        if (nextTime > 0) {
            return DURATION;
        } else if (nextTime == 0) {
            // 零碎续租时间直接补充至结尾的续期
            long fragmentaryTime = this.fragmentaryTime;
            if (fragmentaryTime > 0) {
                this.fragmentaryTime = 0;
                return DURATION + fragmentaryTime;
            }
            return DURATION;
        } else {
            return 0;
        }
    }

    public boolean hasNext() {
        return remainingLeaseTimes > 0 || fragmentaryTime > 0;
    }

    public long getExpire() {
        return expire;
    }

    public String getKey() {
        return key;
    }

    public int getRemainingLeaseTimes() {
        return remainingLeaseTimes;
    }

    public long getFragmentaryTime() {
        return fragmentaryTime;
    }
}
