package cn.cheny.toolbox.coordinator.redis;

import cn.cheny.toolbox.coordinator.BaseResourceCoordinator;
import cn.cheny.toolbox.coordinator.msg.ReBalanceMessage;
import cn.cheny.toolbox.coordinator.resource.Resource;
import cn.cheny.toolbox.coordinator.resource.ResourceManager;
import cn.cheny.toolbox.redis.lock.RedisLock;
import cn.cheny.toolbox.redis.lock.awaken.ReentrantRedisLock;
import cn.cheny.toolbox.redis.lock.executor.RedisExecutor;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * redis资源协调器接口
 *
 * @author by chenyi
 * @date 2021/11/10
 */
@Slf4j
public class RedisCoordinator<T extends Resource> extends BaseResourceCoordinator<T> {

    private static final int CHECK_PERIOD = 10 * 1000;
    private static final int HEARTBEAT_PERIOD = 1100;
    private static final String KEY_SPLIT = ":";
    private static final String VAL_SPLIT = ",";
    private static final String EXIST_FLAG = "1";

    private final String curFlag;

    private final RedisExecutor redisExecutor;
    private ScheduledExecutorService heartbeatThread;
    private final ScheduledExecutorService checkThread;

    private volatile Integer status;

    public RedisCoordinator(String curFlag, Integer port, ResourceManager<T> resourceManager, RedisExecutor redisExecutor) {
        super(resourceManager);
        this.redisExecutor = redisExecutor;
        this.curFlag = curFlag + KEY_SPLIT + port;
        this.heartbeatThread = Executors.newSingleThreadScheduledExecutor();
        this.checkThread = Executors.newSingleThreadScheduledExecutor();
        this.status = 1;
        this.register();
        this.startHeartbeatThread();
        this.tryRebalanced();
        this.startCheckThread();
    }

    private void register() {
        String curFlag = this.curFlag;
        final String heartbeatKey = buildHeartbeatKey(curFlag);
        List<String> keys = Arrays.asList(heartbeatKey, RedisCoordinatorConstant.RESOURCES_REGISTER);
        List<String> values = Arrays.asList(RedisCoordinatorConstant.HEARTBEAT_VAL, curFlag, "");
        // lua初始化注册信息
        redisExecutor.execute(RedisCoordinatorConstant.INIT_REGISTER_SCRIPT, keys, values);
    }

    @Override
    public void tryRebalanced() {
        if (status != 1) {
            return;
        }
        try (RedisLock redisLock = new ReentrantRedisLock(RedisCoordinatorConstant.RE_BALANCE_LOCK)) {
            if (redisLock.tryLock(0, TimeUnit.SECONDS)) {
                String curFlag = this.curFlag;
                ResourceManager<T> resourceManager = getResourceManager();
                Set<T> allResources = resourceManager.getAllResources();
                if (allResources == null) {
                    allResources = new HashSet<>();
                }
                // 当前注册的实例信息
                Map<String, String> registerInfo = redisExecutor.hgetall(RedisCoordinatorConstant.RESOURCES_REGISTER);
                // 当前存活实例的注册信息
                ConcurrentHashMap<String, String> active = filterActive(registerInfo);
                if (!active.containsKey(curFlag)) {
                    active.put(curFlag, "");
                }
                if (!checkShouldReBalance(registerInfo, active, allResources)) {
                    return;
                }
                // 执行reBalance
                List<T> allocated = new ArrayList<>();
                reBalanceResources(active, allResources);
                redisExecutor.del(RedisCoordinatorConstant.RESOURCES_REGISTER);
                redisExecutor.hmset(RedisCoordinatorConstant.RESOURCES_REGISTER, active);

                String flags = active.get(curFlag);
                if (StringUtils.isNotEmpty(flags)) {
                    List<T> curResources = buildByFlags(flags);
                    allocated.addAll(curResources);
                }
                log.info("[Coordinator] 完成重平衡,资源分配结果为:{}", active);
                allocateNewResources(allocated);
                sendReBalanced();
            }
        }
    }

    @Override
    public void refreshCurrentResources() {
        if (status != 1) {
            return;
        }
        String flags = redisExecutor.hget(RedisCoordinatorConstant.RESOURCES_REGISTER, this.curFlag);
        if (flags == null) {
            log.info("[Coordinator] 当前实例未分配资源,执行reBalance");
            this.checkHeartbeat();
            this.tryRebalanced();
        } else {
            List<T> curResources = buildByFlags(flags);
            allocateNewResources(curResources);
        }
    }

    @Override
    public void close() {
        this.status = 0;
        this.heartbeatThread.shutdownNow();
        this.checkThread.shutdownNow();
        String curFlag = this.curFlag;
        String key = buildHeartbeatKey(curFlag);
        redisExecutor.del(key);
        redisExecutor.hdel(RedisCoordinatorConstant.RESOURCES_REGISTER, this.curFlag);
        this.sendReBalanceRequiredMsg();
    }

