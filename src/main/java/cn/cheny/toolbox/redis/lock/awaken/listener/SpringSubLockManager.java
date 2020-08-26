package cn.cheny.toolbox.redis.lock.awaken.listener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;

import java.util.LinkedList;

/**
 * 订阅redis解锁信息
 *
 * @author cheney
 */
@Slf4j
public class SpringSubLockManager implements MessageListener, SubLockManager {

    private LinkedList<LockListener> LockListeners = new LinkedList<>();

    private final Object lock = new Object();

    @Override
    public void onMessage(Message message, byte[] bytes) {
        String unlockMsg = new String(message.getBody());
        String channel = new String(message.getChannel());
        if (AWAKE_MESSAGE.equals(unlockMsg)) {
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
