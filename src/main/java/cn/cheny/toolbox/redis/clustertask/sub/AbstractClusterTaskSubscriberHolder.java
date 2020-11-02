package cn.cheny.toolbox.redis.clustertask.sub;

import java.util.ArrayList;
import java.util.Collection;

/**
 * 集群任务订阅器holder抽象类
 *
 * @author cheney
 * @date 2019-09-03
 */
public abstract class AbstractClusterTaskSubscriberHolder {

    private ClusterTaskDealer clusterTaskDealer;

    private Collection<ClusterTaskSubscriber> subscribers;

    public AbstractClusterTaskSubscriberHolder(ClusterTaskDealer clusterTaskDealer) {
        this.clusterTaskDealer = clusterTaskDealer;
        this.subscribers = new ArrayList<>();
    }

    public AbstractClusterTaskSubscriberHolder(ClusterTaskDealer clusterTaskDealer, Collection<ClusterTaskSubscriber> subscribers) {
        this.clusterTaskDealer = clusterTaskDealer;
        this.subscribers = subscribers;
    }

    public void executeSub(String taskId, int concurrentNums) {
        for (ClusterTaskSubscriber subscriber : subscribers) {
            SubTask subTask = subscriber.getClass().getAnnotation(SubTask.class);
            if (subTask == null) {
                continue;
            }
            String subTaskId = subTask.taskId();
            if (taskId.equals(subTaskId)) {
                clusterTaskDealer.registeredTask(taskId, concurrentNums, subscriber);
            }
        }
    }

    public ClusterTaskDealer getClusterTaskDealer() {
        return clusterTaskDealer;
    }

    public void setClusterTaskDealer(ClusterTaskDealer clusterTaskDealer) {
        this.clusterTaskDealer = clusterTaskDealer;
    }

    public Collection<ClusterTaskSubscriber> getSubscribers() {
        return subscribers;
    }

    public void setSubscribers(Collection<ClusterTaskSubscriber> subscribers) {
        this.subscribers = subscribers;
    }
}
