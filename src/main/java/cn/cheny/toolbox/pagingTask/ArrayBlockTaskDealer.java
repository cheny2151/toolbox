package cn.cheny.toolbox.pagingTask;

import cn.cheny.toolbox.other.page.ExtremumLimit;
import cn.cheny.toolbox.other.page.Limit;
import cn.cheny.toolbox.pagingTask.exception.ConcurrentTaskException;
import cn.cheny.toolbox.pagingTask.function.*;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * 使用{@link ArrayBlockingQueue}提供生产消费功能，主线程负责查询数据，线程池线程负责消费数据
 *
 * @author cheney
 * @date 2020-01-14
 */
@Slf4j
public class ArrayBlockTaskDealer {

    /**
     * 默认线程个数
     */
    private final static int DEFAULT_THREAD_NUM = 8;

    /**
     * 线程个数
     */
    private int threadNum;

    /**
     * 队列容量(默认为线程的2/3倍)
     */
    private int queueNum;

    /**
     * 结束标识
     */
    private volatile boolean finish;

    /**
     * 主线程是否帮助执行
     */
    private boolean mainHelpTask;

    public ArrayBlockTaskDealer() {
        this(DEFAULT_THREAD_NUM, false);
    }

    public ArrayBlockTaskDealer(int threadNum) {
        this(threadNum, false);
    }

    public ArrayBlockTaskDealer(int threadNum, boolean mainHelpTask) {
        this.finish = true;
        this.mainHelpTask = mainHelpTask;
        setThreadNum(threadNum);
    }

    /**
     * 执行多线程阻塞任务，主线程生产数据，异步线程消费数据
     *
     * @param countFunction    count函数
     * @param findDataFunction 数据查询函数
     * @param blockTask        任务函数
     * @param step             步长
     * @param <T>              类型
     * @throws InterruptedException ArrayBlockingQueue导致的任务中断异常
     */
    public <T> void execute(CountFunction countFunction, FindDataFunction<T> findDataFunction,
                            BlockTask<T> blockTask, int step) throws InterruptedException {
        beforeTask();
        int count = countFunction.count();
        if (count < 1) {
            return;
        }
        // 新建阻塞队列
        ArrayBlockingQueue<List<T>> queue = new ArrayBlockingQueue<>(queueNum);
        // 主线程同步获取数据传递至方法内
        MainTask mainTask = () -> putDataToQueueByLimit(findDataFunction, step, count, queue);

        startTask(mainTask, queue, blockTask);
    }

    /**
     * 执行多线程阻塞任务，主线程生产数据，异步线程消费数据
     *
     * @param countFunction    count函数
     * @param findDataFunction 数据查询函数
     * @param blockTask        任务函数
     * @param step             步长
     * @param <T>              类型
     * @throws InterruptedException ArrayBlockingQueue导致的任务中断异常
     */
    public <T extends ExtremumField<V>, V extends Comparable<V>>
    void executeOrderByExtremum(CountFunction countFunction, FindDataExtremumLimitFunction<T> findDataFunction,
                                BlockTask<T> blockTask, int step) throws InterruptedException {
        beforeTask();
        int count = countFunction.count();
        if (count < 1) {
            return;
        }
        // 新建阻塞队列
        ArrayBlockingQueue<List<T>> queue = new ArrayBlockingQueue<>(queueNum);
        // 主线程同步获取数据传递至方法内
        MainTask mainTask = () -> putDataToQueueOrderByExtremum(findDataFunction, step, count, queue);

        startTask(mainTask, queue, blockTask);
    }

    /**
     * 执行多线程阻塞任务，主线程生产数据，异步线程消费数据并返回数据
     *
     * @param countFunction       count函数
     * @param findDataFunction    数据查询函数
     * @param blockTaskWithResult 任务函数（返回数据）
     * @param step                步长
     * @param <T>                 类型
     * @throws InterruptedException ArrayBlockingQueue导致的任务中断异常
     */
    public <T, R> FutureResult<R> execute(CountFunction countFunction,
                                          FindDataFunction<T> findDataFunction,
                                          BlockTaskWithResult<T, R> blockTaskWithResult,
                                          int step) throws InterruptedException {
        beforeTask();
        int count = countFunction.count();
        if (count < 1) {
            return FutureResult.empty();
        }
        // 新建阻塞队列
        ArrayBlockingQueue<List<T>> queue = new ArrayBlockingQueue<>(queueNum);
        // 主线程同步获取数据传递至方法内
        MainTask mainTask = () -> putDataToQueueByLimit(findDataFunction, step, count, queue);

        List<Future<List<R>>> futures = startTaskWithResult(mainTask, queue, blockTaskWithResult);

        return new FutureResult<>(futures);
    }

