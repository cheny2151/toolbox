package cn.cheny.toolbox.reflect.methodHolder;

import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
 * 静态方法MethodHolder实现类
 *
 * @author cheney
 * @date 2019-12-05
 */
@Slf4j
public class StaticMethodHolder extends BaseMethodHolder {

    public StaticMethodHolder(Class<?> clazz) {
        super(clazz);
        Method[] methods = clazz.getMethods();
        for (Method method : methods) {
            if (Modifier.isStatic(method.getModifiers())) {
                cacheMethod(method);
            }
        }
    }

    @Override
    public Object invoke(String methodName, Object obj, Object... args) {
        return super.invoke(methodName, null, args);
    }

    @Override
    public Object invoke(Class<?> returnType, String methodName, Object obj, Object... args) {
        return super.invoke(returnType, methodName, null, args);
    }

}