    public String getCurFlag() {
        return curFlag;
    }

    /**
     * 检查是否需要重平衡
     * 1.注册curFlag与存活curFlag一致
     * 2.注册resource与现有一致
     * 3.注册节点资源数平衡
     *
     * @param registerInfo 原注册信息
     * @param active       存活注册
     * @param allResources 当前所有资源
     * @return 是否需要重平衡
     */
    private boolean checkShouldReBalance(Map<String, String> registerInfo, Map<String, String> active, Set<T> allResources) {
        // 检查实例curFlag
        Set<String> registerKeys = registerInfo.keySet();
        Set<String> activeKeys = active.keySet();
        Collection<String> disjunction = CollectionUtils.disjunction(registerKeys, activeKeys);
        if (disjunction.size() == 0) {
            // 检查resource
            List<String> flagList = allResources.stream()
                    .map(Resource::flag)
                    .collect(Collectors.toList());
            List<String> flagInRedis = registerInfo.values()
                    .stream()
                    .flatMap(info -> Arrays.stream(info.split(VAL_SPLIT)))
                    .collect(Collectors.toList());
            Collection<String> disjunctionResources = CollectionUtils.disjunction(flagList, flagInRedis);
            if (disjunctionResources.size() == 0) {
                // 检查是否平衡
                List<Integer> resourceLen = registerInfo.values()
                        .stream()
                        .map(info -> StringUtils.isEmpty(info) ? 0 : info.split(VAL_SPLIT).length)
                        .collect(Collectors.toList());
                Optional<Integer> max = resourceLen.stream()
                        .max(Comparator.comparingInt(l -> l));
                Optional<Integer> min = resourceLen.stream()
                        .min(Comparator.comparingInt(l -> l));
                if (!max.isPresent() || !min.isPresent() || max.get() - min.get() > 1) {
                    log.info("[Coordinator] 触发rebalanced，资源分配不均衡");
                } else {
                    return false;
                }
            } else {
                log.info("[Coordinator] 触发rebalanced，发现新资源列表:{}", allResources);
            }
        } else {
            log.info("[Coordinator] 实例发生变化，原实例:{}，现有实例:{}", registerKeys, activeKeys);
        }
        return true;
    }

    /**
     * 过滤存活的实例注册信息
     *
     * @param registerInfo 当前所有实例注册信息
     * @return 存活的实例注册信息
     */
    private ConcurrentHashMap<String, String> filterActive(Map<String, String> registerInfo) {
        String curFlag = this.curFlag;
        ConcurrentHashMap<String, String> fixRegister = new ConcurrentHashMap<>();
        for (Map.Entry<String, String> entry : registerInfo.entrySet()) {
            String flag = entry.getKey();
            if (!curFlag.equals(flag)) {
                String key = buildHeartbeatKey(flag);
                Boolean hasKey = redisExecutor.hasKey(key);
                if (hasKey == null || !hasKey) {
                    continue;
                }
            }
            fixRegister.put(flag, entry.getValue());
        }
        return fixRegister;
    }

    /**
     * 重新平衡实例注册的资源
     *
     * @param active       存活的注册信息
     * @param allResources 所有现存资源
     */
    private void reBalanceResources(ConcurrentHashMap<String, String> active, Set<T> allResources) {
        if (CollectionUtils.isEmpty(allResources)) {
            active.replaceAll((k, v) -> "");
            return;
        }
        Set<String> flags = allResources.stream().map(Resource::flag).collect(Collectors.toSet());
        List<String> availableFlag = new ArrayList<>(flags);
        int curFlagSize = active.size();
        int leastSize = flags.size() / curFlagSize;
        if (leastSize == 0) {
            log.warn("资源不足分配，请减少实例或者增加资源；当前实例数:{},当前资源数:{}", curFlagSize, leastSize);
        } else {
            for (Map.Entry<String, String> entry : active.entrySet()) {
                String curFlag = entry.getKey();
                String val = entry.getValue();
                // 原注册flag
                List<String> originFlags = parseFlags(val);
                // 原可用flag(交集)
                Collection<String> originAvailableFlag = CollectionUtils.intersection(originFlags, availableFlag);
                int hfSize = originAvailableFlag.size();
                Set<String> newFlags;
                if (hfSize > leastSize) {
                    // 该curFlag原可用注册数大于当前平均值
                    newFlags = originAvailableFlag.stream().limit(leastSize).collect(Collectors.toSet());
                } else {
                    // 该curFlag原可用注册数小于当前平均值
                    int addSize = leastSize - hfSize;
                    newFlags = new HashSet<>(originAvailableFlag);
                    availableFlag.removeAll(newFlags);
                    for (int i = 0; i < addSize; i++) {
                        newFlags.add(availableFlag.get(i));
                    }
                }
                String newVal = String.join(VAL_SPLIT, newFlags);
                active.put(curFlag, newVal);
                availableFlag.removeAll(newFlags);
            }
            if (availableFlag.size() > 0) {
                List<String> keySet = new ArrayList<>(active.keySet());
                for (int i = 0; i < availableFlag.size(); i++) {
                    String key = keySet.get(i);
                    String flag0 = availableFlag.get(i);
                    String val = active.get(key);
                    String newVal;
                    if (StringUtils.isEmpty(val)) {
                        newVal = flag0;
                    } else {
                        newVal = val + VAL_SPLIT + flag0;
                    }
                    active.put(key, newVal);
                }
            }
        }
    }

