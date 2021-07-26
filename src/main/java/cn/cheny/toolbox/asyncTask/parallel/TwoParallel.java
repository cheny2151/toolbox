package cn.cheny.toolbox.asyncTask.parallel;

import cn.cheny.toolbox.asyncTask.function.Task;
import cn.cheny.toolbox.asyncTask.function.TwoTaskConsume;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * 处理两份并行任务
 *
 * @author by chenyi
 * @date 2021/7/26
 */
public class TwoParallel<ONE, TWO, RESULT> implements Parallel<RESULT> {

    private Task<ONE> one;
    private Task<TWO> two;

    private TwoTaskConsume<ONE, TWO, RESULT> consume;

    private ExecutorService executor;

    public TwoParallel(Task<ONE> task1, Task<TWO> task2, TwoTaskConsume<ONE, TWO, RESULT> consume) {
        this(task1, task2, consume, Executors.newFixedThreadPool(2));
    }

    public TwoParallel(Task<ONE> task1, Task<TWO> task2, TwoTaskConsume<ONE, TWO, RESULT> consume, ExecutorService executor) {
        this.one = task1;
        this.two = task2;
        this.consume = consume;
        this.executor = executor;
    }

    @Override
    public RESULT start() {
        Future<ONE> oneFuture = executor.submit(() -> one.doTask());
        Future<TWO> twoFuture = executor.submit(() -> two.doTask());
        FutureResultHolder<ONE> result1Holder = new FutureResultHolder<>(oneFuture);
        FutureResultHolder<TWO> result2Holder = new FutureResultHolder<>(twoFuture);
        return consume.consume(result1Holder, result2Holder);
    }
}
