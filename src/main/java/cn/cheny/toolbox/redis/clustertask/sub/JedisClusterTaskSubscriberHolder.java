package cn.cheny.toolbox.redis.clustertask.sub;

import java.util.Collection;

/**
 * 集群任务订阅器holder
 *
 * @author cheney
 * @date 2019-09-03
 */
public class JedisClusterTaskSubscriberHolder extends AbstractClusterTaskSubscriberHolder {

    public JedisClusterTaskSubscriberHolder(ClusterTaskDealer clusterTaskDealer, Collection<ClusterTaskSubscriber> subscribers) {
        super(clusterTaskDealer, subscribers);
    }
}
