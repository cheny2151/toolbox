package cn.cheny.toolbox.redis.lock.awaken.listener;

import cn.cheny.toolbox.redis.client.jedis.JedisClient;
import cn.cheny.toolbox.redis.lock.LockConstant;
import lombok.extern.slf4j.Slf4j;
import redis.clients.jedis.JedisPubSub;

import java.util.LinkedList;

/**
 * jedis实现订阅锁释放得广播
 *
 * @author cheney
 * @date 2019/6/6
 */
@Slf4j
public class JedisSubLockManager extends JedisPubSub implements SubLockManager {

    private final LinkedList<LockListener> LockListeners = new LinkedList<>();

    private final Object lock = new Object();

    private JedisClient jedisClient;

    public JedisSubLockManager(JedisClient jedisClient) {
        super();
        this.jedisClient = jedisClient;
    }

    @Override
    public void onPMessage(String pattern, String channel, String message) {
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

    @Override
    public void init() {
        // 开启订阅线程
        new Thread(() -> {
            try {
                String channel = LockConstant.LOCK_CHANNEL + "*";
                jedisClient.psubscribe(this, channel);
            } catch (Exception e) {
                log.error("redis锁订阅线程异常,执行重新订阅", e);
                init();
            }
        }).start();
    }
}
