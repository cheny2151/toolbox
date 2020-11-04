package cn.cheny.toolbox.redis.lock.autolease;

import cn.cheny.toolbox.other.NamePrefixThreadFactory;
import cn.cheny.toolbox.redis.lock.executor.RedisExecutor;
import lombok.extern.slf4j.Slf4j;

import java.io.Closeable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static cn.cheny.toolbox.redis.lock.autolease.LeaseConstant.*;

/**
 * redis key自动续期持有者
 *
 * @author cheney
 * @date 2020-11-03
 */
@Slf4j
public class AutoLeaseHolder implements Closeable {

    private final ConcurrentHashMap<String, Lease> leaseMap;

    private final AtomicBoolean start;

    private ScheduledExecutorService scheduled;

    private RedisExecutor redisExecutor;

    public AutoLeaseHolder(RedisExecutor redisExecutor) {
        this.leaseMap = new ConcurrentHashMap<>(32);
        this.start = new AtomicBoolean(false);
        this.redisExecutor = redisExecutor;
    }

    public void addLease(Lease lease) {
        if (!start.get()) {
            initScheduled();
        }
        leaseMap.put(lease.getKey(), lease);
    }

    private void initScheduled() {
        if (start.compareAndSet(false, true)) {
            NamePrefixThreadFactory threadFactory = new NamePrefixThreadFactory("Lock-lease");
            scheduled = Executors.newSingleThreadScheduledExecutor(threadFactory);
            scheduled.scheduleAtFixedRate(this::leaseLock,
                    TASK_CYCLE, TASK_CYCLE, TimeUnit.MICROSECONDS);
        }
    }

    /**
     * 遍历leaseMap，查询需要延期的key并执行延期
     */
    private void leaseLock() {
        List<String> defaultDurationPaths = new ArrayList<>();
        List<String> waitRemoveKeys = new ArrayList<>(leaseMap.size());
        leaseMap.forEach((path, lease) -> {
            long leaseTime = lease.getLeaseTime();
            if (leaseTime == DURATION) {
                defaultDurationPaths.add(path);
            } else if (leaseTime > 0) {
                log.info("执行自动续租,续租key:{},续租时间:{}", path, leaseTime);
                addOneExpire(path, leaseTime);
            } else {
                waitRemoveKeys.add(path);
                return;
            }
            if (!lease.hasNext()) {
                waitRemoveKeys.add(path);
            }
        });
        log.info("执行自动续租,续租keys:[{}],续租时间:{}", defaultDurationPaths, DURATION);
        addMultiExpire(defaultDurationPaths, DURATION);
        log.info("删除续租,keys:{}", waitRemoveKeys);
        waitRemoveKeys.forEach(leaseMap::remove);
    }

    /**
     * 为一个path增长过期时间
     *
     * @param path redis key
     * @param time 毫秒值
     */
    private void addOneExpire(String path, long time) {
        redisExecutor.expire(path, time, TimeUnit.MILLISECONDS);
    }

    /**
     * 为多个path增长过期时间
     *
     * @param paths redis keys
     * @param time  毫秒值
     */
    private void addMultiExpire(List<String> paths, long time) {
        redisExecutor.execute(ADD_MULTI_EXPIRE_SCRIPT, paths, Collections.singletonList(String.valueOf(time)));
    }


    @Override
    public void close() {
        if (scheduled != null && !scheduled.isShutdown()) {
            try {
                scheduled.shutdown();
            } catch (Exception e) {
                // do nothing
            }
        }
    }

}