    /**
     * 开始心跳线程
     */
    private void startHeartbeatThread() {
        final String key = buildHeartbeatKey(curFlag);
        heartbeatThread.scheduleAtFixedRate(() -> {
            try {
                boolean expire = redisExecutor.expire(key, 1100, TimeUnit.MILLISECONDS);
                if (!expire) {
                    redisExecutor.set(key, EXIST_FLAG);
                    redisExecutor.expire(key, HEARTBEAT_PERIOD, TimeUnit.MILLISECONDS);
                }
            } catch (Exception e) {
                log.error("[Coordinator] 心跳包发送异常", e);
            }
        }, 0, 500, TimeUnit.MILLISECONDS);
    }

    /**
     * 开始检查资源线程
     * 1.检查资源是否变化
     * 2.检查是否有实例异常断开
     */
    private void startCheckThread() {
        checkThread.scheduleWithFixedDelay(() -> {
            try {
                this.tryRebalanced();
                /*// 检查资源是否变化
                ResourceManager<T> resourceManager = this.getResourceManager();
                List<T> allResources = resourceManager.getAllResources();
                HashOperations<String, String, String> opsForHash = redisTemplate.opsForHash();
                Map<String, String> registerInfo = opsForHash.entries(RedisCoordinatorConstant.RESOURCES_REGISTER);
                List<String> flagsInRedis = registerInfo.values()
                        .stream()
                        .map(this::parseFlags)
                        .flatMap(Collection::stream)
                        .collect(Collectors.toList());
                Collection<Object> disjunction = CollectionUtils.disjunction(allResources, flagsInRedis);
                if (disjunction.size() > 0) {
                    this.tryRebalanced();
                    return;
                }
                // 检查是否有实例异常断开
                ConcurrentHashMap<String, String> active = filterActive(registerInfo);
                if (active.size() != registerInfo.size()) {
                    this.tryRebalanced();
                }*/
            } catch (Exception e) {
                log.error("[Coordinator] 资源检查异常", e);
            }
        }, CHECK_PERIOD, CHECK_PERIOD, TimeUnit.MILLISECONDS);
    }

    /**
     * 检查当前实例心跳
     * 1.是否已注册心跳
     * 2.未注册则重启心跳线程
     */
    private void checkHeartbeat() {
        final String key = buildHeartbeatKey(curFlag);
        String val = redisExecutor.get(key);
        if (val != null) {
            return;
        }
        synchronized (this) {
            this.heartbeatThread.shutdown();
            redisExecutor.set(key, EXIST_FLAG);
            this.heartbeatThread = Executors.newSingleThreadScheduledExecutor();
            this.startHeartbeatThread();
        }
    }

    private String buildHeartbeatKey(String curFlag) {
        return RedisCoordinatorConstant.HEART_BEAT_KEY_PRE + KEY_SPLIT + curFlag;
    }

    private List<String> parseFlags(String val) {
        return StringUtils.isEmpty(val) ? Collections.emptyList() : Arrays.asList(val.split(VAL_SPLIT));
    }

    private List<T> buildByFlags(String flags) {
        ResourceManager<T> resourceManager = this.getResourceManager();
        return StringUtils.isEmpty(flags) ? new ArrayList<>() :
                Arrays.stream(flags.split(VAL_SPLIT))
                        .map(resourceManager::buildByFlag)
                        .collect(Collectors.toList());
    }

    private void sendReBalanceRequiredMsg() {
        ReBalanceMessage message = new ReBalanceMessage(ReBalanceMessage.TYPE_REQUIRED_RE_BALANCE, curFlag);
        this.redisExecutor.publish(RedisCoordinatorConstant.REDIS_CHANNEL, JSON.toJSONString(message));
    }

    private void sendReBalanced() {
        ReBalanceMessage message = new ReBalanceMessage(ReBalanceMessage.TYPE_RE_BALANCE, this.curFlag);
        redisExecutor.publish(RedisCoordinatorConstant.REDIS_CHANNEL, JSON.toJSONString(message));
    }

}
