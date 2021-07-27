package cn.cheny.toolbox.asyncTask.parallel;

import cn.cheny.toolbox.asyncTask.function.Task;
import cn.cheny.toolbox.asyncTask.function.TwoTaskConsume;
import cn.cheny.toolbox.exception.ToolboxRuntimeException;

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

    private final ExecutorService executor;

    private FutureResultHolder<ONE> oneResult;

    private FutureResultHolder<TWO> twoResult;

    public TwoParallel(ExecutorService executor) {
        this.executor = executor == null ? Executors.newFixedThreadPool(2) : executor;
    }

    public TwoParallel(Task<ONE> task1, Task<TWO> task2, TwoTaskConsume<ONE, TWO, RESULT> consume) {
        this(task1, task2, consume, Executors.newFixedThreadPool(2));
    }

    public TwoParallel(Task<ONE> task1, Task<TWO> task2, TwoTaskConsume<ONE, TWO, RESULT> consume, ExecutorService executor) {
        this.one = task1;
        this.two = task2;
        this.consume = consume;
        this.executor = executor;
    }

    public static <ONE, TWO, RESULT> TwoParallel<ONE, TWO, RESULT> build() {
        return build(null);
    }

    public static <ONE, TWO, RESULT> TwoParallel<ONE, TWO, RESULT> build(ExecutorService executor) {
        return new TwoParallel<>(executor);
    }

    public void subOneTask(Task<ONE> task) {
        this.one = task;
        Future<ONE> submit = executor.submit(task::doTask);
        this.oneResult = new FutureResultHolder<>(submit);
    }

    public void subTwoTask(Task<TWO> task) {
        this.two = task;
        Future<TWO> submit = executor.submit(task::doTask);
        this.twoResult = new FutureResultHolder<>(submit);
    }

    public RESULT consume(TwoTaskConsume<ONE, TWO, RESULT> consume) {
        if (this.oneResult == null || this.twoResult == null) {
            throw new ToolboxRuntimeException("undone one task");
        }
        this.consume = consume;
        return consume.consume(oneResult, twoResult);
    }

    @Override
    public RESULT start() {
        if (executor == null || consume == null) {
            throw new IllegalArgumentException();
        }
        FutureResultHolder<ONE> oneResult = this.oneResult;
        if (oneResult == null && one != null) {
            this.subOneTask(one);
        }
        FutureResultHolder<TWO> twoResult = this.twoResult;
        if (twoResult == null && two != null) {
            this.subTwoTask(two);
        }
        return consume(consume);
    }
}
