package cn.cheny.toolbox.redis.lock.awaken.listener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;

/**
 * 订阅redis解锁信息
 *
 * @author cheney
 */
@Slf4j
public class SpringSubLockManager implements MessageListener, SubLockManager {

    private final LockListeners LockListeners;

    public SpringSubLockManager() {
        // 默认为公平队列
        this(true);
    }

    public SpringSubLockManager(boolean fair) {
        LockListeners = fair ? new LockListeners.FairLockListeners() : new LockListeners.NonFairLockListeners();
    }

    @Override
    public void onMessage(Message message, byte[] bytes) {
        String unlockMsg = new String(message.getBody());
        String channel = new String(message.getChannel());
        if (AWAKE_MESSAGE.equals(unlockMsg)) {
            LockListeners.awake(channel);
        }
    }

    @Override
    public void addMessageListener(LockListener lockListener) {
        LockListeners.addLockListener(lockListener);
    }

}
