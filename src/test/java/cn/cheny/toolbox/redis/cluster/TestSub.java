package cn.cheny.toolbox.redis.cluster;

import cn.cheny.toolbox.other.page.Limit;
import cn.cheny.toolbox.redis.clustertask.TaskInfo;
import cn.cheny.toolbox.redis.clustertask.pub.ClusterTaskPublisher;
import cn.cheny.toolbox.redis.clustertask.sub.AbstractClusterTaskSubscriber;
import cn.cheny.toolbox.redis.clustertask.sub.SubTask;
import com.alibaba.fastjson.JSON;
import org.springframework.stereotype.Component;

import java.util.Calendar;

/**
 * 集群任务调度测试类
 * 继承AbstractClusterTaskSubscriber并存放于spring容器中
 * 再通过@SubTask指定执行的任务ID
 * <p>
 * (任务推送，调用{@link ClusterTaskPublisher#publish(String, int, int, int, boolean)})
 *
 * @author cheney
 * @date 2019-09-03
 */
@Component
@SubTask(taskId = "test")
public class TestSub extends AbstractClusterTaskSubscriber {

    @Override
    public void subscribe(TaskInfo taskInfo, Limit limit) {
        System.out.println("----------------------subscribe test----------------------");
        System.out.println("taskInfo->" + JSON.toJSONString(taskInfo));
        try {
            Thread.sleep(300);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Calendar instance = Calendar.getInstance();
        int i = instance.get(Calendar.MINUTE);
        if (i == 40) {
            System.out.println("====================invoke stop method=====================");
            stop();
        }
    }

    @Override
    public void afterAllTask() {
        System.out.println("=======================finish task=======================");
    }

}
