package cn.cheny.toolbox.redis.clustertask.sub;

import cn.cheny.toolbox.other.page.Limit;
import cn.cheny.toolbox.redis.clustertask.TaskInfo;

/**
 * 集群任务订阅
 *
 * @author cheney
 * @date 2019-09-03
 */
public interface ClusterTaskSubscriber {

    /**
     * 订阅集群任务
     *
     * @param taskInfo 任务信息
     * @param limit    分页信息
     */
    void subscribe(TaskInfo taskInfo, Limit limit);

    /**
     * 执行集群任务
     *
     * @param taskInfo 任务信息
     * @param limit    分页信息
     */
    void execute(TaskInfo taskInfo, Limit limit);

    /**
     * 单个分页任务开始前执行
     */
    void before();

    /**
     * 单个分页任务执行后执行
     */
    void after();

    /**
     * 所有线程任务执行后执行
     */
    void afterAllTask();

    /**
     * error method
     *
     * @param t 异常
     */
    void error(Throwable t);

    /**
     * 主动停止任务，不保证立刻停止
     */
    void stop();

    /**
     * 重置状态值
     */
    void resetActive();

    /**
     * 是否继续执行任务
     *
     * @return boolean
     */
    boolean isActive();

}
