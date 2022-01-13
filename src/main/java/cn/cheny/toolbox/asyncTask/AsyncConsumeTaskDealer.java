package cn.cheny.toolbox.asyncTask;

import cn.cheny.toolbox.asyncTask.exception.ConcurrentTaskException;
import cn.cheny.toolbox.asyncTask.exception.TaskInterruptedException;
import cn.cheny.toolbox.asyncTask.function.*;
import cn.cheny.toolbox.exception.ToolboxRuntimeException;
import cn.cheny.toolbox.other.NamePrefixThreadFactory;
import cn.cheny.toolbox.other.order.Orders;
import cn.cheny.toolbox.other.page.ExtremumLimit;
import cn.cheny.toolbox.other.page.Limit;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;

import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 异步多线程消费任务处理器
 * 使用{@link ArrayBlockingQueue}提供生产消费功能，主线程负责同步查询数据，线程池线程负责异步消费数据
 * 适用于生产速度比消费速度快的任务
 * 任务异常中断将抛出{@link TaskInterruptedException}
 * <p>
 * v1.1 新增任务包{@link TaskPackage}，用于对任务结果进行排序
 * v1.2 新增衍生方法foreach,map
 * v1.3 新增生产消费任务方式
 *
 * @author cheney
 * @date 2020-01-14
 */
@Slf4j
public class AsyncConsumeTaskDealer {

    /**
     * 默认数据排序类型
     */
    private final static Orders.OrderType DEFAULT_SORT_TYPE = Orders.OrderType.asc;

    /**
     * 默认线程个数
     */
    private final static int DEFAULT_THREAD_NUM = 8;

    /**
     * 默认任务线程名
     */
    private final static String DEFAULT_THREAD_NAME = "AsyncConsumeTaskDealer";

    /**
     * 线程个数
     */
    private int threadNum;

    /**
     * 队列容量(默认为线程的2倍),队列容量越大，
     * 生产者可以生产更多的数据存放于缓存中，
     * 意味着主线程越容易出逃
     */
    private int queueNum;

    /**
     * 任务线程名
     */
    private String threadName;

    /**
     * 主线程是否帮助执行
     */
    private boolean mainHelpTask;

    /**
     * 分片异常是否继续执行
     */
    protected boolean continueWhenSliceTaskError;

    /**
     * 数据读取顺序
     */
    private Orders.OrderType orderType;

    @Data
    static class TaskState {

        /**
         * 任务排序
         */
        private final Orders.OrderType orderType;

        /**
         * 任务线程数
         */
        private int threadNum;

        /**
         * 结束标识
         */
        private volatile boolean finishCreated;

        /**
         * 中断标识
         */
        protected volatile boolean interrupted;

        /**
         * 中断原因
         */
        protected Throwable interruptedCause;

        public TaskState(Orders.OrderType orderType) {
            this.orderType = orderType;
            this.finishCreated = false;
            this.interrupted = false;
        }

        public void setInterruptedCause(Throwable interruptedCause) {
            this.setInterrupted(true);
            this.interruptedCause = interruptedCause;
        }
    }

    public AsyncConsumeTaskDealer() {
        this(DEFAULT_THREAD_NUM, false, false);
    }

    public AsyncConsumeTaskDealer(int threadNum) {
        this(threadNum, false, false);
    }

    public AsyncConsumeTaskDealer(int threadNum, boolean mainHelpTask) {
        this(threadNum, mainHelpTask, false);
    }

    public AsyncConsumeTaskDealer(int threadNum, boolean mainHelpTask, boolean continueWhenSliceTaskError) {
        this.mainHelpTask = mainHelpTask;
        this.continueWhenSliceTaskError = continueWhenSliceTaskError;
        this.threadName = DEFAULT_THREAD_NAME;
        this.orderType = DEFAULT_SORT_TYPE;
        threadNum(threadNum);
    }

