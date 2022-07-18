package cn.cheny.toolbox.redis.lock.awaken.listener;

import java.util.LinkedList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingDeque;

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
     * 删除锁监听者
     */
    void removeLockListener(LockListener lockListener);

    /**
     * 唤醒争抢锁线程
     */
    void awake(String lockChannel);

    /**
     * 公平锁监听队列
     */
    class FairLockListeners implements LockListeners {

        private final ConcurrentHashMap<String, LinkedBlockingDeque<LockListener>> LockListeners = new ConcurrentHashMap<>();

        @Override
        public void addLockListener(LockListener lockListener) {
            LinkedBlockingDeque<LockListener> listeners = LockListeners.computeIfAbsent(lockListener.getListenerChannel(), key -> new LinkedBlockingDeque<>());
            listeners.push(lockListener);
        }

        @Override
        public void removeLockListener(LockListener lockListener) {
            LinkedBlockingDeque<LockListener> listeners = LockListeners.get(lockListener.getListenerChannel());
            if (listeners != null) {
                listeners.remove(lockListener);
            }
        }

        @Override
        public void awake(String lockChannel) {
            LinkedBlockingDeque<LockListener> listeners = LockListeners.get(lockChannel);
            if (listeners != null) {
                LockListener lockListener = listeners.pollLast();
                if (lockListener != null) {
                    lockListener.handleListener();
                }
            }
        }
    }

    /**
     * 非公平锁监听队列
     */
    class NonFairLockListeners implements LockListeners {

        private final ConcurrentHashMap<String, LinkedList<LockListener>> LockListeners = new ConcurrentHashMap<>();

        @Override
        public void addLockListener(LockListener lockListener) {
            LinkedList<LockListener> linkedList = LockListeners.computeIfAbsent(lockListener.getListenerChannel(), key -> new LinkedList<>());
            synchronized (linkedList) {
                linkedList.push(lockListener);
            }
        }

        @Override
        public void removeLockListener(LockListener lockListener) {
            LinkedList<LockListener> linkedList = LockListeners.get(lockListener.getListenerChannel());
            if (linkedList != null) {
                synchronized (linkedList) {
                    linkedList.push(lockListener);
                }
            }
        }

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
