package cn.cheny.toolbox.asyncTask.parallel;

import cn.cheny.toolbox.asyncTask.function.OneTaskConsume;
import cn.cheny.toolbox.asyncTask.function.Task;
import cn.cheny.toolbox.exception.ToolboxRuntimeException;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * 处理一份并行任务
 *
 * @author by chenyi
 * @date 2021/7/26
 */
public class OneParallel<ONE, RESULT> implements Parallel<RESULT> {

    private Task<ONE> one;

    private OneTaskConsume<ONE, RESULT> consume;

    private final ExecutorService executor;

    private FutureResultHolder<ONE> oneResult;

    protected OneParallel(ExecutorService executor) {
        this.executor = executor == null ? Executors.newSingleThreadExecutor() : executor;
    }

    public OneParallel(Task<ONE> task, OneTaskConsume<ONE, RESULT> consume) {
        this(task, consume, Executors.newSingleThreadExecutor());
    }

    public OneParallel(Task<ONE> task, OneTaskConsume<ONE, RESULT> consume, ExecutorService executor) {
        this.one = task;
        this.consume = consume;
        this.executor = executor;
    }

    /**
     * 提交第一个异步任务
     *
     * @param task 任务
     */
    public void subOneTask(Task<ONE> task) {
        this.one = task;
        Future<ONE> submit = getExecutor().submit(task::doTask);
        this.oneResult = new FutureResultHolder<>(submit);
    }

    /**
     * 消费异步任务结果
     *
     * @param consume 一任务消费者
     * @return 消费执行结果
     */
    public RESULT consume1(OneTaskConsume<ONE, RESULT> consume) {
        FutureResultHolder<ONE> oneResult = getOneResult();
        if (oneResult == null) {
            throw new ToolboxRuntimeException("undone one task");
        }
        this.consume = consume;
        return consume.consume(oneResult);
    }

    @Override
    public RESULT start() {
        this.doAsyncTask();
        return consume1(consume);
    }

    protected void doAsyncTask() {
        if (getExecutor() == null || consume == null) {
            throw new IllegalArgumentException();
        }
        FutureResultHolder<ONE> oneResult = getOneResult();
        if (oneResult == null && one != null) {
            this.subOneTask(one);
        }
    }

    public ExecutorService getExecutor() {
        return executor;
    }

    public FutureResultHolder<ONE> getOneResult() {
        return oneResult;
    }

    @Override
    public void close() {
        if (this.executor != null) {
            this.executor.shutdownNow();
        }
    }

}
