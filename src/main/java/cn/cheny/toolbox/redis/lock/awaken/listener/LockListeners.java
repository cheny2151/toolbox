package cn.cheny.toolbox.redis.lock.awaken.listener;

import java.util.LinkedList;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 锁监听队列封装，赋值监听锁释放信号与唤醒争抢锁线程
 *
 * @author cheney
 * @date 2020-10-28
 */
public interface LockListeners {

    /**
     * 添加锁监听者
     */
    void addLockListener(LockListener lockListener);

    /**
     * 唤醒争抢锁线程
     */
    void awake(String lockChannel);

    abstract class AbstractLockListeners implements LockListeners {

        protected final ConcurrentHashMap<String, LinkedList<LockListener>> LockListeners = new ConcurrentHashMap<>();

        @Override
        public void addLockListener(LockListener lockListener) {
            LinkedList<LockListener> linkedList = LockListeners.computeIfAbsent(lockListener.getListenerChannel(), key -> new LinkedList<>());
            synchronized (linkedList) {
                linkedList.push(lockListener);
            }
        }
    }

    /**
     * 公平锁监听队列
     */
    class FairLockListeners extends AbstractLockListeners {

        @Override
        public void awake(String lockChannel) {
            LinkedList<LockListener> linkedList = LockListeners.get(lockChannel);
            if (linkedList != null) {
                synchronized (linkedList) {
                    if (linkedList.size() > 0) {
                        LockListener lockListener = linkedList.removeLast();
                        lockListener.handleListener();
                    }
                }
            }
        }
    }

    /**
     * 非公平锁监听队列
     */
    class NonFairLockListeners extends AbstractLockListeners {

        @Override
        public void awake(String lockChannel) {
            LinkedList<LockListener> linkedList = LockListeners.get(lockChannel);
            if (linkedList != null) {
                synchronized (linkedList) {
                    linkedList.forEach(LockListener::handleListener);
                    linkedList.clear();
                }
            }
        }
    }

}