    /**
     * 执行多线程阻塞任务，主线程生产数据，异步线程消费数据
     *
     * @param countFunction    count函数
     * @param findDataFunction 数据查询函数
     * @param asyncTask        任务函数
     * @param step             步长
     * @param <T>              类型
     */
    public <T> void execute(CountFunction countFunction, FindDataFunction<T> findDataFunction,
                            AsyncTask<T> asyncTask, int step) {
        TaskState taskState = beforeTask();
        int count = countFunction.count();
        if (count < 1) {
            return;
        }
        // 新建阻塞队列
        ArrayBlockingQueue<TaskPackage<List<T>>> queue = new ArrayBlockingQueue<>(queueNum);
        // 主线程同步获取数据传递至方法内
        MainTask mainTask = () -> putDataToQueueByLimit(findDataFunction, step, count, queue, taskState);

        startTask(mainTask, queue, asyncTask, taskState);
    }

    /**
     * 执行多线程阻塞任务，主线程生产数据，异步线程消费数据
     *
     * @param countFunction    count函数
     * @param findDataFunction 数据查询函数
     * @param asyncTask        任务函数
     * @param step             步长
     * @param <T>              类型
     */
    public <T extends ExtremumField<V>, V extends Comparable<V>>
    void executeOrderByExtremum(CountFunction countFunction, FindDataExtremumLimitFunction<T> findDataFunction,
                                AsyncTask<T> asyncTask, int step) {
        TaskState taskState = beforeTask();
        int count = countFunction.count();
        if (count < 1) {
            return;
        }
        // 新建阻塞队列
        ArrayBlockingQueue<TaskPackage<List<T>>> queue = new ArrayBlockingQueue<>(queueNum);
        // 主线程同步获取数据传递至方法内
        MainTask mainTask = () -> putDataToQueueOrderByExtremum(findDataFunction, step, count, queue, taskState);

        startTask(mainTask, queue, asyncTask, taskState);
    }

    /**
     * 执行生产消费任务
     * v1.3
     *
     * @param producer  生产任务
     * @param asyncTask 异步消费任务
     * @param <T>       数据类型
     */
    public <T> void execute(Producer<T> producer, AsyncTask<T> asyncTask) {
        TaskState taskState = beforeTask();
        // 新建阻塞队列
        ArrayBlockingQueue<TaskPackage<List<T>>> queue = new ArrayBlockingQueue<>(queueNum);
        // 主线程同步获取数据传递至方法内
        TaskPublish<T> taskPublish = new TaskPublish<>(queue, taskState);
        MainTask mainTask = () -> doProduce(producer, taskPublish, taskState);

        startTask(mainTask, queue, asyncTask, taskState);
    }

    /**
     * 衍生方法，切割集合任务进行执行
     *
     * @param data      集合数据
     * @param splitSize 任务包分片大小
     * @param asyncTask 任务函数
     * @param <T>       数据泛型
     */
    public <T> void foreach(List<T> data, int splitSize, AsyncTask<T> asyncTask) {
        if (data.size() <= splitSize) {
            asyncTask.execute(data);
            return;
        }
        this.execute(data::size, this.listFindDataFunction(data), asyncTask, splitSize);
    }

    /**
     * 执行多线程阻塞任务，主线程生产数据，异步线程消费数据并返回数据
     *
     * @param countFunction       count函数
     * @param findDataFunction    数据查询函数
     * @param asyncTaskWithResult 任务函数（返回数据）
     * @param step                步长
     * @param <T>                 类型
     */
    public <T, R> FutureResult<R> submit(CountFunction countFunction,
                                         FindDataFunction<T> findDataFunction,
                                         AsyncTaskWithResult<T, R> asyncTaskWithResult,
                                         int step) {
        TaskState taskState = beforeTask();
        int count = countFunction.count();
        if (count < 1) {
            return FutureResult.empty();
        }
        // 新建阻塞队列
        ArrayBlockingQueue<TaskPackage<List<T>>> queue = new ArrayBlockingQueue<>(queueNum);
        // 主线程同步获取数据传递至方法内
        MainTask mainTask = () -> putDataToQueueByLimit(findDataFunction, step, count, queue, taskState);

        return startTaskWithResult(mainTask, queue, asyncTaskWithResult, taskState);
    }

