package cn.cheny.toolbox.asyncTask.pool2;

import cn.cheny.toolbox.asyncTask.AsyncConsumeTaskDealer;
import cn.cheny.toolbox.exception.ToolboxRuntimeException;
import cn.cheny.toolbox.other.NamePrefixThreadFactory;
import lombok.extern.slf4j.Slf4j;

import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 可池化AsyncConsumeTaskDealer改造
 *
 * @author by chenyi
 * @date 2022/1/12
 */
@Slf4j
public class AsyncConsumeTaskDealerPooled extends AsyncConsumeTaskDealer implements Closeable {

    private final AsyncConsumeTaskDealerPool belongPool;
    private final AtomicInteger state;
    private final AtomicInteger runningCount;
    /**
     * 线程池
     */
    private ExecutorService executorService;

    public AsyncConsumeTaskDealerPooled(AsyncConsumeTaskDealerPool belongPool) {
        super();
        this.belongPool = belongPool;
        this.state = new AtomicInteger(State.INACTIVATION.getVal());
        this.runningCount = new AtomicInteger(0);
    }

    @Override
    protected AsyncConsumeTaskDealer.TaskState beforeTask() {
        checkActive();
        return super.beforeTask();
    }

    @Override
    protected void afterTaskStarting(ExecutorService executorService) {
        // do nothing
    }

    @Override
    protected void afterMainRun() {
    }

    @Override
    protected void beforeAsyncRun() {
        if (runningCount.get() == 0) {
            checkActive();
        }
        runningCount.incrementAndGet();
    }

    @Override
    protected void afterAsyncRun(TaskState taskState) {
        runningCount.decrementAndGet();
    }

    private void checkActive() {
        int state = this.state.get();
        if (state != State.ACTIVATION.getVal()) {
            throw new ToolboxRuntimeException("Can not start task, abnormal state: " + State.valueOf(state).name());
        }
    }

    public int getRunningNumber() {
        return runningCount.get();
    }

    void updateState(int expire) {
        state.set(expire);
    }

    public void destroy() {
        if (this.executorService != null) {
            this.executorService.shutdown();
        }
    }

    /**
     * 创建/获取线程池，当非一次性时缓存线程池
     *
     * @param threadNum 线程数
     * @return 线程池
     */
    protected ExecutorService createExecutorService(int threadNum) {
        if (executorService != null) {
            return executorService;
        }
        ExecutorService executorService = Executors.newFixedThreadPool(threadNum, new NamePrefixThreadFactory(this.getThreadName()));
        this.executorService = executorService;
        return executorService;
    }

    @Override
    public void close() throws IOException {
        updateState(State.RETURNING.getVal());
        belongPool.returnObject(this);
    }

}
