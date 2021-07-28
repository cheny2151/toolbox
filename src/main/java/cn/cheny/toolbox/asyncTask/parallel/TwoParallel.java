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
public class TwoParallel<ONE, TWO, RESULT> extends OneParallel<ONE, RESULT> {

    public final static int TWO_PARALLEL_NUM = 2;

    private Task<TWO> two;

    private TwoTaskConsume<ONE, TWO, RESULT> consume;

    private FutureResultHolder<TWO> twoResult;

    public TwoParallel(ExecutorService executor) {
        super(executor == null ? Executors.newFixedThreadPool(TWO_PARALLEL_NUM) : executor);
    }

    public TwoParallel(Task<ONE> task1, Task<TWO> task2, TwoTaskConsume<ONE, TWO, RESULT> consume) {
        this(task1, task2, consume, Executors.newFixedThreadPool(TWO_PARALLEL_NUM));
    }

    public TwoParallel(Task<ONE> task1, Task<TWO> task2, TwoTaskConsume<ONE, TWO, RESULT> consume, ExecutorService executor) {
        super(task1, null, executor);
        this.two = task2;
        this.consume = consume;
    }

    /**
     * 提交第二个异步任务
     *
     * @param task 任务
     */
    public void subTwoTask(Task<TWO> task) {
        this.two = task;
        Future<TWO> submit = getExecutor().submit(task::doTask);
        this.twoResult = new FutureResultHolder<>(submit);
    }

    /**
     * 消费异步任务结果
     *
     * @param consume 两任务消费者
     * @return 消费执行结果
     */
    public RESULT consume2(TwoTaskConsume<ONE, TWO, RESULT> consume) {
        FutureResultHolder<ONE> oneResult = getOneResult();
        if (oneResult == null || this.twoResult == null) {
            throw new ToolboxRuntimeException("undone one/two task");
        }
        this.consume = consume;
        return consume.consume(oneResult, twoResult);
    }

    @Override
    public RESULT start() {
        this.doAsyncTask();
        return consume2(consume);
    }

    @Override
    protected void doAsyncTask() {
        super.doAsyncTask();
        FutureResultHolder<TWO> twoResult = this.twoResult;
        if (twoResult == null && two != null) {
            this.subTwoTask(two);
        }
    }

    public FutureResultHolder<TWO> getTwoResult() {
        return twoResult;
    }

}
