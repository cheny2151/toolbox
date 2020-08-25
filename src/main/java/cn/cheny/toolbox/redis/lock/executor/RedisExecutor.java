package cn.cheny.toolbox.redis.lock.executor;

import java.util.List;

/**
 * redis命令执行器
 *
 * @author cheney
 * @date 2020-08-17
 */
public interface RedisExecutor {

    /**
     * 执行lua脚本并返回结果
     *
     * @param script 脚本内容
     * @param keys   key
     * @param args   arg
     * @return 脚本返回值
     */
    Object execute(String script, List<String> keys, List<String> args);

}
