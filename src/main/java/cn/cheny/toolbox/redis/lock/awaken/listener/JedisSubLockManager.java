package cn.cheny.toolbox.redis.lock.awaken.listener;

import cn.cheny.toolbox.redis.client.jedis.JedisClient;
import cn.cheny.toolbox.redis.lock.LockConstant;
import lombok.extern.slf4j.Slf4j;
import redis.clients.jedis.JedisPubSub;

/**
 * jedis实现订阅锁释放得广播
 *
 * @author cheney
 * @date 2019/6/6
 */
@Slf4j
public class JedisSubLockManager extends JedisPubSub implements SubLockManager {

    private final LockListeners LockListeners;

    private JedisClient jedisClient;

    public JedisSubLockManager(JedisClient jedisClient) {
        this(jedisClient, true);
    }

    public JedisSubLockManager(JedisClient jedisClient, boolean fair) {
        this.jedisClient = jedisClient;
        LockListeners = fair ? new LockListeners.FairLockListeners() : new LockListeners.NonFairLockListeners();
    }

    @Override
    public void onPMessage(String pattern, String channel, String message) {
        if (AWAKE_MESSAGE.equals(message)) {
            LockListeners.awake(channel);
        }
    }

    @Override
    public void addMessageListener(LockListener lockListener) {
        LockListeners.addLockListener(lockListener);
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
