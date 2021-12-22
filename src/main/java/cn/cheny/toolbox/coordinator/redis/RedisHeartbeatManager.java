package cn.cheny.toolbox.coordinator.redis;

import cn.cheny.toolbox.coordinator.HeartbeatManager;
import cn.cheny.toolbox.redis.lock.executor.RedisExecutor;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static cn.cheny.toolbox.coordinator.redis.RedisCoordinatorConstant.KEY_SPLIT;

/**
 * redis心跳注册管理器
 *
 * @author by chenyi
 * @date 2021/12/21
 */
@Slf4j
public class RedisHeartbeatManager implements HeartbeatManager {

    private static final int HEARTBEAT_PERIOD = 1100;
    private static final int HEARTBEAT_THREAD_PERIOD = 500;

    private final String sid;
    private final RedisExecutor redisExecutor;
    private ScheduledExecutorService heartbeatThread;

    private volatile Integer status;

    public RedisHeartbeatManager(String sid, RedisExecutor redisExecutor) {
        this.redisExecutor = redisExecutor;
        this.sid = sid;
        this.heartbeatThread = Executors.newSingleThreadScheduledExecutor();
        this.status = 1;
        this.startHeartbeatThread();
    }

    @Override
    public void close() {
        this.status = 0;
        this.heartbeatThread.shutdownNow();
        String sid = this.sid;
        String key = buildHeartbeatKey(sid);
        redisExecutor.del(key);
//        redisExecutor.hdel(RedisCoordinatorConstant.RESOURCES_REGISTER, this.sid);
    }

    @Override
    public String getSid() {
        return sid;
    }

    @Override
    public boolean isActive(String sid) {
        String curSid = this.sid;
        if (!curSid.equals(sid)) {
            String key = buildHeartbeatKey(sid);
            return redisExecutor.hasKey(key);
        }
        checkHeartbeat();
        return true;
    }

    @Override
    public void checkHeartbeat() {
        final String key = buildHeartbeatKey(this.sid);
        String val = redisExecutor.get(key);
        if (val != null) {
            return;
        }
        synchronized (this) {
            this.heartbeatThread.shutdown();
            redisExecutor.set(key, RedisCoordinatorConstant.HEARTBEAT_VAL);
            this.heartbeatThread = Executors.newSingleThreadScheduledExecutor();
            this.startHeartbeatThread();
        }
    }

    /**
     * 开始心跳线程
     */
    private void startHeartbeatThread() {
        final String key = buildHeartbeatKey(sid);
        heartbeatThread.scheduleAtFixedRate(() -> {
            try {
                boolean expire = redisExecutor.expire(key, HEARTBEAT_PERIOD, TimeUnit.MILLISECONDS);
                if (!expire) {
                    redisExecutor.set(key, RedisCoordinatorConstant.HEARTBEAT_VAL);
                    redisExecutor.expire(key, HEARTBEAT_PERIOD, TimeUnit.MILLISECONDS);
                }
            } catch (Exception e) {
                log.error("[Coordinator] 心跳包发送异常", e);
            }
        }, 0, HEARTBEAT_THREAD_PERIOD, TimeUnit.MILLISECONDS);
    }

    private String buildHeartbeatKey(String sid) {
        return RedisCoordinatorConstant.HEART_BEAT_KEY_PRE + KEY_SPLIT + sid;
    }

}
