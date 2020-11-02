package cn.cheny.toolbox.redis.clustertask.pub;


import java.util.Map;

/**
 * 集群任务发布
 *
 * @author cheney
 * @date 2019-09-03
 */
public interface ClusterTaskPublisher {

    /**
     * 集群任务redis订阅发布channel
     */
    public final String CLUSTER_TASK_CHANNEL_PRE_KEY = "TOOLBOX:CLUSTER_TASK_CHANNEL:";

    /**
     * 集群任务redis任务信息key
     */
    public final String CLUSTER_TASK_PRE_KEY = "TOOLBOX:CLUSTER_TASK:";

    /**
     * 发布集群任务
     *
     * @param taskId         任务ID
     * @param dataNums       待处理数据个数
     * @param stepSize       步长
     * @param concurrentNums 单个服务器并发数量
     * @param desc           是否倒序
     */
    void publish(String taskId, int dataNums, int stepSize, int concurrentNums, boolean desc);

    /**
     * 发布集群任务
     *
     * @param taskId         任务ID
     * @param dataNums       待处理数据个数
     * @param stepSize       步长
     * @param concurrentNums 单个服务器并发数量
     * @param desc           是否倒序
     * @param header         任务头部信息
     */
    void publish(String taskId, int dataNums, int stepSize, int concurrentNums, boolean desc, Map<String, Object> header);

}
