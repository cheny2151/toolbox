package cn.cheny.toolbox.redis.clustertask.sub;

import cn.cheny.toolbox.spring.SpringUtils;

/**
 * 集群任务订阅器holder
 *
 * @author cheney
 * @date 2019-09-03
 */
public class SpringClusterTaskSubscriberHolder extends AbstractClusterTaskSubscriberHolder {

    public SpringClusterTaskSubscriberHolder(ClusterTaskDealer clusterTaskDealer) {
        super(clusterTaskDealer);
    }

    public void executeSub(String taskId, int concurrentNums) {
        setSubscribers(SpringUtils.getBeansOfType(ClusterTaskSubscriber.class));
        super.executeSub(taskId, concurrentNums);
    }

}