    /**
     * 执行多线程阻塞任务，主线程生产数据，异步线程消费数据并返回数据
     *
     * @param countFunction       count函数
     * @param findDataFunction    数据查询函数
     * @param asyncTaskWithResult 任务函数（返回数据）
     * @param step                步长
     * @param <T>                 类型
     */
    public <T extends ExtremumField<V>, V extends Comparable<V>, R>
    FutureResult<R> submitOrderByExtremum(CountFunction countFunction,
                                          FindDataExtremumLimitFunction<T> findDataFunction,
                                          AsyncTaskWithResult<T, R> asyncTaskWithResult,
                                          int step) {
        TaskState taskState = beforeTask();
        int count = countFunction.count();
        if (count < 1) {
            return FutureResult.empty();
        }
        // 新建阻塞队列
        ArrayBlockingQueue<TaskPackage<List<T>>> queue = new ArrayBlockingQueue<>(queueNum);
        // 主线程同步获取数据传递至方法内
        MainTask mainTask = () -> putDataToQueueOrderByExtremum(findDataFunction, step, count, queue, taskState);

        return startTaskWithResult(mainTask, queue, asyncTaskWithResult, taskState);
    }

    /**
     * 执行生产消费任务
     * v1.3
     *
     * @param producer            生产任务
     * @param asyncTaskWithResult 异步消费任务函数（返回数据）
     * @param <T>                 数据类型
     */
    public <T, R> FutureResult<R> submit(Producer<T> producer, AsyncTaskWithResult<T, R> asyncTaskWithResult) {
        TaskState taskState = beforeTask();
        // 新建阻塞队列
        ArrayBlockingQueue<TaskPackage<List<T>>> queue = new ArrayBlockingQueue<>(queueNum);
        TaskPublish<T> taskPublish = new TaskPublish<>(queue, taskState);
        // 主线程同步获取数据传递至方法内
        MainTask mainTask = () -> doProduce(producer, taskPublish, taskState);

        return startTaskWithResult(mainTask, queue, asyncTaskWithResult, taskState);
    }


    /**
     * 衍生方法，切割集合任务进行执行
     *
     * @param data                集合数据
     * @param splitSize           任务包分片大小
     * @param asyncTaskWithResult 任务函数（返回数据）
     * @param <T>                 数据泛型
     */
    public <T, R> FutureResult<R> map(List<T> data, int splitSize, AsyncTaskWithResult<T, R> asyncTaskWithResult) {
        if (data.size() <= splitSize) {
            List<R> rs = asyncTaskWithResult.execute(data);
            WrapFeature<List<TaskPackage<List<R>>>> wrapFeature = new WrapFeature<>(Collections.singletonList(new TaskPackage<>(rs, 0)));
            return new FutureResult<>(Collections.singletonList(wrapFeature));
        }
        return this.submit(data::size, this.listFindDataFunction(data), asyncTaskWithResult, splitSize);
    }

    /**
     * 开始任务，新建线程池并由主线程生产数据，线程池多线程执行消费数据
     *
     * @param mainTask  主线程任务
     * @param queue     队列
     * @param asyncTask 线程池阻塞任务
     * @param <T>       类型
     */
    private <T> void startTask(MainTask mainTask, ArrayBlockingQueue<TaskPackage<List<T>>> queue,
                               AsyncTask<T> asyncTask, TaskState taskState) {
        // 初始化线程池，阻塞队列
        int threadNum = this.getThreadNum();
        taskState.setThreadNum(threadNum);
        ExecutorService executorService = createExecutorService(threadNum);
        this.startTask(mainTask, queue, asyncTask, executorService, taskState);
    }

