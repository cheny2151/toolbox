package cn.cheny.toolbox.redis.lock.awaken;

import cn.cheny.toolbox.redis.factory.RedisLockFactory;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

import static cn.cheny.toolbox.redis.lock.LockConstant.*;
import static cn.cheny.toolbox.redis.lock.awaken.listener.SubLockManager.AWAKE_MESSAGE;

/**
 * 二级锁
 *
 * @author cheney
 * @date 2020-01-13
 */
@Slf4j
public class SecondLevelRedisLock extends AwakenRedisLock {

    /**
     * 锁类型标志
     */
    private final static String LOCK_TYPE_FLAG = "LOCK_TYPE";

    /**
     * 一级锁类型
     */
    public final static short TYPE_FIRST_LEVEL = 0;

    /**
     * 二级锁类型
     */
    public final static short TYPE_SECOND_LEVEL = 1;

    /**
     * master锁标识
     */
    private String secondPath;

    /**
     * 0：一级锁，1：二级锁
     */
    private short type;

    private SecondLevelRedisLock(String firstPath, String secondPath) {
        super(firstPath);
        this.secondPath = secondPath;
        type = secondPath == null ? TYPE_FIRST_LEVEL : TYPE_SECOND_LEVEL;
    }

    /**
     * 获取一级锁对象
     *
     * @param firstPath 一级锁路径
     * @return 一级锁对象
     */
    public static SecondLevelRedisLock firstLevelLock(String firstPath) {
        return new SecondLevelRedisLock(firstPath, null);
    }

    /**
     * 获取二级锁对象
     *
     * @param firstPath  一级锁路径
     * @param secondPath 二级锁路径
     * @return 二级锁对象
     */
    public static SecondLevelRedisLock secondLevelLock(String firstPath, String secondPath) {
        return new SecondLevelRedisLock(firstPath, secondPath);
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
        args.add(LOCK_TYPE_FLAG);
        args.add(String.valueOf(leaseTime));
        args.add(String.valueOf(type));
        if (type == 1) {
            args.add(secondPath);
        }
        return execute(SECONDARY_LOCK_LUA_SCRIPT, keys, args);
    }

    /**
     * 执行解锁脚本
     *
     * @return redis执行脚本返回值
     */
    protected Object unLockScript() {
        ArrayList<String> keys = new ArrayList<>();
        keys.add(path);
        keys.add(getChannelName());
        ArrayList<String> args = new ArrayList<>();
        args.add(LOCK_TYPE_FLAG);
        args.add(AWAKE_MESSAGE);
        args.add(String.valueOf(type));
        if (TYPE_SECOND_LEVEL == type) {
            args.add(secondPath);
        }
        return execute(SECONDARY_UNLOCK_LUA_SCRIPT, keys, args);
    }

    protected String getChannelName() {
        String base = LOCK_CHANNEL + path;
        if (TYPE_SECOND_LEVEL == type) {
            return base + ":" + secondPath;
        }
        return base;
    }

    @Override
    public String pathPreLabel() {
        return "SECOND_LEVEL:";
    }

    /**
     * 获取二级锁标识
     *
     * @return 二级锁
     */
    public String getSecondPath() {
        return secondPath;
    }

    /**
     * 获取锁类型
     *
     * @return 0：一级锁，1：二级锁
     */
    public short getType() {
        return type;
    }
}
