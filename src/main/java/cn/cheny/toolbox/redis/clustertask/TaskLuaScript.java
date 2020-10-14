package cn.cheny.toolbox.redis.clustertask;

/**
 * lua脚本
 *
 * @author cheney
 * @date 2019-09-03
 */
public class TaskLuaScript {

    /**
     * 增加步数脚本
     */
    public static final String ADD_STEP_LUA_SCRIPT =
            "if (redis.call('exists', KEYS[1]) == 1) then " +
                    "local step = redis.call('hincrby', KEYS[1], ARGV[1], 1); " +
                    "return step - 1; " +
                    "end;" +
                    "return nil; ";

    /**
     * 注册任务脚本
     */
    public static final String REGISTERED_LUA_SCRIPT =
            "if (redis.call('exists', KEYS[1]) == 1) then " +
                    "local count = redis.call('hincrby', KEYS[1], ARGV[1], ARGV[2]); " +
                    "return count; " +
                    "end;" +
                    "return nil; ";
}