    /**
     * 开始任务，创建线程池并由主线程生产数据，线程池多线程执行消费数据
     *
     * @param mainTask        主线程任务
     * @param queue           队列
     * @param asyncTask       线程池阻塞任务
     * @param executorService 异步任务线程池
     * @param <T>             类型
     */
    protected <T> void startTask(MainTask mainTask, ArrayBlockingQueue<TaskPackage<List<T>>> queue,
                                 AsyncTask<T> asyncTask, ExecutorService executorService, TaskState taskState) {
        // 异步订阅任务
        Runnable task = createTask(queue, asyncTask, taskState);
        int threadNum = taskState.getThreadNum();
        for (int i = 0; i < threadNum; i++) {
            executorService.submit(task);
        }

        // 主线程同步获取数据
        try {
            mainTask.run();
        } catch (InterruptedException e) {
            throw new TaskInterruptedException("主线程任务获取中断", e);
        }
        // 查看主线程帮助执行任务
        if (mainHelpTask) {
            try {
                task.run();
            } catch (Exception e) {
                log.error("主线程协助执行异常", e);
            }
        }
        // 关闭线程池
        afterTask(executorService);
        if (taskState.isInterrupted()) {
            Throwable interruptedCause = taskState.getInterruptedCause();
            throw new TaskInterruptedException("任务运行异常终止" + interruptedCause.getMessage(), interruptedCause);
        }
    }

    /**
     * 开始任务，新建线程池并由主线程生产数据，线程池多线程执行消费数据
     *
     * @param mainTask            主线程任务
     * @param queue               队列
     * @param asyncTaskWithResult 线程池阻塞任务
     * @param <T>                 类型
     */
    private <T, R> FutureResult<R> startTaskWithResult(MainTask mainTask, ArrayBlockingQueue<TaskPackage<List<T>>> queue,
                                                       AsyncTaskWithResult<T, R> asyncTaskWithResult, TaskState taskState) {
        // 初始化线程池，阻塞队列
        int threadNum = this.getThreadNum();
        taskState.setThreadNum(threadNum);
        ExecutorService executorService = createExecutorService(threadNum);
        return startTaskWithResult(mainTask, queue, asyncTaskWithResult, executorService, taskState);
    }

    /**
     * 开始任务，新建线程池并由主线程生产数据，线程池多线程执行消费数据
     *
     * @param mainTask            主线程任务
     * @param queue               队列
     * @param asyncTaskWithResult 线程池阻塞任务
     * @param executorService     异步任务线程池
     * @param <T>                 类型
     */
    protected <T, R> FutureResult<R> startTaskWithResult(MainTask mainTask, ArrayBlockingQueue<TaskPackage<List<T>>> queue,
                                                         AsyncTaskWithResult<T, R> asyncTaskWithResult,
                                                         ExecutorService executorService, TaskState taskState) {
        List<Future<List<TaskPackage<List<R>>>>> futures = new ArrayList<>();
        // 启动线程池调用任务
        Callable<List<TaskPackage<List<R>>>> task = createTaskWithResult(queue, asyncTaskWithResult, taskState);
        int threadNum = taskState.getThreadNum();
        for (int i = 0; i < threadNum; i++) {
            // 异步订阅任务
            futures.add(executorService.submit(task));
        }

        // 主线程同步获取数据
        try {
            mainTask.run();
        } catch (InterruptedException e) {
            throw new TaskInterruptedException("主线程任务获取中断", e);
        }
        // 查看主线程帮助执行任务
        if (mainHelpTask) {
            try {
                List<TaskPackage<List<R>>> call = task.call();
                futures.add(new WrapFeature<>(call));
            } catch (Exception e) {
                log.error("主线程协助执行异常", e);
            }
        }
        // 关闭线程池
        afterTask(executorService);
        if (taskState.isInterrupted()) {
            Throwable interruptedCause = taskState.getInterruptedCause();
            throw new TaskInterruptedException("任务运行异常终止:" + interruptedCause.getMessage(), interruptedCause);
        }
        return new FutureResult<>(futures);
    }

