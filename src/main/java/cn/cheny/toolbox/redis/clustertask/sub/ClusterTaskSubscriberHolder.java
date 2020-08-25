package cn.cheny.toolbox.redis.clustertask.sub;

import cn.cheny.toolbox.spring.SpringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Collection;

/**
 * 集群任务订阅器holder
 *
 * @author cheney
 * @date 2019-09-03
 */
@Component
public class ClusterTaskSubscriberHolder {

    @Resource(name = "clusterTaskDealer")
    private ClusterTaskDealer clusterTaskDealer;

    public void executeSub(String taskId, int concurrentNums) {
        Collection<ClusterTaskSubscriber> subscribers = SpringUtils.getBeansOfType(ClusterTaskSubscriber.class);
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

}
