package cn.cheny.toolbox.redis.lock.awaken.listener;

import cn.cheny.toolbox.redis.lock.LockConstant;
import lombok.extern.slf4j.Slf4j;
import redis.clients.jedis.JedisPubSub;

import java.util.LinkedList;

/**
 * todo 未实现
 *
 * @author cheney
 * @date 2019/6/6
 */
@Slf4j
public class JedisSubLockManager extends JedisPubSub implements SubLockManager {

    private LinkedList<LockListener> LockListeners = new LinkedList<>();

    private final Object lock = new Object();

    public JedisSubLockManager() {
        super();
        super.psubscribe(LockConstant.LOCK_CHANNEL + "*");
    }

    @Override
    public void onPMessage(String pattern, String channel, String message) {
        log.info("收到解锁信号{}", channel);
        if (AWAKE_MESSAGE.equals(message)) {
            synchronized (lock) {
                LockListeners.stream()
                        .filter(lockListener -> channel.equals(lockListener.getListenerChannel()))
                        .forEach(LockListener::handleListener);
                LockListeners.removeIf(lockListener -> channel.equals(lockListener.getListenerChannel()));
            }
        }
    }

    @Override
    public void addMessageListener(LockListener lockListener) {
        synchronized (lock) {
            LockListeners.add(lockListener);
        }
    }
}
