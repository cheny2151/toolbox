package cn.cheny.toolbox.designPattern.observer.async;

/**
 * 订阅者
 */
public interface Subscriber<T> {

    void onExecute(T var1);

}
