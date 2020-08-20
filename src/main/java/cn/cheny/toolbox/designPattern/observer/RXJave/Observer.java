package cn.cheny.toolbox.designPattern.observer.RXJave;

/**
 * 观察者接口
 *
 * @param <T>
 */
public interface Observer<T> {

    void onStart();

    void onCompleted();

    void onError(Throwable t);

    void onExecute(T var1);

}
