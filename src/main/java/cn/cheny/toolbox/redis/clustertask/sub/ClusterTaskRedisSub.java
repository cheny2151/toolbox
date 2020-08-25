package cn.cheny.toolbox.redis.clustertask.sub;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;

import javax.annotation.Resource;

import java.io.ByteArrayInputStream;

import static cn.cheny.toolbox.redis.clustertask.pub.ClusterTaskPublisher.CLUSTER_TASK_CHANNEL_PRE_KEY;

/**
 * redis集群任务订阅器
 *
 * @author cheney
 * @date 2019-09-03
 */
@Slf4j
public class ClusterTaskRedisSub implements MessageListener {

    @Resource(name = "clusterTaskSubscriberHolder")
    private ClusterTaskSubscriberHolder clusterTaskSubscriberHolder;

    @Override
    public void onMessage(Message message, byte[] bytes) {
        String concurrentNums = new String(message.getBody());
        String channel = new String(message.getChannel());
        String taskId = channel.replace(CLUSTER_TASK_CHANNEL_PRE_KEY, "");
        log.info("订阅集群任务,taskId:{}", taskId);
        clusterTaskSubscriberHolder.executeSub(taskId, Integer.parseInt(concurrentNums));
    }

}
