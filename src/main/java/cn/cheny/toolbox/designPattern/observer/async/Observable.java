package cn.cheny.toolbox.designPattern.observer.async;

import java.util.Vector;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * 任务源
 * 异步任务获取资源返回数据
 * 订阅该资源（1，任务已经完成数据已返回，同步执行订阅任务；2，任务还未完成数据未返回，将订阅者加入队列等待通知）
 * TODO: 2018/2/10 待完善
 *
 * @param <T>
 */
public class Observable<T> {

    private Executor executor;

    private Vector<Subscriber<T>> subscribers = new Vector<>();

    /**
     * 异步任务
     */
    private Action<T> action;

    /**
     * 任务完成标志
     *
     * @param action
     */
    private boolean isCompleted;

    private Observable(Action<T> action) {
        this.action = action;
        executor = Executors.newSingleThreadExecutor();
    }

    /**
     * 创建实例
     *
     * @param action 资源任务
     * @param <T>
     * @return
     */
    public static <T> Observable<T> create(Action<T> action) {
        Observable<T> observable = new Observable<>(action);
        action.setObservable(observable);
        return observable;
    }

    /**
     * 执行异步获取资源任务
     */
    public void asyncAction() {
        executor.execute(action);
    }

    /**
     * 订阅
     * 如果action已经返回数据则同步执行,否则存起来等待通知
     *
     * @param subscriber
     */
    public void subscribe(Subscriber<T> subscriber) {
        if (!isCompleted) {
            subscribers.add(subscriber);
        } else {
            subscriber.onExecute(action.getFuture());
        }
    }

    /**
     * 异步订阅
     * 返回数据通知执行
     *
     * @param future
     */
    private void executeSubscriber(T future) {
        for (Subscriber<T> subscriber : subscribers) {
            subscriber.onExecute(future);
        }
    }

    /**
     * 静态内部类 异步任务
     *
     * @param <T>
     */
    public static class Action<T> implements Runnable {

        /**
         * 持有执行此异步任务的任务源
         */
        private Observable<T> observable;

        /**
         * 执行的任务
         */
        private Task<T> task;

        /**
         * 任务执行完返回的数据
         */
        private T future;

        private Action(Task<T> task) {
            this.task = task;
        }

        public static <T> Action<T> create(Task<T> task) {
            return new Action<>(task);
        }

        @Override
        public void run() {
            try {
                future = task.execute();
                observable.isCompleted = true;
            } catch (Exception e) {
                observable.isCompleted = false;
            }
            if (observable.isCompleted) {
                observable.executeSubscriber(future);
            }
        }


        private T getFuture() {
            return future;
        }

        private void setObservable(Observable<T> observable) {
            this.observable = observable;
        }

    }

    public interface Task<T> {
        T execute();
    }

}
