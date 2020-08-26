package cn.cheny.toolbox.redis.lock.awaken;

import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;

import java.util.*;

import static cn.cheny.toolbox.redis.lock.LockConstant.*;
import static cn.cheny.toolbox.redis.lock.awaken.listener.SubLockManager.AWAKE_MESSAGE;

/**
 * redis多路径锁
 * 通过redis set实现
 * 支持同时对多个path上锁，path与当前锁路径的set存在交集则获取锁失败
 *
 * @author cheney
 */
@Slf4j
public class MultiPathRedisLock extends AwakenRedisLock {

    /**
     * 多路径path
     */
    private Set<String> multiPaths;

    public MultiPathRedisLock(String path, Collection<String> multiPaths) {
        super(path);
        if (CollectionUtils.isEmpty(multiPaths)) {
            throw new IllegalArgumentException("multi paths can not be empty");
        }
        this.multiPaths = new HashSet<>(multiPaths);
    }

    /**
     * 执行上锁脚本
     *
     * @param leaseTime 超时释放锁时间
     * @return redis执行脚本返回值
     */
    protected Object LockScript(long leaseTime) {
        List<String> keys = new ArrayList<>();
        keys.add(path);
        List<String> args = new ArrayList<>();
        args.add(String.valueOf(leaseTime));
        args.addAll(multiPaths);
        return execute(MULTI_LOCK_LUA_SCRIPT, keys, args);
    }

    /**
     * 执行解锁脚本
     *
     * @return redis执行脚本返回值
     */
    protected Object unLockScript() {
        List<String> keys = new ArrayList<>();
        keys.add(path);
        keys.add(getChannelName());
        List<String> args = new ArrayList<>();
        args.add(AWAKE_MESSAGE);
        args.addAll(multiPaths);
        return execute(MULTI_UNLOCK_LUA_SCRIPT, keys, args);
    }

    protected String getChannelName() {
        return LOCK_CHANNEL + path;
    }

    @Override
    public String pathPreLabel() {
        return "MULTI:";
    }

}
