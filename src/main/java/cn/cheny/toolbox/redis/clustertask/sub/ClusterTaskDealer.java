package cn.cheny.toolbox.redis.clustertask.sub;

import cn.cheny.toolbox.other.page.Limit;
import cn.cheny.toolbox.redis.RedisConfiguration;
import cn.cheny.toolbox.redis.RedisKeyUtils;
import cn.cheny.toolbox.redis.clustertask.TaskConfig;
import cn.cheny.toolbox.redis.clustertask.TaskInfo;
import cn.cheny.toolbox.redis.lock.executor.RedisExecutor;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import static cn.cheny.toolbox.redis.clustertask.TaskLuaScript.ADD_STEP_LUA_SCRIPT;
import static cn.cheny.toolbox.redis.clustertask.TaskLuaScript.REGISTERED_LUA_SCRIPT;
import static cn.cheny.toolbox.redis.clustertask.pub.ClusterTaskPublisher.CLUSTER_TASK_PRE_KEY;

/**
 * 集群任务处理器
 * 负责集群任务的分页的获取，线程任务的分配与执行，
 * 任务执行结束后的回调。
 *
 * v1.1.0 更新：执行任务完成回调函数afterAllTask()变更：
 *             订阅到任务时通过redis脚本注册执行任务的分布式服务器数,
 *             每个服务器执行完毕任务时再减少注册数，直至0视为所有任务执行完毕,
 *             此时执行清除redis任务数据,回调afterAllTask()。
 *             见{@link ClusterTaskDealer#registeredTask(String taskId, int concurrentNums, ClusterTaskSubscriber subscriber)}
 * v1.1.1 更新 {@link TaskInfo} 新增header任务信息头，方便拓展
 * @author cheney
 * @date 2019-09-03
 * @version 1.1.0
 */
@Slf4j
public class ClusterTaskDealer {

    /**
     * 服务器注册执行任务标识
     */
    private final static String REGISTERED_LABEL = "REGISTERED_COUNT";

    /**
     * redis执行器
     */
    private final RedisExecutor redisExecutor;

    /**
     * 任务线程池
     */
    private ExecutorService taskExecutor;

    public ClusterTaskDealer(ExecutorService taskExecutor) {
        this(RedisConfiguration.DEFAULT.getRedisManagerFactory().getRedisExecutor(), taskExecutor);
    }

    public ClusterTaskDealer(RedisExecutor redisExecutor,
                             ExecutorService taskExecutor) {
        this.taskExecutor = taskExecutor;
        this.redisExecutor = redisExecutor;
    }


    /**
     * 注册任务
     * v1.1.0新增
     *
     * @param taskId         任务id
     * @param concurrentNums 任务线程数
     * @param subscriber     订阅者
     */
    public void registeredTask(String taskId, int concurrentNums, ClusterTaskSubscriber subscriber) {

        List<String> keys = new ArrayList<>();
        String taskRedisKey = RedisKeyUtils.generatedSafeKey(CLUSTER_TASK_PRE_KEY, taskId, null);
        keys.add(taskRedisKey);
        List<String> args = new ArrayList<>();
        args.add(REGISTERED_LABEL);

        long time = System.currentTimeMillis();
        try {
            // 执行lua脚本注册任务 v1.1.0
            args.add("1");
            redisExecutor.execute(REGISTERED_LUA_SCRIPT, keys, args);
            this.distributionTask(taskId, concurrentNums, subscriber);
            log.info("【集群任务】任务taskId:{},本机执行完毕,本机耗时:{}秒", taskId, (System.currentTimeMillis() - time) / 1000);
        } finally {
            args.remove(1);
            args.add("-1");
            // 执行lua脚本获取当前剩余注册数
            Object RemainingNum = redisExecutor.execute(REGISTERED_LUA_SCRIPT, keys, args);
            if ((long) RemainingNum == 0) {
                log.info("【集群任务】任务taskId:{},所有服务器执行完毕", taskId);
                // 清除任务(兼容高低spring版本)
                redisExecutor.del(taskRedisKey);
                // 执行任务完成回调
                subscriber.afterAllTask();
            }
        }

    }

