package cn.cheny.toolbox.asyncTask.pool2;

import cn.cheny.toolbox.other.order.Orders;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * AsyncConsumeTaskDealerPooled池
 *
 * @author by chenyi
 * @date 2022/1/14
 */
public class AsyncConsumeTaskDealerPool extends GenericObjectPool<AsyncConsumeTaskDealerPooled> {

    private List<AsyncConsumeTaskDealerPooled> waitRecycledList;

    private ScheduledExecutorService checkThread = Executors.newSingleThreadScheduledExecutor();

    private AsyncConsumeTaskDealerPool(AsyncConsumeTaskDealerPooledFactory factory,
                                       GenericObjectPoolConfig<AsyncConsumeTaskDealerPooled> poolConfig) {
        super(factory, poolConfig);
        waitRecycledList = Collections.synchronizedList(new ArrayList<>());
        startCheckThread();
    }

    public static AsyncConsumeTaskDealerPoolBuilder builder() {
        return new AsyncConsumeTaskDealerPoolBuilder();
    }

    @Override
    public void returnObject(AsyncConsumeTaskDealerPooled pooled) {
        if (pooled.getRunningNumber() == 0) {
            super.returnObject(pooled);
        } else {
            waitRecycledList.add(pooled);
        }
    }

    private void startCheckThread() {
        checkThread.scheduleWithFixedDelay(this::findToReturn, 10, 10, TimeUnit.SECONDS);
    }

    private void findToReturn() {
        List<AsyncConsumeTaskDealerPooled> returnList = waitRecycledList.stream()
                .filter(pooled -> pooled.getRunningNumber() == 0)
                .collect(Collectors.toList());
        if (returnList.size() > 0) {
            waitRecycledList.removeAll(returnList);
            returnList.forEach(super::returnObject);
        }
    }

    /**
     * AsyncConsumeTaskDealerPooled池构建器
     */
    public static class AsyncConsumeTaskDealerPoolBuilder {

        private Integer threadNum;

        private Integer queueNum;

        private String threadName;

        private Boolean mainHelpTask;

        private Boolean continueWhenSliceTaskError;

        private Orders.OrderType orderType;

        private Integer maxTotal;
        private Integer maxIdle;
        private Integer minIdle;

        public AsyncConsumeTaskDealerPool build() {
            AsyncConsumeTaskDealerPooledFactory factory
                    = new AsyncConsumeTaskDealerPooledFactory(threadNum, queueNum, threadName, mainHelpTask, continueWhenSliceTaskError, orderType);
            GenericObjectPoolConfig<AsyncConsumeTaskDealerPooled> poolConfig = new GenericObjectPoolConfig<>();
            if (maxTotal != null) {
                poolConfig.setMaxTotal(maxTotal);
            }
            if (maxIdle != null) {
                poolConfig.setMaxIdle(maxIdle);
            }
            if (minIdle != null) {
                poolConfig.setMinIdle(minIdle);
            }
            // 10分钟最大空闲时间
            poolConfig.setMinEvictableIdleTime(Duration.ofMillis(600000L));
            // 5分钟检测一次
            poolConfig.setTimeBetweenEvictionRuns(Duration.ofMillis(300000L));
            AsyncConsumeTaskDealerPool pool = new AsyncConsumeTaskDealerPool(factory, poolConfig);
            factory.setBelongPool(pool);
            return pool;
        }

        public AsyncConsumeTaskDealerPoolBuilder threadNum(Integer threadNum) {
            this.threadNum = threadNum;
            return this;
        }

        public AsyncConsumeTaskDealerPoolBuilder queueNum(Integer queueNum) {
            this.queueNum = queueNum;
            return this;
        }

        public AsyncConsumeTaskDealerPoolBuilder threadName(String threadName) {
            this.threadName = threadName;
            return this;
        }

        public AsyncConsumeTaskDealerPoolBuilder mainHelpTask(Boolean mainHelpTask) {
            this.mainHelpTask = mainHelpTask;
            return this;
        }

        public AsyncConsumeTaskDealerPoolBuilder continueWhenSliceTaskError(Boolean continueWhenSliceTaskError) {
            this.continueWhenSliceTaskError = continueWhenSliceTaskError;
            return this;
        }

        public AsyncConsumeTaskDealerPoolBuilder orderType(Orders.OrderType orderType) {
            this.orderType = orderType;
            return this;
        }

        public AsyncConsumeTaskDealerPoolBuilder maxTotal(Integer maxTotal) {
            this.maxTotal = maxTotal;
            return this;
        }

        public AsyncConsumeTaskDealerPoolBuilder maxIdle(Integer maxIdle) {
            this.maxIdle = maxIdle;
            return this;
        }

        public AsyncConsumeTaskDealerPoolBuilder minIdle(Integer minIdle) {
            this.minIdle = minIdle;
            return this;
        }
    }

}
