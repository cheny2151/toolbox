package cn.cheny.toolbox.coordinator.redis;

import cn.cheny.toolbox.coordinator.BaseResourceCoordinator;
import cn.cheny.toolbox.coordinator.CoordinatorProperties;
import cn.cheny.toolbox.coordinator.HeartbeatManager;
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
    private static final String VAL_SPLIT = ",";

    private final HeartbeatManager heartBeatManager;
    private final RedisExecutor redisExecutor;
    private final ScheduledExecutorService checkThread;
    private final String resourceKey;
    private final String lockKey;
    private final String channelKey;

    private volatile Integer status;

    public RedisCoordinator(CoordinatorProperties coordinatorProperties, HeartbeatManager heartBeatManager,
                            ResourceManager<T> resourceManager, RedisExecutor redisExecutor) {
        super(resourceManager);
        String key = coordinatorProperties.getKey();
        this.heartBeatManager = heartBeatManager;
        this.redisExecutor = redisExecutor;
        this.checkThread = Executors.newSingleThreadScheduledExecutor();
        this.status = 1;
        String resourceKeyPre = RedisCoordinatorConstant.RESOURCES_REGISTER.buildKey(key);
        this.resourceKey = resourceKeyPre + RedisCoordinatorConstant.KEY_SPLIT + resourceManager.resourceKey();
        this.lockKey = RedisCoordinatorConstant.RE_BALANCE_LOCK.buildKey(key);
        this.channelKey = RedisCoordinatorConstant.REDIS_CHANNEL.buildKey(key);
        this.initCurrent();
        this.tryRebalanced();
        this.startCheckThread();
    }

    @Override
    public void tryRebalanced() {
        if (status != 1) {
            return;
        }
        try (RedisLock redisLock = new ReentrantRedisLock(lockKey)) {
            if (redisLock.tryLock(0, TimeUnit.SECONDS)) {
                String sid = this.getSid();
                ResourceManager<T> resourceManager = getResourceManager();
                Set<T> allResources = resourceManager.getAllResources();
                if (allResources == null) {
                    allResources = new HashSet<>();
                }
                // 当前注册的实例信息
                Map<String, String> registerInfo = redisExecutor.hgetall(resourceKey);
                // 当前存活实例的注册信息
                ConcurrentHashMap<String, String> active = filterActive(registerInfo);
                if (!active.containsKey(sid)) {
                    active.put(sid, "");
                }
                if (checkShouldRebalanced(registerInfo, active, allResources)) {
                    // 执行reBalance
                    reBalanceResources(active, allResources);
                    redisExecutor.del(resourceKey);
                    redisExecutor.hmset(resourceKey, active);
                    log.info("[Coordinator] 完成重平衡,资源分配结果为:{}", active);
                    sendReBalanced();
                }

                String resourceFlag = active.get(sid);
                List<T> allocated = buildByFlags(resourceFlag);
                allocatedResources(allocated);
            } else {
                refreshCurrentResources();
            }
        }
    }

    @Override
    public void refreshCurrentResources() {
        if (status != 1) {
            return;
        }
        String resourceFlag = redisExecutor.hget(resourceKey, getSid());
        if (resourceFlag == null) {
            log.warn("当前节点未初始化资源");
            heartBeatManager.checkHeartbeat();
            initCurrent();
        } else {
            List<T> curResources = buildByFlags(resourceFlag);
            allocatedResources(curResources);
        }
    }

    @Override
    public void close() {
        String sid = getSid();
        this.status = 0;
        this.checkThread.shutdownNow();
        this.redisExecutor.hdel(resourceKey, sid);
        this.sendReBalanceRequiredMsg();
    }

    public String getSid() {
        return heartBeatManager.getSid();
    }

    /**
     * 检查是否需要重平衡
     * 1.注册sid与存活sid一致
     * 2.注册resource与现有一致
     * 3.注册节点资源数平衡
     *
     * @param registerInfo 原注册信息
     * @param active       存活注册
     * @param allResources 当前所有资源
     * @return 是否需要重平衡
     */
    private boolean checkShouldRebalanced(Map<String, String> registerInfo, Map<String, String> active, Set<T> allResources) {
        // 检查实例sid
        Set<String> registerKeys = registerInfo.keySet();
        Set<String> activeKeys = active.keySet();
        if (sameCollection(registerKeys, activeKeys)) {
            // 检查resource
            List<String> flagList = allResources.stream()
                    .map(Resource::flag)
                    .collect(Collectors.toList());
            List<String> flagInRedis = registerInfo.values()
                    .stream()
                    .filter(StringUtils::isNotEmpty)
                    .flatMap(info -> Arrays.stream(info.split(VAL_SPLIT)))
                    .collect(Collectors.toList());
            if (sameCollection(flagList, flagInRedis)) {
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
                log.info("[Coordinator] 触发rebalanced，发现新资源列表或有其他实例下线:{}", allResources);
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
        ConcurrentHashMap<String, String> fixRegister = new ConcurrentHashMap<>();
        for (Map.Entry<String, String> entry : registerInfo.entrySet()) {
            String sid = entry.getKey();
            if (this.heartBeatManager.isActive(sid)) {
                fixRegister.put(sid, entry.getValue());
            }
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
        // 当前所有可用资源flags
        List<String> availableFlag = new ArrayList<>(flags);
        // 存活的实例数
        int serverSize = active.size();
        // 至少应持有数量
        int leastSize = flags.size() / serverSize;
        if (leastSize == 0) {
            log.warn("[Coordinator] 资源不足分配，请减少实例或者增加资源；当前实例数:{},当前资源数:{}", serverSize, leastSize);
        } else {
            for (Map.Entry<String, String> entry : active.entrySet()) {
                String sid = entry.getKey();
                String val = entry.getValue();
                // 原注册资源
                List<String> originFlags = parseResourceFlags(val);
                // 原可用资源(交集)
                Collection<String> originAvailable = CollectionUtils.intersection(originFlags, availableFlag);
                int oaSize = originAvailable.size();
                Set<String> newFlags;
                if (oaSize > leastSize) {
                    // 该实例原可用注册数大于当前平均值
                    newFlags = originAvailable.stream().limit(leastSize).collect(Collectors.toSet());
                } else {
                    // 该实例原可用注册数小于当前平均值
                    int addSize = leastSize - oaSize;
                    newFlags = new HashSet<>(originAvailable);
                    availableFlag.removeAll(newFlags);
                    for (int i = 0; i < addSize; i++) {
                        newFlags.add(availableFlag.get(i));
                    }
                }
                String newVal = String.join(VAL_SPLIT, newFlags);
                active.put(sid, newVal);
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
                Map<String, String> registerInfo = opsForHash.entries(resourceKey);
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

    private List<String> parseResourceFlags(String val) {
        return StringUtils.isEmpty(val) ? Collections.emptyList() : Arrays.asList(val.split(VAL_SPLIT));
    }

    private List<T> buildByFlags(String flags) {
        ResourceManager<T> resourceManager = this.getResourceManager();
        return StringUtils.isEmpty(flags) ? new ArrayList<>() :
                Arrays.stream(flags.split(VAL_SPLIT))
                        .map(resourceManager::buildByFlag)
                        .collect(Collectors.toList());
    }

    /**
     * 判断集合1、2是否一样
     *
     * @param collection1 集合1
     * @param collection2 集合2
     * @return 是否一样
     */
    private boolean sameCollection(Collection<String> collection1, Collection<String> collection2) {
        return collection1.size() == collection2.size() && collection1.containsAll(collection2);
    }

    /**
     * 发送重平衡请求广播
     */
    private void sendReBalanceRequiredMsg() {
        ReBalanceMessage message = new ReBalanceMessage(resourceKey, ReBalanceMessage.TYPE_NEED_TO_REBALANCE, this.getSid());
        this.redisExecutor.publish(channelKey, JSON.toJSONString(message));
    }

    /**
     * 发送已重平衡广播
     */
    private void sendReBalanced() {
        ReBalanceMessage message = new ReBalanceMessage(resourceKey, ReBalanceMessage.TYPE_REBALANCED, this.getSid());
        redisExecutor.publish(channelKey, JSON.toJSONString(message));
    }

    /**
     * 初始化为空资源
     */
    private void initCurrent() {
        redisExecutor.hset(resourceKey, getSid(), "");
    }

}