    /**
     * 执行生产数据的任务
     *
     * @param producer    生产者
     * @param taskPublish 任务数据推送
     * @param <T>         数据类型
     */
    protected <T> void doProduce(Producer<T> producer, TaskPublish<T> taskPublish, TaskState taskState) {
        try {
            producer.produce(taskPublish);
        } catch (InterruptedException e) {
            // do nothing
        } catch (Exception e) {
            if (continueWhenSliceTaskError) {
                log.error("执行生产任务异常,任务继续", e);
            } else {
                taskState.setInterruptedCause(e);
            }
        } finally {
            try {
                // 保证消费者获取到数据后才退出
                Thread.sleep(2);
            } catch (InterruptedException e) {
                // do thing
            }
            taskState.setFinishCreated(true);
        }
    }

    /**
     * 创建/获取线程池，当非一次性时缓存线程池
     *
     * @return 线程池
     */
    protected ExecutorService createExecutorService(int threadNum) {
        return Executors.newFixedThreadPool(threadNum, new NamePrefixThreadFactory(this.threadName));
    }

    /**
     * 创建带返回类型的callback任务
     *
     * @param <T>       泛型：数据类型
     * @param queue     队列
     * @param asyncTask 任务体
     * @param taskState 任务状态
     */
    private <T> Runnable createTask(ArrayBlockingQueue<TaskPackage<List<T>>> queue,
                                    AsyncTask<T> asyncTask, TaskState taskState) {
        return () -> {
            try {
                TaskPackage<List<T>> taskPackage;
                while ((taskPackage = queue.poll(2, TimeUnit.MILLISECONDS)) != null || !taskState.isFinishCreated()) {
                    if (taskState.isInterrupted()) {
                        continue;
                    }
                    try {
                        if (taskPackage != null && taskPackage.getData() != null) {
                            asyncTask.execute(taskPackage.getData());
                        }
                    } catch (Throwable e) {
                        if (continueWhenSliceTaskError) {
                            log.error("执行任务分片异常,任务继续", e);
                        } else {
                            taskState.setInterruptedCause(e);
                        }
                    }
                }
            } catch (InterruptedException e) {
                throw new ToolboxRuntimeException("队列poll数据异常", e);
            }
        };
    }

    /**
     * 创建带返回类型的callback任务
     *
     * @param <T>                 泛型：数据类型
     * @param <R>                 泛型：返回类型
     * @param queue               队列
     * @param asyncTaskWithResult 任务体
     * @param taskState           任务状态
     * @return callback任务
     */
    private <T, R> Callable<List<TaskPackage<List<R>>>> createTaskWithResult(ArrayBlockingQueue<TaskPackage<List<T>>> queue,
                                                                             AsyncTaskWithResult<T, R> asyncTaskWithResult,
                                                                             TaskState taskState) {
        return () -> {
            List<TaskPackage<List<R>>> rs = new ArrayList<>();
            TaskPackage<List<T>> taskPackage;
            try {
                while ((taskPackage = queue.poll(2, TimeUnit.MILLISECONDS)) != null || !taskState.isFinishCreated()) {
                    if (taskState.isInterrupted()) {
                        continue;
                    }
                    try {
                        if (taskPackage != null && taskPackage.getData() != null) {
                            List<R> taskResult = asyncTaskWithResult.execute(taskPackage.getData());
                            if (taskResult != null) {
                                rs.add(new TaskPackage<>(taskResult, taskPackage.getIndex()));
                            }
                        }
                    } catch (Throwable e) {
                        if (continueWhenSliceTaskError) {
                            log.error("执行任务分片异常,任务继续", e);
                        } else {
                            taskState.setInterruptedCause(e);
                        }
                        rs.add(new TaskPackage<>(null, taskPackage.getIndex(), !continueWhenSliceTaskError, e));
                    }
                }
            } catch (InterruptedException e) {
                throw new ToolboxRuntimeException("队列poll数据异常", e);
            }
            return rs;
        };
    }

