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

    private OneParallel(ExecutorService executor) {
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

    public static <ONE, RESULT> OneParallel<ONE, RESULT> build() {
        return build(null);
    }

    public static <ONE, RESULT> OneParallel<ONE, RESULT> build(ExecutorService executor) {
        return new OneParallel<>(executor);
    }

    public void subOneTask(Task<ONE> task) {
        this.one = task;
        Future<ONE> submit = executor.submit(task::doTask);
        this.oneResult = new FutureResultHolder<>(submit);
    }

    public RESULT consume(OneTaskConsume<ONE, RESULT> consume) {
        if (this.oneResult == null) {
            throw new ToolboxRuntimeException("undone one task");
        }
        this.consume = consume;
        return consume.consume(oneResult);
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
        return consume(consume);
    }
}
