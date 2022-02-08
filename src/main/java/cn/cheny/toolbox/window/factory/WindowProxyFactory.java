package cn.cheny.toolbox.window.factory;

import cn.cheny.toolbox.window.factory.javassist.JavassistWindowProxyFactory;

/**
 * @author by chenyi
 * @date 2021/9/22
 */
public interface WindowProxyFactory {

    WindowProxyFactory INSTANCE = new JavassistWindowProxyFactory();

    <T> T createProxy(T target);

    <T> T createProxy(T target, Class<?>[] classes, Object[] args);

    static WindowProxyFactory getInstance(){
        return INSTANCE;
    }

}
