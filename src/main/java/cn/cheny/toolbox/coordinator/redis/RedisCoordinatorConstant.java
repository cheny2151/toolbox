package cn.cheny.toolbox.coordinator.redis;

/**
 * @author by chenyi
 * @date 2021/11/12
 */
public class RedisCoordinatorConstant {

    public static final String REDIS_CHANNEL = "COORDINATOR:RE_BALANCE:CHANNEL";
    public static final String RESOURCES_REGISTER = "COORDINATOR:REGISTER";
    public static final String RE_BALANCE_LOCK = "COORDINATOR:RE_BALANCE:LOCK";
    public static final String HEART_BEAT_KEY_PRE = "COORDINATOR:HEART_BEAT";
    public static final String HEARTBEAT_VAL = "1";

    public static final String INIT_REGISTER_SCRIPT = "redis.call('set', KEYS[1],ARGV[1])) " +
            "redis.call('hset', KEYS[2], KEYS[3], ARGV[2]) return nil";

}