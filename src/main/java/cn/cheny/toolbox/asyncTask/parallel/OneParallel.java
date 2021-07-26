package cn.cheny.toolbox.asyncTask.parallel;

import cn.cheny.toolbox.asyncTask.function.OneTaskConsume;
import cn.cheny.toolbox.asyncTask.function.Task;

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

    private ExecutorService executor;

    public OneParallel(Task<ONE> task, OneTaskConsume<ONE, RESULT> consume) {
        this(task,consume, Executors.newSingleThreadExecutor());
    }

    public OneParallel(Task<ONE> task, OneTaskConsume<ONE, RESULT> consume, ExecutorService executor) {
        this.one = task;
        this.consume = consume;
        this.executor = executor;
    }

    @Override
    public RESULT start() {
        Future<ONE> oneFuture = executor.submit(() -> one.doTask());
        FutureResultHolder<ONE> resultHolder = new FutureResultHolder<>(oneFuture);
        return consume.consume(resultHolder);
    }
}
