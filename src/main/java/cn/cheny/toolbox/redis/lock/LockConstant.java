package cn.cheny.toolbox.redis.lock;

/**
 * @author cheney
 */
public class LockConstant {

    /**
     * 重入锁上锁脚本
     * keys:1,锁Path
     * ARGV:1,过期时间;2,当前线程ID
     * <p>
     * 返回：null代表上锁成功;数值为过期时间
     */
    public static final String AWAKEN_LOCK_LUA_SCRIPT = "if (redis.call('exists', KEYS[1]) == 0) then " +
            "redis.call('hset', KEYS[1], ARGV[2], 1); " +
            "if (tonumber(ARGV[1]) > 0) then " +
            "redis.call('pexpire', KEYS[1], ARGV[1]); " +
            "end;" +
            "return nil; " +
            "end; " +
            "if (redis.call('hexists', KEYS[1], ARGV[2]) == 1) then " +
            "redis.call('hincrby', KEYS[1], ARGV[2], 1); " +
            "if (tonumber(ARGV[1]) > 0) then " +
            "redis.call('pexpire', KEYS[1], ARGV[1]); " +
            "end;" +
            "return nil; " +
            "end; " +
            "return redis.call('pttl', KEYS[1]);";

    /**
     * 重入锁解锁脚本
     * keys:1,锁Path;2,channel名
     * ARGV:1,channel信息;2,重入时锁延长时间;3,当前线程ID
     * <p>
     * 返回：1代表解锁成功/已经解锁;null代表未持有该锁;0代表减少重入数
     */
    public static final String AWAKEN_UNLOCK_LUA_SCRIPT = "if (redis.call('exists', KEYS[1]) == 0) then " +
            "redis.call('publish', KEYS[2], ARGV[1]); " +
            "return 1; " +
            "end;" +
            "if (redis.call('hexists', KEYS[1], ARGV[3]) == 0) then " +
            "return nil;" +
            "end; " +
            "local counter = redis.call('hincrby', KEYS[1], ARGV[3], -1); " +
            "if (counter > 0) then " +
            "if (tonumber(ARGV[2]) > 0) then " +
            "redis.call('pexpire', KEYS[1], ARGV[2]); " +
            "end;" +
            "return 0; " +
            "else " +
            "redis.call('del', KEYS[1]); " +
            "redis.call('publish', KEYS[2], ARGV[1]); " +
            "return 1; " +
            "end; " +
            "return nil;";

    /**
     * 二级锁上锁脚本
     * keys:1,一级Path
     * ARGV:1,type标识KEY;2,过期时间;3,type值;4,二级path
     * <p>
     * 返回：null代表上锁成功;数值为过期时间
     */
    public static final String SECONDARY_LOCK_LUA_SCRIPT = "local type = tonumber(ARGV[3]);" +
            "if (redis.call('exists', KEYS[1]) == 0) then " +
            "redis.call('hset', KEYS[1], ARGV[1], type);" +
            "if (type == 1) then " +
            "redis.call('hset', KEYS[1], ARGV[4], 1)" +
            "end;" +
            "if (tonumber(ARGV[2]) > 0) then " +
            "redis.call('pexpire', KEYS[1], ARGV[2]); " +
            "end;" +
            "return nil; " +
            "end; " +
            // 注意：必须已上锁类型和欲上锁类型都为二级锁(1)才需要执行二级锁逻辑，否则直接返回上锁失败的剩余过期时间
            "if (tonumber(redis.call('hget', KEYS[1], KEYS[2])) == 1 and type == 1) then " +
            "if(redis.call('hexists', KEYS[1], ARGV[4]) == 0) then " +
            "redis.call('hset', KEYS[1], ARGV[4], 1);" +
            "if (tonumber(ARGV[2]) > 0) then " +
            "redis.call('pexpire', KEYS[1], ARGV[2]); " +
            "end;" +
            "return nil;" +
            "end;" +
            "end;" +
            "return redis.call('pttl', KEYS[1]);";

    /**
     * 二级锁解锁脚本
     * keys:1,一级Path;2,channel名
     * ARGV:1,type标识KEY;2,channel信息;3,type值;4,二级path
     * 注意：释放二级锁(hdel hkey)后需要检查hash长度(hlen)是否为1，为1时(只有type标识数据)释放一级锁。
     * <p>
     * 返回：1代表解锁成功/已经解锁;null代表未持有该锁
     */
    public static final String SECONDARY_UNLOCK_LUA_SCRIPT = "local type = tonumber(ARGV[3]);" +
            "if (redis.call('exists', KEYS[1]) == 0) then " +
            "redis.call('publish', KEYS[2], ARGV[2]);" +
            "return 1;" +
            "end;" +
            "if (tonumber(redis.call('hget', KEYS[1], ARGV[1])) == type) then " +
            "if (type == 0) then " +
            "redis.call('del', KEYS[1]);" +
            "else " +
            "if (redis.call('hexists', KEYS[1], ARGV[4]) == 0) then " +
            "redis.call('publish', KEYS[2], ARGV[2]);" +
            "return nil;" +
            "end;" +
            "redis.call('hdel', KEYS[1], ARGV[4]);" +
            // 检查是否释放一级锁
            "if (redis.call('hlen', KEYS[1]) == 1) then " +
            "redis.call('del', KEYS[1]);" +
            "end;" +
            "end;" +
            "redis.call('publish', KEYS[2], ARGV[2]); " +
            "return 1; " +
            "end;" +
            "return nil;";

