package cn.cheny.toolbox.redis.clustertask.sub;

import cn.cheny.toolbox.redis.RedisKeyUtils;
import lombok.extern.slf4j.Slf4j;
import redis.clients.jedis.JedisPubSub;

/**
 * redis集群任务订阅器
 *
 * @author cheney
 * @date 2019-09-03
 */
@Slf4j
public class JedisClusterTaskRedisSub extends JedisPubSub {

    private final JedisClusterTaskSubscriberHolder clusterTaskSubscriberHolder;

    public JedisClusterTaskRedisSub(JedisClusterTaskSubscriberHolder clusterTaskSubscriberHolder) {
        this.clusterTaskSubscriberHolder = clusterTaskSubscriberHolder;
    }

    @Override
    public void onPMessage(String pattern, String channel, String message) {
        String taskId = RedisKeyUtils.splitKeyCore(channel);
        log.info("订阅集群任务,taskId:{}", taskId);
        clusterTaskSubscriberHolder.executeSub(taskId, Integer.parseInt(message));
    }

}