    /**
     * 执行多线程阻塞任务，主线程生产数据，异步线程消费数据并返回数据
     *
     * @param countFunction       count函数
     * @param findDataFunction    数据查询函数
     * @param blockTaskWithResult 任务函数（返回数据）
     * @param step                步长
     * @param <T>                 类型
     * @throws InterruptedException ArrayBlockingQueue导致的任务中断异常
     */
    public <T extends ExtremumField<V>, V extends Comparable<V>, R>
    FutureResult<R> executeOrderByExtremum(CountFunction countFunction,
                                           FindDataExtremumLimitFunction<T> findDataFunction,
                                           BlockTaskWithResult<T, R> blockTaskWithResult,
                                           int step) throws InterruptedException {
        beforeTask();
        int count = countFunction.count();
        if (count < 1) {
            return FutureResult.empty();
        }
        // 新建阻塞队列
        ArrayBlockingQueue<List<T>> queue = new ArrayBlockingQueue<>(queueNum);
        // 主线程同步获取数据传递至方法内
        MainTask mainTask = () -> putDataToQueueOrderByExtremum(findDataFunction, step, count, queue);

        List<Future<List<R>>> futures = startTaskWithResult(mainTask, queue, blockTaskWithResult);

        return new FutureResult<>(futures);
    }

    /**
     * 开始任务，新建线程池并由主线程生产数据，线程池多线程执行消费数据
     *
     * @param mainTask  主线程任务
     * @param queue     队列
     * @param blockTask 线程池阻塞任务
     * @param <T>       类型
     * @throws InterruptedException 任务中断异常
     */
    private <T> void startTask(MainTask mainTask, ArrayBlockingQueue<List<T>> queue,
                               BlockTask<T> blockTask) throws InterruptedException {
        // 初始化线程池，阻塞队列
        ExecutorService executorService = Executors.newFixedThreadPool(this.threadNum);
        // 异步订阅任务
        Runnable task = createTask(queue, blockTask);
        for (int i = 0; i < this.threadNum; i++) {
            executorService.submit(task);
        }

        // 主线程同步获取数据
        mainTask.run();
        // 查看主线程帮助执行任务
        if (mainHelpTask) {
            try {
                task.run();
            } catch (Exception e) {
                log.error("主线程协助执行异常", e);
            }
        }
        // 关闭线程池
        executorService.shutdown();
    }

    /**
     * 开始任务，新建线程池并由主线程生产数据，线程池多线程执行消费数据
     *
     * @param mainTask            主线程任务
     * @param queue               队列
     * @param blockTaskWithResult 线程池阻塞任务
     * @param <T>                 类型
     * @throws InterruptedException 任务中断异常
     */
    private <T, R> List<Future<List<R>>> startTaskWithResult(MainTask mainTask, ArrayBlockingQueue<List<T>> queue,
                                                             BlockTaskWithResult<T, R> blockTaskWithResult) throws InterruptedException {
        List<Future<List<R>>> futures = new ArrayList<>();
        // 初始化线程池，阻塞队列
        ExecutorService executorService = Executors.newFixedThreadPool(this.threadNum);
        // 启动线程池调用任务
        for (int i = 0; i < this.threadNum; i++) {
            // 异步订阅任务
            Callable<List<R>> task = createTaskWithResult(queue, blockTaskWithResult);
            futures.add(executorService.submit(task));
        }

        // 主线程同步获取数据
        mainTask.run();
        // 查看主线程帮助执行任务
        if (mainHelpTask) {
            try {
                List<R> call = createTaskWithResult(queue, blockTaskWithResult).call();
                futures.add(new WrapFeature<>(call));
            } catch (Exception e) {
                log.error("主线程协助执行异常", e);
            }
        }
        // 关闭线程池
        executorService.shutdown();
        return futures;
    }

    /**
     * 创建带返回类型的callback任务
     *
     * @param queue     队列
     * @param blockTask 任务体
     * @param <T>       泛型：数据类型
     */
    private <T> Runnable createTask(ArrayBlockingQueue<List<T>> queue,
                                    BlockTask<T> blockTask) {
        return () -> {
            try {
                List<T> data;
                while ((data = queue.poll(1, TimeUnit.SECONDS)) != null || !finish) {
                    try {
                        if (data != null) {
                            blockTask.execute(data);
                        }
                    } catch (Exception e) {
                        log.error("执行任务分片异常", e);
                    }
                }
            } catch (InterruptedException e) {
                log.error("队列poll数据异常:", e);
            }
        };
    }

