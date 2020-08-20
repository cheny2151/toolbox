package cn.cheny.toolbox.designPattern.observer.RXJave;

/**
 * 观察者抽象类
 *
 * @param <T>
 */
public abstract class AbstractSubscriber<T> implements Observer<T> {

    public void onError(Throwable t) {
        t.printStackTrace();
    }

    public void onCompleted() {
    }

    @Override
    public void onStart() {
    }

}
