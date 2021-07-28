package cn.cheny.toolbox.asyncTask.parallel;

import cn.cheny.toolbox.asyncTask.function.Task;
import cn.cheny.toolbox.asyncTask.function.ThreeTaskConsume;
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
public class ThreeParallel<ONE, TWO, THREE, RESULT> extends TwoParallel<ONE, TWO, RESULT> {

    private Task<THREE> three;

    private ThreeTaskConsume<ONE, TWO, THREE, RESULT> consume;

    private FutureResultHolder<THREE> threeResult;

    public ThreeParallel(ExecutorService executor) {
        super(executor == null ? Executors.newFixedThreadPool(TWO_PARALLEL_NUM) : executor);
    }

    public ThreeParallel(Task<ONE> task1, Task<TWO> task2, Task<THREE> task3, ThreeTaskConsume<ONE, TWO, THREE, RESULT> consume) {
        this(task1, task2, task3, consume, Executors.newFixedThreadPool(3));
    }

    public ThreeParallel(Task<ONE> task1, Task<TWO> task2, Task<THREE> task3, ThreeTaskConsume<ONE, TWO, THREE, RESULT> consume, ExecutorService executor) {
        super(task1, task2, null, executor);
        this.three = task3;
        this.consume = consume;
    }

    /**
     * 提交第三个异步任务
     *
     * @param task 任务
     */
    public void subThreeTask(Task<THREE> task) {
        this.three = task;
        Future<THREE> submit = getExecutor().submit(task::doTask);
        this.threeResult = new FutureResultHolder<>(submit);
    }

    /**
     * 消费异步任务结果
     *
     * @param consume 三任务消费者
     * @return 消费执行结果
     */
    public RESULT consume(ThreeTaskConsume<ONE, TWO, THREE, RESULT> consume) {
        FutureResultHolder<ONE> oneResult = getOneResult();
        FutureResultHolder<TWO> twoResult = getTwoResult();
        if (oneResult == null || twoResult == null || this.threeResult == null) {
            throw new ToolboxRuntimeException("undone one/two/three task");
        }
        this.consume = consume;
        return consume.consume(oneResult, twoResult, threeResult);
    }

    @Override
    public RESULT start() {
        this.doAsyncTask();
        return consume(consume);
    }

    @Override
    protected void doAsyncTask() {
        super.doAsyncTask();
        FutureResultHolder<THREE> twoResult = this.threeResult;
        if (twoResult == null && three != null) {
            this.subThreeTask(three);
        }
    }

}
