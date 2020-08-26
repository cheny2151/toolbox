package cn.cheny.toolbox.redis.rateLimit;

import cn.cheny.toolbox.redis.RedisConfiguration;
import cn.cheny.toolbox.redis.lock.executor.RedisExecutor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

import static cn.cheny.toolbox.redis.rateLimit.RateLimitScript.KEY_PRE_PATH;

/**
 * @author cheney
 * @date 2020-02-27
 */
@Slf4j
public class RateLimiter {

    /**
     * 限流路径
     */
    private String path;

    /**
     * 最大令牌数
     */
    private int maxPermits;

    /**
     * qps
     */
    private int rate;

    /**
     * redis命令执行器
     */
    private RedisExecutor redisExecutor;

    public RateLimiter(String path, int maxPermits, int rate) {
        this(path, maxPermits, rate, RedisConfiguration.DEFAULT.getRedisLockFactory().getRedisExecutor());
    }

    public RateLimiter(String path, int maxPermits, int rate, RedisExecutor redisExecutor) {
        if (StringUtils.isEmpty(path)) {
            throw new IllegalArgumentException();
        }
        this.path = path;
        this.maxPermits = maxPermits;
        this.rate = rate;
        this.redisExecutor = redisExecutor;
        this.init();
    }

    /**
     * 初始化
     * 执行令牌桶初始化脚本
     */
    private void init() {
        List<String> args = new ArrayList<>();
        args.add(String.valueOf(maxPermits));
        args.add(String.valueOf(rate));
        args.add(String.valueOf(maxPermits));
        redisExecutor.execute(RateLimitScript.INIT,
                Collections.singletonList(getRealPath()), args);
    }

    /**
     * 获取1个单位令牌
     *
     * @return 是否获取成功
     */
    public boolean acquire() {
        return acquire(1);
    }

    /**
     * 获取permits个令牌
     *
     * @param permits 令牌数
     * @return 是否获取成功
     */
    public boolean acquire(int permits) {
        if (permits <= 0) {
            return true;
        }
        Object result = redisExecutor.execute(RateLimitScript.GET_TOKEN,
                Collections.singletonList(getRealPath()),
                Collections.singletonList(String.valueOf(permits)));
        return result != null;
    }

    /**
     * 尝试获取1个令牌并等待
     *
     * @param waitTime 等待时长
     * @param timeUnit 时间单位
     * @return 是否获取成功
     */
    public boolean tryAcquire(long waitTime, TimeUnit timeUnit) {
        return tryAcquire(1, waitTime, timeUnit);
    }

    /**
     * 尝试获取permits个令牌并等待
     *
     * @param permits  令牌个数
     * @param waitTime 等待时长
     * @param timeUnit 时间单位
     * @return 是否获取成功
     */
    public boolean tryAcquire(int permits, long waitTime, TimeUnit timeUnit) {
        long maxTime = System.currentTimeMillis() + timeUnit.toMillis(waitTime);
        long nanos = TimeUnit.SECONDS.toNanos(1);

        boolean result;
        try {
            while (!(result = acquire(permits))) {
                long timeout = maxTime - System.currentTimeMillis();
                if (timeout <= 0) {
                    //timeout return false
                    break;
                }
                LockSupport.parkNanos(this, nanos);
            }
        } catch (Exception e) {
            log.error("try lock error", e);
            return false;
        }

        return result;
    }

    public String getPath() {
        return path;
    }

    public Integer getMaxPermits() {
        return maxPermits;
    }

    public Integer getRate() {
        return rate;
    }

    /**
     * 获取补充前缀后的path
     */
    private String getRealPath() {
        return KEY_PRE_PATH + path;
    }

}