    /**
     * 将查询结果数据入队
     *
     * @param <T>              类型
     * @param findDataFunction 查询函数
     * @param step             步长
     * @param count            总数
     * @param queue            队列
     * @param taskState        任务状态
     * @throws InterruptedException 任务中断异常
     */
    private <T> void putDataToQueueByLimit(FindDataFunction<T> findDataFunction,
                                           int step, int count,
                                           ArrayBlockingQueue<TaskPackage<List<T>>> queue, TaskState taskState) throws InterruptedException {
        // 只有主线程跑，无原子性问题
        int index = 0;
        try {
            Limit limit = Limit.create(0, step);
            boolean asc = orderType.equals(Orders.OrderType.asc);
            int size = step;
            int amount = count;
            int curNum = asc ? -1 * step : count;
            int add = asc ? step : step * -1;
            while (amount > 0 && !taskState.isInterrupted()) {
                if (amount < step) {
                    size = amount;
                    limit.setSize(size);
                }
                curNum += add;
                if (curNum < 0) {
                    curNum = 0;
                }
                amount -= size;
                limit.setNum(curNum);
                List<T> data = findDataFunction.findData(limit);
                TaskPackage<List<T>> taskPackage = new TaskPackage<>(data, index++);
                queue.put(taskPackage);
            }
        } finally {
            // 保证消费者获取到数据后才退出
            Thread.sleep(2);
            taskState.setFinishCreated(true);
        }
    }

    /**
     * 将查询结果数据入队
     *
     * @param <T>              类型
     * @param findDataFunction 查询函数
     * @param step             步长
     * @param count            总数
     * @param queue            队列
     * @param taskState        任务状态
     * @throws InterruptedException 任务中断异常
     */
    private <T extends ExtremumField<V>, V extends Comparable<V>> void
    putDataToQueueOrderByExtremum(FindDataExtremumLimitFunction<T> findDataFunction,
                                  int step, int count,
                                  ArrayBlockingQueue<TaskPackage<List<T>>> queue, TaskState taskState) throws InterruptedException {
        int index = 0;
        try {
            ExtremumLimit extremumLimit = ExtremumLimit.create(null, step, ExtremumLimit.ExtremumType.MINIMUM);
            Object extremum = null;
            while (count > 0) {
                if (taskState.isInterrupted()) {
                    break;
                }
                extremumLimit.setExtremum(extremum);
                if (count < step) {
                    extremumLimit.setSize(count);
                }
                count -= step;
                List<T> data = findDataFunction.findData(extremumLimit);
                Optional<T> max = data.parallelStream().max(Comparator.comparing(ExtremumField::getExtremumValue));
                extremum = max.orElseThrow(() -> new ConcurrentTaskException("data extremum not exists")).getExtremumValue();
                TaskPackage<List<T>> taskPackage = new TaskPackage<>(data, index++);
                queue.put(taskPackage);
            }
        } finally {
            // 保证消费者获取到数据后才退出
            Thread.sleep(2);
            taskState.setFinishCreated(true);
        }
    }

    private <T> FindDataFunction<T> listFindDataFunction(List<T> data) {
        return limit -> {
            int num = limit.getNum();
            int size = limit.getSize();
            List<T> splitData = new ArrayList<>();
            for (int i = 0; i < size; i++) {
                splitData.add(data.get(num++));
            }
            return splitData;
        };
    }

    protected TaskState beforeTask() {
        return new TaskState(this.orderType);
    }

    protected void afterTask(ExecutorService executorService) {
        executorService.shutdown();
    }

    public int getThreadNum() {
        return threadNum;
    }

    public int getQueueNum() {
        return queueNum;
    }

    public boolean isMainHelpTask() {
        return mainHelpTask;
    }

    public String getThreadName() {
        return threadName;
    }

    public AsyncConsumeTaskDealer threadName(String threadName) {
        this.threadName = threadName;
        return this;
    }

    public AsyncConsumeTaskDealer threadNum(int threadNum) {
        this.threadNum = threadNum;
        setDefaultQueueNum();
        return this;
    }

    public AsyncConsumeTaskDealer queueNum(int queueNum) {
        this.queueNum = queueNum;
        return this;
    }

    public AsyncConsumeTaskDealer mainHelpTask(boolean mainHelpTask) {
        this.mainHelpTask = mainHelpTask;
        return this;
    }

    public AsyncConsumeTaskDealer continueWhenSliceTaskError(boolean continueWhenSliceTaskError) {
        this.continueWhenSliceTaskError = continueWhenSliceTaskError;
        return this;
    }

