package cn.cheny.toolbox.coordinator.redis;

import cn.cheny.toolbox.coordinator.CoordinatorProperties;
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

    private final String id;
    private final String sid;
    private final String heartbeatKey;
    private final RedisExecutor redisExecutor;
    private ScheduledExecutorService heartbeatThread;

    private volatile Integer status;

    public RedisHeartbeatManager(CoordinatorProperties coordinatorProperties, String sid, RedisExecutor redisExecutor) {
        this.redisExecutor = redisExecutor;
        this.sid = sid;
        String key = coordinatorProperties.getKey();
        this.id = key;
        this.heartbeatKey = buildHeartbeatKey(key, sid);
        this.heartbeatThread = Executors.newSingleThreadScheduledExecutor();
        this.status = 1;
        this.startHeartbeatThread();
    }

    @Override
    public void close() {
        this.status = 0;
        this.heartbeatThread.shutdownNow();
        this.redisExecutor.del(heartbeatKey);
    }

    @Override
    public String getSid() {
        return sid;
    }

    @Override
    public boolean isActive(String sid) {
        String curSid = this.sid;
        if (!curSid.equals(sid)) {
            String key = buildHeartbeatKey(id, sid);
            return redisExecutor.hasKey(key);
        }
        checkHeartbeat();
        return true;
    }

    @Override
    public void checkHeartbeat() {
        final String key = heartbeatKey;
        String val = redisExecutor.get(key);
        if (val != null) {
            return;
        }
        synchronized (this) {
            this.heartbeatThread.shutdown();
            this.redisExecutor.set(key, RedisCoordinatorConstant.HEARTBEAT_VAL);
            this.heartbeatThread = Executors.newSingleThreadScheduledExecutor();
            this.startHeartbeatThread();
        }
    }

    /**
     * 开始心跳线程
     */
    private void startHeartbeatThread() {
        final String key = this.heartbeatKey;
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

    private String buildHeartbeatKey(String id, String sid) {
        String keyPre = RedisCoordinatorConstant.HEART_BEAT_KEY_PRE.buildKey(id);
        return keyPre + KEY_SPLIT + sid;
    }

}
