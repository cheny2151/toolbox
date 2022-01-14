package cn.cheny.toolbox.asyncTask.pool2;

import cn.cheny.toolbox.other.order.Orders;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.PooledObjectFactory;
import org.apache.commons.pool2.impl.DefaultPooledObject;

/**
 * AsyncConsumeTaskDealerPooled池化工厂
 *
 * @author by chenyi
 * @date 2022/1/12
 */
public class AsyncConsumeTaskDealerPooledFactory implements PooledObjectFactory<AsyncConsumeTaskDealerPooled> {

    private AsyncConsumeTaskDealerPool belongPool;

    private final Integer threadNum;

    private final Integer queueNum;

    private final String threadName;

    private final Boolean mainHelpTask;

    private final Boolean continueWhenSliceTaskError;

    private final Orders.OrderType orderType;

    AsyncConsumeTaskDealerPooledFactory(Integer threadNum, Integer queueNum, String threadName, Boolean mainHelpTask,
                                               Boolean continueWhenSliceTaskError, Orders.OrderType orderType) {
        this.threadNum = threadNum;
        this.queueNum = queueNum;
        this.threadName = threadName;
        this.mainHelpTask = mainHelpTask;
        this.continueWhenSliceTaskError = continueWhenSliceTaskError;
        this.orderType = orderType;
    }

    @Override
    public void activateObject(PooledObject<AsyncConsumeTaskDealerPooled> pooledObject) throws Exception {
        pooledObject.getObject().updateState(AsyncConsumeTaskDealerPooled.ACTIVATION);
    }

    @Override
    public void passivateObject(PooledObject<AsyncConsumeTaskDealerPooled> pooledObject) throws Exception {
        pooledObject.getObject().updateState(AsyncConsumeTaskDealerPooled.INACTIVATION);
    }

    @Override
    public void destroyObject(PooledObject<AsyncConsumeTaskDealerPooled> pooledObject) throws Exception {
        pooledObject.getObject().destroy();
    }

    @Override
    public PooledObject<AsyncConsumeTaskDealerPooled> makeObject() throws Exception {
        AsyncConsumeTaskDealerPooled pooled = new AsyncConsumeTaskDealerPooled(belongPool);
        if (threadNum != null) {
            pooled.threadNum(threadNum);
        }
        if (queueNum != null) {
            pooled.queueNum(queueNum);
        }
        if (threadName != null) {
            pooled.threadName(threadName);
        }
        if (mainHelpTask != null) {
            pooled.mainHelpTask(mainHelpTask);
        }
        if (continueWhenSliceTaskError != null) {
            pooled.continueWhenSliceTaskError(continueWhenSliceTaskError);
        }
        if (orderType != null) {
            pooled.orderType(orderType);
        }
         return new DefaultPooledObject<>(pooled);
    }

    @Override
    public boolean validateObject(PooledObject<AsyncConsumeTaskDealerPooled> pooledObject) {
        return true;
    }

    public void setBelongPool(AsyncConsumeTaskDealerPool belongPool) {
        this.belongPool = belongPool;
    }
}
