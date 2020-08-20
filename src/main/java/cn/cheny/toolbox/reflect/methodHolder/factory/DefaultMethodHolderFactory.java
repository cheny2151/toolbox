package cn.cheny.toolbox.reflect.methodHolder.factory;

import cn.cheny.toolbox.reflect.methodHolder.MethodHolder;
import cn.cheny.toolbox.reflect.methodHolder.exception.MethodHolderReflectException;

import java.lang.reflect.Constructor;
import java.util.concurrent.ConcurrentHashMap;

/**
 * MethodHolder工厂类
 *
 * @author cheney
 * @date 2019-12-05
 */
public class DefaultMethodHolderFactory implements MethodHolderFactory {

    protected ConcurrentHashMap<Class<?>, MethodHolder> methodHolderCache = new ConcurrentHashMap<>();

    @Override
    public MethodHolder getMethodHolder(Class<?> clazz, Class<? extends MethodHolder> methodHolderClass) {
        return methodHolderCache.computeIfAbsent(clazz, key -> registeredClass(clazz, methodHolderClass));
    }

    @Override
    public MethodHolder registeredClass(Class<?> clazz, Class<? extends MethodHolder> methodHolderClass) {
        try {
            Constructor<? extends MethodHolder> constructor = methodHolderClass.getConstructor(Class.class);
            return constructor.newInstance(clazz);
        } catch (Exception e) {
            throw new MethodHolderReflectException("反射执行构造函数异常,class:'" + clazz.getSimpleName() + "'", e);
        }
    }

}