    public AsyncConsumeTaskDealer orderType(Orders.OrderType type) {
        this.orderType = type;
        return this;
    }

    public Orders.OrderType getOrderType() {
        return orderType;
    }

    private void setDefaultQueueNum() {
        // 默认队列容量为线程数的2倍
        this.queueNum = this.threadNum * 2;
    }

    /**
     * 异步任务执行结果封装
     *
     * @param <R> 返回类型
     */
    public static class FutureResult<R> {

        public FutureResult(List<Future<List<TaskPackage<List<R>>>>> futures) {
            this.futures = futures;
        }

        private final List<Future<List<TaskPackage<List<R>>>>> futures;

        public static <R> FutureResult<R> empty() {
            return new FutureResult<>(null);
        }

        public List<Future<List<TaskPackage<List<R>>>>> getFutures() {
            return futures;
        }

        /**
         * 从所有Future中获取异步结果
         *
         * @return 任务返回数据
         */
        public List<R> getResults() {
            return futures == null ? Collections.emptyList() : futures.stream().flatMap(e -> {
                        try {
                            // 获取taskPackage的stream
                            return e.get().stream();
                        } catch (Exception ex) {
                            throw new ToolboxRuntimeException("Fail to do Future#get()", ex);
                        }
                    }).sorted(Comparator.comparingInt(TaskPackage::getIndex))
                    .flatMap(taskPackage -> {
                        if (taskPackage.isInterrupted()) {
                            Throwable error = taskPackage.getError();
                            throw new TaskInterruptedException("任务运行异常终止:" + error.getMessage(), error);
                        }
                        List<R> data = taskPackage.getData();
                        return data == null ? Stream.empty() : data.stream();
                    }).collect(Collectors.toList());
        }
    }

    public interface MainTask {
        void run() throws InterruptedException;
    }

    public static class WrapFeature<V> implements Future<V> {

        private V data;

        public WrapFeature(V data) {
            this.data = data;
        }

        @Override
        public boolean cancel(boolean mayInterruptIfRunning) {
            return false;
        }

        @Override
        public boolean isCancelled() {
            return false;
        }

        @Override
        public boolean isDone() {
            return true;
        }

        @Override
        public V get() throws InterruptedException, ExecutionException {
            return data;
        }

        @Override
        public V get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
            return data;
        }
    }

    /**
     * 任务数据发布接口
     *
     * @author cheney
     * @date 2021-06-06
     */
    public class TaskPublish<T> implements Publish<T> {

        private final ArrayBlockingQueue<TaskPackage<List<T>>> queue;

        private final TaskState taskState;

        private int index;

        public TaskPublish(ArrayBlockingQueue<TaskPackage<List<T>>> queue, TaskState taskState) {
            this.queue = queue;
            this.index = 0;
            this.taskState = taskState;
        }

        public void push(T t) throws InterruptedException {
            if (taskState.isInterrupted()) {
                throw new InterruptedException();
            }
            if (t == null) {
                return;
            }
            TaskPackage<List<T>> taskPackage = new TaskPackage<>(Collections.singletonList(t), index++);
            queue.put(taskPackage);
        }

        public void pushMulti(List<T> list) throws InterruptedException {
            if (taskState.isInterrupted()) {
                throw new InterruptedException();
            }
            if (CollectionUtils.isEmpty(list)) {
                return;
            }
            TaskPackage<List<T>> taskPackage = new TaskPackage<>(list, index++);
            queue.put(taskPackage);
        }

    }

    @Data
    public static class TaskPackage<DT> {
        private DT data;
        private int index;
        private boolean interrupted;
        private Throwable error;

        public TaskPackage(DT data, int index) {
            this.data = data;
            this.index = index;
            this.interrupted = false;
            this.error = null;
        }

        public TaskPackage(DT data, int index, boolean interrupted, Throwable error) {
            this.data = data;
            this.index = index;
            this.interrupted = interrupted;
            this.error = error;
        }
    }

}
