package cn.cheny.toolbox.asyncTask;

import cn.cheny.toolbox.asyncTask.function.AsyncTask;
import cn.cheny.toolbox.asyncTask.function.AsyncTaskWithResult;
import cn.cheny.toolbox.asyncTask.function.Producer;
import cn.cheny.toolbox.exception.ToolboxRuntimeException;
import cn.cheny.toolbox.other.NamePrefixThreadFactory;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * AsyncConsumeTaskDealer的响应式发布订阅模式
 * 只有调用订阅任务时，才真正开始生产消费数据
 *
 * @author cheney
 * @date 2021-06-13
 */
@Slf4j
public class AsyncConsumeTaskPublisher extends AsyncConsumeTaskDealer {

    public AsyncConsumeTaskPublisher() {
    }

    public AsyncConsumeTaskPublisher(int threadNum) {
        super(threadNum);
    }

    public AsyncConsumeTaskPublisher(int threadNum, boolean mainHelpTask) {
        super(threadNum, mainHelpTask);
    }

    public AsyncConsumeTaskPublisher(int threadNum, boolean mainHelpTask, boolean continueWhereSliceTaskError) {
        super(threadNum, mainHelpTask, continueWhereSliceTaskError);
    }

    /**
     * 执行生产消费任务
     *
     * @param producer 生产任务
     * @param <T>      数据类型
     */
    public <T, R> TaskDealerPublisher<T> publisher(Producer<T> producer) {
        beforeTask();
        // 新建阻塞队列
        ArrayBlockingQueue<TaskPackage<List<T>>> queue = new ArrayBlockingQueue<>(getQueueNum());
        TaskPublish<T> taskPublish = new TaskPublish<>(queue);
        // 主线程同步获取数据传递至方法内
        MainTask mainTask = () -> doProduce(producer, taskPublish);

        return new TaskDealerPublisher<>(mainTask, queue);
    }

    /**
     * 创建通过队列传递数据的任务
     *
     * @param queue               输入队列
     * @param asyncTaskWithResult 任务体
     * @param resultQueue         输出队列
     * @param <T>                 泛型：输入数据类型
     * @param <R>                 泛型：输出数据类型
     * @return callback任务
     */
    private <T, R> Runnable createTaskPushToQueue(ArrayBlockingQueue<TaskPackage<List<T>>> queue,
                                                  AsyncTaskWithResult<T, R> asyncTaskWithResult,
                                                  ArrayBlockingQueue<TaskPackage<List<R>>> resultQueue) {
        return () -> {
            TaskPackage<List<T>> taskPackage;
            try {
                while ((taskPackage = queue.poll(1, TimeUnit.SECONDS)) != null || !finish) {
                    if (interrupted) {
                        continue;
                    }
                    try {
                        if (taskPackage != null) {
                            List<R> taskResult = asyncTaskWithResult.execute(taskPackage.getData());
                            if (taskResult != null) {
                                resultQueue.put(new TaskPackage<>(taskResult, taskPackage.getIndex()));
                            }
                        }
                    } catch (Throwable e) {
                        if (continueWhereSliceTaskError) {
                            log.error("执行任务分片异常", e);
                        } else {
                            this.interrupted = true;
                            this.interruptedCause = e;
                        }
                    }
                }
            } catch (InterruptedException e) {
                throw new ToolboxRuntimeException("队列poll数据异常", e);
            }
        };
    }

    public class TaskDealerPublisher<T> {

        private final MainTask mainTask;

        private final ArrayBlockingQueue<TaskPackage<List<T>>> fromQueue;

        private Runnable task;

        private TaskDealerPublisher<?> from;

        private String innerThreadName;

        private int innerThreadNum;

        private int innerQueueNum;

        public TaskDealerPublisher(MainTask mainTask, ArrayBlockingQueue<TaskPackage<List<T>>> fromQueue) {
            this.mainTask = mainTask;
            this.fromQueue = fromQueue;
            // 线程、队列参数继承于AsyncConsumeTaskDealer
            this.innerThreadNum = getThreadNum();
            this.innerQueueNum = getQueueNum();
            this.innerThreadName = getThreadName();
        }

        public TaskDealerPublisher(TaskDealerPublisher<?> from,
                                   MainTask mainTask, Runnable task,
                                   ArrayBlockingQueue<TaskPackage<List<T>>> fromQueue) {
            this(mainTask, task, fromQueue);
            this.from = from;
        }

        public TaskDealerPublisher(MainTask mainTask, Runnable task, ArrayBlockingQueue<TaskPackage<List<T>>> fromQueue) {
            this(mainTask, fromQueue);
            this.task = task;
        }