    /**
     * 多路径锁上锁脚本
     * keys:1,路径set的key
     * ARGV:1,过期时间;剩余的为路径值，需将路径值存放到set中
     * <p>
     * 返回：null代表上锁成功;数值为过期时间
     */
    public static final String MULTI_LOCK_LUA_SCRIPT = "if (redis.call('exists', KEYS[1]) == 0) then " +
            "for i = 2, #ARGV do " +
            "redis.call('sadd', KEYS[1], ARGV[i]);" +
            "end " +
            "if (tonumber(ARGV[1]) > 0) then " +
            "redis.call('pexpire', KEYS[1], ARGV[1]);" +
            "end " +
            "return nil;" +
            "end " +
            "local ex = 0;" +
            "for i = 2, #ARGV do " +
            "if (redis.call('sismember', KEYS[1], ARGV[i]) == 1) then " +
            "ex = 1;" +
            "break;" +
            "end " +
            "end " +
            "if (ex == 0) then " +
            "for i = 2, #ARGV do " +
            "redis.call('sadd', KEYS[1], ARGV[i]);" +
            "end " +
            "if (tonumber(ARGV[1]) > 0) then " +
            "redis.call('pexpire', KEYS[1], ARGV[1]);" +
            "end " +
            "return nil;" +
            "else " +
            "return redis.call('pttl', KEYS[1]);" +
            "end ";


    /**
     * 多路径锁解锁脚本
     * keys:1,锁Path;2,channel路径
     * ARGV:1,channel信息;剩余的为路径值，需将路径值存放到set中
     * <p>
     * 返回：1,代表目前路径解锁成功/已经解锁;null代表未持有该锁
     */
    public static final String MULTI_UNLOCK_LUA_SCRIPT = "if (redis.call('exists', KEYS[1]) == 0) then " +
            "redis.call('publish', KEYS[2], ARGV[1]);" +
            "return 1;" +
            "end " +
            "for i = 2, #ARGV do " +
            "redis.call('srem', KEYS[1], ARGV[i]);" +
            "end " +
            "redis.call('publish', KEYS[2], ARGV[1]);" +
            "return 1;";

    public static final String LOCK_LUA_SCRIPT = "if (redis.call('exists', KEYS[1]) == 0) then " +
            "redis.call('hset', KEYS[1], ARGV[2], 1); " +
            "redis.call('pexpire', KEYS[1], ARGV[1]); " +
            "return nil; " +
            "end; " +
            "if (redis.call('hexists', KEYS[1], ARGV[2]) == 1) then " +
            "redis.call('hincrby', KEYS[1], ARGV[2], 1); " +
            "redis.call('pexpire', KEYS[1], ARGV[1]); " +
            "return nil; " +
            "end; " +
            "return redis.call('pttl', KEYS[1]);";

    public static final String UNLOCK_LUA_SCRIPT = "if (redis.call('exists', KEYS[1]) == 0) then " +
            "return nil; " +
            "end;" +
            "if (redis.call('hexists', KEYS[1], ARGV[2]) == 0) then " +
            "return nil;" +
            "end; " +
            "local counter = redis.call('hincrby', KEYS[1], ARGV[2], -1); " +
            "if (counter > 0) then " +
            "redis.call('pexpire', KEYS[1], ARGV[1]); " +
            "return 0; " +
            "else " +
            "redis.call('del', KEYS[1]); " +
            "return 1; " +
            "end; " +
            "return nil;";

    public static final String LOCK_CHANNEL = "LOCK_CHANNEL:";

     /*"local type = tonumber(ARGV[2]);if (redis.call('exists', KEYS[1]) == 0) then redis.call('hset', KEYS[1], KEYS[2], type);if (type == 1) then redis.call('hset', KEYS[1], ARGV[3], 1)end;if (tonumber(ARGV[1]) > 0) then redis.call('pexpire', KEYS[1], ARGV[1]);end;return nil;end;if (tonumber(redis.call('hget', KEYS[1], KEYS[2])) == 1) then if(redis.call('hexists', KEYS[1], ARGV[3]) == 0) then redis.call('hset', KEYS[1], ARGV[3], 1);if (tonumber(ARGV[1]) > 0) then redis.call('pexpire', KEYS[1], ARGV[1]);end;return nil;end;end;return redis.call('pttl', KEYS[1]);";

     "local type = tonumber(ARGV[2]);if (redis.call('exists', KEYS[1]) == 0) then redis.call('publish', KEYS[3], ARGV[1]);return 1;end;if (tonumber(redis.call('hget', KEYS[1], KEYS[2])) == type) then if (type == 0) then redis.call('del', KEYS[1]);else if (redis.call('hexists', KEYS[1], ARGV[3]) == 0) then redis.call('publish', KEYS[3], ARGV[1]);return nil;end;redis.call('hdel', KEYS[1], ARGV[3]);if (redis.call('hlen', KEYS[1]) == 1) then redis.call('del', KEYS[1]);end;end;redis.call('publish', KEYS[3], ARGV[1]); return 1;end;return nil;";*/

}
