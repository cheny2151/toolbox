package cn.cheny.toolbox.window.factory;

/**
 * @author by chenyi
 * @date 2021/9/22
 */
public interface WindowProxyFactory {

    <T> T createProxy(T target);

}
