package cn.cheny.toolbox.redis.lock.awaken.listener;

/**
 * 锁订阅Manager
 *
 * @author cheney
 * @date 2019/6/6
 */
public interface SubLockManager {

    /**
     * awake标识
     */
    String AWAKE_MESSAGE = "AWAKE";

    void addMessageListener(LockListener lockListener);

   default void init(){};

}