    /**
     * 创建带返回类型的callback任务
     *
     * @param queue               队列
     * @param blockTaskWithResult 任务体
     * @param <T>                 泛型：数据类型
     * @param <R>                 泛型：返回类型
     * @return callback任务
     */
    private <T, R> Callable<List<R>> createTaskWithResult(ArrayBlockingQueue<List<T>> queue,
                                                          BlockTaskWithResult<T, R> blockTaskWithResult) {
        List<R> rs = new ArrayList<>();
        return () -> {
            List<T> data;
            try {
                while ((data = queue.poll(1, TimeUnit.SECONDS)) != null || !finish) {
                    try {
                        if (data != null) {
                            List<R> taskResult = blockTaskWithResult.execute(data);
                            if (taskResult != null) {
                                rs.addAll(taskResult);
                            }
                        }
                    } catch (Exception e) {
                        log.error("执行任务分片异常", e);
                    }
                }
            } catch (InterruptedException e) {
                log.error("队列poll数据异常:", e);
            }
            return rs;
        };
    }

    /**
     * 将查询结果数据入队
     *
     * @param findDataFunction 查询函数
     * @param step             步长
     * @param count            总数
     * @param queue            队列
     * @param <T>              类型
     * @throws InterruptedException 任务中断异常
     */
    private <T> void putDataToQueueByLimit(FindDataFunction<T> findDataFunction,
                                           int step, int count,
                                           ArrayBlockingQueue<List<T>> queue) throws InterruptedException {
        try {
            Limit limit = Limit.create(0, step);
            while (count > 0) {
                if (count >= step) {
                    limit.setNum(count -= step);
                } else {
                    limit.setNum(0);
                    limit.setSize(count);
                    count -= step;
                }
                queue.put(findDataFunction.findData(limit));
            }
            finish = true;
        } finally {
            finish = true;
        }
    }

    /**
     * 将查询结果数据入队
     *
     * @param findDataFunction 查询函数
     * @param step             步长
     * @param count            总数
     * @param queue            队列
     * @param <T>              类型
     * @throws InterruptedException 任务中断异常
     */
    private <T extends ExtremumField<V>, V extends Comparable<V>> void
    putDataToQueueOrderByExtremum(FindDataExtremumLimitFunction<T> findDataFunction,
                                  int step, int count,
                                  ArrayBlockingQueue<List<T>> queue) throws InterruptedException {
        try {
            ExtremumLimit extremumLimit = ExtremumLimit.create(null, step, ExtremumLimit.ExtremumType.MINIMUM);
            Object extremum = null;
            while (count > 0) {
                extremumLimit.setExtremum(extremum);
                if (count < step) {
                    extremumLimit.setSize(count);
                }
                count -= step;
                List<T> data = findDataFunction.findData(extremumLimit);
                Optional<T> max = data.parallelStream().max(Comparator.comparing(ExtremumField::getExtremumValue));
                extremum = max.orElseThrow(() -> new ConcurrentTaskException("data extremum not exists")).getExtremumValue();
                queue.put(data);
            }
        } finally {
            finish = true;
        }
    }

    /**
     * 任务并发安全检查
     */
    public synchronized void beforeTask() {
        if (!finish) {
            // 一个实例无法并发进行任务
            throw new ConcurrentTaskException();
        }
        finish = false;
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

    public void setMainHelpTask(boolean mainHelpTask) {
        this.mainHelpTask = mainHelpTask;
    }

    public void setThreadNum(int threadNum) {
        this.threadNum = threadNum;
        setQueueNum();
    }

    private void setQueueNum() {
        // 默认队列容量为线程数的0.75倍
        int queueNum = (int) (this.threadNum * 0.75);
        if (queueNum == 0) {
            queueNum = 1;
        }
        this.queueNum = queueNum;
    }

    /**
     * 异步任务执行结果封装
     *
     * @param <R> 返回类型
     */
    public static class FutureResult<R> {

        public FutureResult(List<Future<List<R>>> futures) {
            this.futures = futures;
        }

        private final List<Future<List<R>>> futures;

        public static <R> FutureResult<R> empty() {
            return new FutureResult<>(null);
        }

        public List<Future<List<R>>> getFutures() {
            return futures;
        }

        /**
         * 从所有Future中获取异步结果
         *
         * @return 任务返回数据
         */
        public List<R> getResults() {
            return futures == null ? Collections.emptyList() : futures.stream().map(e -> {
                try {
                    return e.get();
                } catch (Exception ex) {
                    log.error("Future#get()异常", ex);
                    return null;
                }
            }).filter(Objects::nonNull).flatMap(List::stream).collect(Collectors.toList());
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

}