    /**
     * 分配任务
     */
    private void distributionTask(String taskId, int concurrentNums, ClusterTaskSubscriber subscriber) {

        // 重置活动状态
        subscriber.resetActive();
        // 任务redis key
        String taskRedisKey = RedisKeyUtils.generatedSafeKey(CLUSTER_TASK_PRE_KEY, taskId, null);
        // redis中获取任务信息
        TaskInfo taskInfo = getTaskInfo(taskRedisKey);

        if (!taskInfo.isValid()) {
            log.info("【集群任务】无需执行任务,所有分页已被其他节点消费");
            return;
        }

        // 统计线程执行
        CountDownLatch taskCountDownLatch = new CountDownLatch(concurrentNums);

        List<Callable<String>> tasks = new ArrayList<>();
        for (int i = 0; i < concurrentNums; i++) {
            Callable<String> task = () -> {
                try {
                    LimitResult limitResult;
                    while (subscriber.isActive() && (limitResult = getLimit(taskInfo, taskRedisKey)).isSuccess()) {
                        Limit limit = limitResult.getResult();
                        log.info("【集群任务】开始执行集群任务,ID:'{}'，数据总数:{},limit:{{},{}}",
                                taskId, taskInfo.getDataNums(), limit.getNum(), limit.getSize());
                        try {
                            subscriber.execute(taskInfo, limit);
                        } catch (Exception e) {
                            log.error("【集群任务】执行线程任务,ID:'{}'，limit:{{},{}}异常:{}",
                                    taskId, limit.getNum(), limit.getSize(), e);
                            subscriber.error(e);
                        }
                    }
                    // all tasks finished or stop
                    subscriber.stop();
                } catch (Throwable t) {
                    log.error("【集群任务】任务线程执行异常", t);
                } finally {
                    // count down信号量
                    taskCountDownLatch.countDown();
                }
                return "success";
            };
            tasks.add(task);
        }

        subscriber.before();
        try {
            // 开始执行线程
            taskExecutor.invokeAll(tasks);
        } catch (InterruptedException e) {
            log.error("【集群任务】集群任务线程中断", e);
        }

        try {
            // 主线程等待任务结束
            taskCountDownLatch.await();
        } catch (Exception e) {
            log.error("【集群任务】主线程等待任务执行报错", e);
        }

    }

    /**
     * 获取任务信息
     *
     * @param taskRedisKey 任务key
     * @return 任务信息实体
     */
    private TaskInfo getTaskInfo(String taskRedisKey) {
        Map<String, String> taskInfo = redisExecutor.hgetall(taskRedisKey);
        return new TaskInfo(taskInfo);
    }

    /**
     * 获取当前线程任务处理数分页limit
     *
     * @param taskInfo 任务信息
     * @return 分页实体
     */
    private LimitResult getLimit(TaskInfo taskInfo, String taskRedisKey) {
        // 分页步长
        Integer stepSize = taskInfo.getStepSize();
        Integer dataNums = taskInfo.getDataNums();
        List<String> keys = new ArrayList<>();
        keys.add(taskRedisKey);
        long stepCount;
        try {
            // 执行lua脚本获取当前步长
            Object executeResult = redisExecutor.execute(ADD_STEP_LUA_SCRIPT, keys, Collections.singletonList("stepCount"));
            if (executeResult == null) {
                // 已经被其他线程删除
                return LimitResult.completed();
            }
            stepCount = (long) executeResult;
        } catch (Exception e) {
            log.error("【集群任务】执行lua脚本异常", e);
            return LimitResult.completed();
        }
        // 分页开始数
        int startNum = (int) (stepCount * stepSize);
        boolean last = false;
        if (startNum >= dataNums) {
            // 已经执行完毕
            return LimitResult.completed();
        } else if (startNum + stepSize >= dataNums) {
            // 最后一步
            stepSize = dataNums - startNum;
            last = true;
        } else {
            // 延长过期时间
            extendedExpire(taskRedisKey);
        }

        if (taskInfo.isDesc()) {
            // 倒序
            startNum = dataNums - startNum - stepSize;
        }
        return LimitResult.newLimit(Limit.create(startNum, stepSize), last);
    }

    /**
     * 延长key过期时间
     *
     * @param key 任务key
     */
    private void extendedExpire(String key) {
        redisExecutor.expire(key, TaskConfig.KEY_EXPIRE_SECONDS, TimeUnit.SECONDS);
    }

}