        /**
         * 订阅任务
         * 只有调用订阅任务时，才真正开始生产消费数据
         *
         * @param asyncTask 异步任务
         */
        public void subscribe(AsyncTask<T> asyncTask) {
            subscribe(asyncTask, getThreadNum(), getThreadName());
        }

        /**
         * 订阅任务，返回结果数据
         *
         * @param asyncTaskWithResult 异步任务（返回结果）
         * @param <R>                 结果类型
         * @return 结果数据
         */
        public <R> FutureResult<R> subscribe(AsyncTaskWithResult<T, R> asyncTaskWithResult) {
            return subscribe(asyncTaskWithResult, getThreadNum(), getThreadName());
        }

        /**
         * 订阅任务，指定线程数，线程名
         *
         * @param asyncTask 异步任务
         * @param threadNum 线程数
         * @param threadNme 线程名
         */
        public void subscribe(AsyncTask<T> asyncTask, int threadNum, String threadNme) {
            ExecutorService executorService = newExecutorService(threadNum, threadNme);
            subscribe(asyncTask, executorService);
        }

        /**
         * 订阅任务，指定线程数，线程名
         *
         * @param asyncTaskWithResult 异步任务（返回结果）
         * @param threadNum           线程数
         * @param threadNme           线程名
         */
        public <R> FutureResult<R> subscribe(AsyncTaskWithResult<T, R> asyncTaskWithResult, int threadNum, String threadNme) {
            ExecutorService executorService = newExecutorService(threadNum, threadNme);
            return subscribe(asyncTaskWithResult, executorService);
        }

        /**
         * 订阅任务，指定线程池
         *
         * @param asyncTask       异步任务
         * @param executorService 线程池
         */
        public void subscribe(AsyncTask<T> asyncTask, ExecutorService executorService) {
            doSubscribe();
            // 最终通过AsyncConsumeTaskDealer的函数开始任务
            startTask(mainTask, fromQueue, asyncTask, executorService);
        }

        /**
         * 订阅任务，指定线程池
         *
         * @param asyncTaskWithResult 异步任务（返回结果）
         * @param executorService     线程池
         */
        public <R> FutureResult<R> subscribe(AsyncTaskWithResult<T, R> asyncTaskWithResult, ExecutorService executorService) {
            doSubscribe();
            // 最终通过AsyncConsumeTaskDealer的函数开始任务
            return startTaskWithResult(mainTask, fromQueue, asyncTaskWithResult, executorService);
        }

        /**
         * 创建下一步任务
         *
         * @param asyncTaskWithResult 下一步骤的异步任务
         * @param <R>                 任务执行的输出类型
         * @return
         */
        public <R> TaskDealerPublisher<R> then(AsyncTaskWithResult<T, R> asyncTaskWithResult) {
            ArrayBlockingQueue<TaskPackage<List<R>>> resultQueue = new ArrayBlockingQueue<>(innerQueueNum);
            Runnable taskPushToQueue = createTaskPushToQueue(this.fromQueue, asyncTaskWithResult, resultQueue);
            return new TaskDealerPublisher<>(this, mainTask, taskPushToQueue, resultQueue);
        }

        private void doSubscribe() {
            if (from != null) {
                from.doSubscribe();
            }
            if (task != null) {
                ExecutorService executorService = newExecutorService(this.innerThreadNum, this.innerThreadName);
                // 异步订阅任务
                for (int i = 0; i < this.innerThreadNum; i++) {
                    executorService.submit(task);
                }
                executorService.shutdown();
            }
        }

        private ExecutorService newExecutorService(int threadNum, String threadName) {
            return Executors.newFixedThreadPool(threadNum, new NamePrefixThreadFactory(threadName));
        }

        public String getInnerThreadName() {
            return innerThreadName;
        }

        public void setInnerThreadName(String innerThreadName) {
            this.innerThreadName = innerThreadName;
        }

        public int getInnerThreadNum() {
            return innerThreadNum;
        }

        public void setInnerThreadNum(int innerThreadNum) {
            this.innerThreadNum = innerThreadNum;
            this.innerQueueNum = 2 * innerThreadNum;
        }

        public int getInnerQueueNum() {
            return innerQueueNum;
        }

        public void setInnerQueueNum(int innerQueueNum) {
            this.innerQueueNum = innerQueueNum;
        }

        public TaskDealerPublisher<T> innerThreadName(String innerThreadName) {
            this.setInnerThreadName(innerThreadName);
            return this;
        }

        public TaskDealerPublisher<T> innerThreadNum(int innerThreadNum) {
            this.setInnerThreadNum(innerThreadNum);
            return this;
        }

        public TaskDealerPublisher<T> innerQueueNum(int innerQueueNum) {
            this.setInnerQueueNum(innerQueueNum);
            return this;
        }
    }

}
