package cn.cheny.toolbox.redis.lock.autolease;

/**
 * 续租常量类
 *
 * @author cheney
 * @date 2020-11-03
 */
public class LeaseConstant {

    /**
     * 一次续租的时长:1分钟（毫秒值）
     */
    public final static long DURATION = 60000;

    /**
     * 需要执行续期逻辑的阀值
     */
    public final static long USE_LEASE_THRESHOLD = (long) (DURATION * 1.5);

    /**
     * 定时任务周期
     */
    public final static long TASK_CYCLE = 50000;

    /**
     * add multi key expire
     */
    public final static String ADD_MULTI_EXPIRE_SCRIPT = "local t = ARGV[1] for i = 1, #KEYS do redis.call('pexpire', KEYS[i], t) end";

}
