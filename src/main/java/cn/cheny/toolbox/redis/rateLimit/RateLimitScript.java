package cn.cheny.toolbox.redis.rateLimit;

/**
 * 令牌桶算法脚本
 *
 * @author cheney
 * @date 2020-02-26
 */
public class RateLimitScript {

    /**
     * 初始化脚本
     * max_permits：最大令牌个数；rate：每秒新增令牌数；permits：初始令牌个数
     */
    public final static String INIT = "redis.call('HMSET', KEYS[1], 'max_permits', ARGV[1], 'rate', ARGV[2], 'permits', ARGV[3]); return 1;";

    /**
     * 获取令牌脚本
     * 具体见getToken.lua
     */
    public final static String GET_TOKEN =
            "local rate_limit = redis.call('HMGET', KEYS[1], 'last_time', 'permits', 'rate', 'max_permits');" +
            "local last_time = rate_limit[1];" +
            "local permits = rate_limit[2];" +
            "local rate = rate_limit[3];" +
            "local max_permits = rate_limit[4];" +
            "redis.replicate_commands();" +
            "local cur_time = redis.call('time')[1];" +
            "local expect_permits = max_permits;" +
            "if (last_time ~= false and last_time ~= nil) then " +
            "local add_permits = (cur_time - last_time) * rate;" +
            "if (add_permits > 0) then " +
            "redis.call('HSET', KEYS[1], 'last_time', cur_time);" +
            "end " +
            "expect_permits = math.min(add_permits + permits, tonumber(max_permits));" +
            "else " +
            "redis.call('HSET', KEYS[1], 'last_time', cur_time);" +
            "end " +
            "if (expect_permits < tonumber(ARGV[1])) then " +
            "redis.call('HSET', KEYS[1], 'permits', expect_permits);" +
            "return nil;" +
            "else " +
            "redis.call('HSET', KEYS[1], 'permits', expect_permits - ARGV[1]);" +
            "return 1;" +
            "end";

    /**
     * 令牌桶redis key前置标识
     */
    public final static String KEY_PRE_PATH = "RATE_LIMIT:";

}
