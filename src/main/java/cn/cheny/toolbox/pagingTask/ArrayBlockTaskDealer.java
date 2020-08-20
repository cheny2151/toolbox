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
     * 默认队列容量
     */
    private final static int DEFAULT_QUEUE_CAPACITY = 4;

    /**
     * 线程个数
     */
    private int threadNum;

    /**
     * 结束标识
     */
    private volatile boolean finish;

    public ArrayBlockTaskDealer() {
        threadNum = DEFAULT_THREAD_NUM;
        finish = true;
    }

    public ArrayBlockTaskDealer(int threadNum) {
        this.threadNum = threadNum;
        finish = true;
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
    public <T> void executeBlockTask(CountFunction countFunction, FindDataFunction<T> findDataFunction,
                                     BlockTask<T> blockTask, int step) throws InterruptedException {
        beforeTask();
        int count = countFunction.count();
        if (count < 1) {
            return;
        }
        // 新建阻塞队列
        ArrayBlockingQueue<List<T>> queue = new ArrayBlockingQueue<>(DEFAULT_QUEUE_CAPACITY);
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
    void executeBlockTaskByExtremumLimit(CountFunction countFunction, FindDataExtremumLimitFunction<T> findDataFunction,
                                         BlockTask<T> blockTask, int step) throws InterruptedException {
        beforeTask();
        int count = countFunction.count();
        if (count < 1) {
            return;
        }
        // 新建阻塞队列
        ArrayBlockingQueue<List<T>> queue = new ArrayBlockingQueue<>(DEFAULT_QUEUE_CAPACITY);
        // 主线程同步获取数据传递至方法内
        MainTask mainTask = () -> putDataToQueueByExtremumLimit(findDataFunction, step, count, queue);

        startTask(mainTask, queue, blockTask);
    }

    /**
     * 开始任务，新建线程池并由主线程生产数据，线程池多线程执行消费数据
     *
     * @param mainTask  主线程任务
     * @param queue     队列
     * @param blockTask 线程池阻塞任务
     * @param <T>       类型
     * @throws InterruptedException
     */
    private <T> void startTask(MainTask mainTask, ArrayBlockingQueue<List<T>> queue,
                               BlockTask<T> blockTask) throws InterruptedException {
        // 初始化线程池，阻塞队列
        ExecutorService executorService = Executors.newFixedThreadPool(this.threadNum);
        // 异步订阅任务
        for (int i = 0; i < this.threadNum; i++) {
            executorService.submit(() -> {
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
            });
        }

        // 主线程同步获取数据
        mainTask.run();
        // 关闭线程池
        executorService.shutdown();
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
    public <T, R> FutureResult<R> executeBlockTask(CountFunction countFunction,
                                                   FindDataFunction<T> findDataFunction,
                                                   BlockTaskWithResult<T, R> blockTaskWithResult,
                                                   int step) throws InterruptedException {
        beforeTask();
        int count = countFunction.count();
        if (count < 1) {
            return FutureResult.empty();
        }
        // 新建阻塞队列
        ArrayBlockingQueue<List<T>> queue = new ArrayBlockingQueue<>(DEFAULT_QUEUE_CAPACITY);
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
    FutureResult<R> executeBlockTaskByExtremumLimit(CountFunction countFunction,
                                                    FindDataExtremumLimitFunction<T> findDataFunction,
                                                    BlockTaskWithResult<T, R> blockTaskWithResult,
                                                    int step) throws InterruptedException {
        beforeTask();
        int count = countFunction.count();
        if (count < 1) {
            return FutureResult.empty();
        }
        // 新建阻塞队列
        ArrayBlockingQueue<List<T>> queue = new ArrayBlockingQueue<>(DEFAULT_QUEUE_CAPACITY);
        // 主线程同步获取数据传递至方法内
        MainTask mainTask = () -> putDataToQueueByExtremumLimit(findDataFunction, step, count, queue);

        List<Future<List<R>>> futures = startTaskWithResult(mainTask, queue, blockTaskWithResult);

        return new FutureResult<>(futures);
    }

    /**
     * 开始任务，新建线程池并由主线程生产数据，线程池多线程执行消费数据
     *
     * @param mainTask            主线程任务
     * @param queue               队列
     * @param blockTaskWithResult 线程池阻塞任务
     * @param <T>                 类型
     * @throws InterruptedException
     */
    private <T, R> List<Future<List<R>>> startTaskWithResult(MainTask mainTask, ArrayBlockingQueue<List<T>> queue,
                                                             BlockTaskWithResult<T, R> blockTaskWithResult) throws InterruptedException {
        List<Future<List<R>>> futures = new ArrayList<>();
        // 初始化线程池，阻塞队列
        ExecutorService executorService = Executors.newFixedThreadPool(this.threadNum);
        // 异步订阅任务
        for (int i = 0; i < this.threadNum; i++) {
            futures.add(executorService.submit(() -> {
                List<R> rs = new ArrayList<>();
                List<T> data;
                try {
                    while ((data = queue.poll(1, TimeUnit.SECONDS)) != null || !finish) {
                        try {
                            if (data != null) {
                                rs.addAll(blockTaskWithResult.execute(data));
                            }
                        } catch (Exception e) {
                            log.error("执行任务分片异常", e);
                        }
                    }
                } catch (InterruptedException e) {
                    log.error("队列poll数据异常:", e);
                }
                return rs;
            }));
        }

        // 主线程同步获取数据
        mainTask.run();
        // 关闭线程池
        executorService.shutdown();
        return futures;
    }

    /**
     * 将查询结果数据入队
     *
     * @param findDataFunction 查询函数
     * @param step             步长
     * @param count            总数
     * @param queue            队列
     * @param <T>              类型
     * @throws InterruptedException
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
     * @throws InterruptedException
     */
    private <T extends ExtremumField<V>, V extends Comparable<V>> void
    putDataToQueueByExtremumLimit(FindDataExtremumLimitFunction<T> findDataFunction,
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

    public static void main(String[] args) throws InterruptedException {
        demo2();
    }

    public static void demo1() throws InterruptedException {
        long l = System.currentTimeMillis();
        ArrayBlockTaskDealer taskDealer = new ArrayBlockTaskDealer(8);
        FutureResult<HashMap<String, Object>> futureResult = taskDealer.executeBlockTask(() -> 2000, limit -> {
            System.out.println("put data");
            int num = limit.getNum();
            ArrayList<HashMap<String, Object>> hashMaps = new ArrayList<>();
            for (int i = 0; i < limit.getSize(); i++) {
                HashMap<String, Object> hashMap = new HashMap<>();
                hashMap.put("num", num + i);
                hashMaps.add(hashMap);
            }
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return hashMaps;
        }, data -> {
            try {
                System.out.println(data);
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return data;
        }, 100);
        List<HashMap<String, Object>> results = futureResult.getResults();
        System.out.println(results);
        System.out.println(System.currentTimeMillis() - l);
    }

    public static void demo2() throws InterruptedException {
        ArrayBlockTaskDealer arrayBlockTaskDealer = new ArrayBlockTaskDealer();
        FutureResult<TestEntity> find_end = arrayBlockTaskDealer.executeBlockTaskByExtremumLimit(
                () -> 2000,
                limit -> {
                    System.out.println("max:" + limit.getExtremum());
                    ArrayList<TestEntity> integers = new ArrayList<>();
                    for (int i = 0; i < limit.getSize(); i++) {
                        TestEntity testEntity = new TestEntity();
                        testEntity.setId(i);
                        integers.add(testEntity);
                    }
                    System.out.println("find end");
                    return integers;
                },
                data -> {
                    try {
                        Thread.sleep(3000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    System.out.println("size:" + data.size());
                    return data.stream().filter(e -> e.getId() > 10).collect(Collectors.toList());
                },
                100);
        find_end.getResults();
        System.out.println("~~~");
    }

    private static class TestEntity implements ExtremumField<Integer> {

        private int id;

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        @Override
        public Integer getExtremumValue() {
            return id;
        }
    }

}
