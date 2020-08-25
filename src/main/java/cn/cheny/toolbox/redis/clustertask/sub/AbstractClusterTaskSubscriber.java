package cn.cheny.toolbox.redis.clustertask.sub;

import cn.cheny.toolbox.other.page.Limit;
import cn.cheny.toolbox.redis.clustertask.TaskInfo;
import lombok.extern.slf4j.Slf4j;

/**
 * 集群任务订阅抽象接口
 *
 * @author cheney
 * @date 2019-09-03
 */
@Slf4j
public abstract class AbstractClusterTaskSubscriber implements ClusterTaskSubscriber {

    /**
     * 任务激活状态
     */
    private volatile boolean active = true;

    @Override
    public void execute(TaskInfo taskInfo, Limit limit) {
        before();
        subscribe(taskInfo, limit);
        after();
    }

    @Override
    public void before() {
    }

    @Override
    public void after() {
    }

    @Override
    public void afterAllTask() {
    }

    @Override
    public void error(Throwable t) {
    }

    @Override
    public void stop() {
        this.active = false;
    }

    @Override
    public void resetActive() {
        this.active = true;
    }

    @Override
    public boolean isActive() {
        return active;
    }
}
