package cn.cheny.toolbox.proxy.javassist;

import cn.cheny.toolbox.proxy.ToolboxAopContext;
import javassist.util.proxy.MethodHandler;

import java.lang.reflect.Method;

/**
 * javassist代理MethodHandler模版
 *
 * @author by chenyi
 * @date 2022/7/7
 */
public class MethodHandlerTemplate implements MethodHandler {

    private Object origin;

    public MethodHandlerTemplate(Object origin) {
        this.origin = origin;
    }

    @Override
    public Object invoke(Object self, Method thisMethod, Method proceed, Object[] args) throws Throwable {
        Object result;
        Object old = ToolboxAopContext.setCurrentProxy(self);
        try {
            before(self, thisMethod, proceed, args);
            result = handle(self, thisMethod, proceed, args);
            after(self, thisMethod, proceed, args, result);
        } catch (Throwable e) {
            result = error(self, thisMethod, proceed, args, e);
        } finally {
            ToolboxAopContext.setCurrentProxy(old);
        }
        return result;
    }

    protected Object handle(Object self, Method thisMethod, Method proceed, Object[] args) throws Throwable {
        return invokeOrigin(thisMethod, args);
    }

    protected final Object invokeOrigin(Method thisMethod, Object[] args) throws Throwable {
        return thisMethod.invoke(origin, args);
    }

    protected void before(Object self, Method thisMethod, Method proceed, Object[] args) {

    }

    protected void after(Object self, Method thisMethod, Method proceed, Object[] args, Object result) {

    }

    protected Object error(Object self, Method thisMethod, Method proceed, Object[] args, Throwable error) throws Throwable {
        throw error;
    }

    public Object getOrigin() {
        return origin;
    }

    public void setOrigin(Object origin) {
        this.origin = origin;
    }
}
