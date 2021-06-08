package cn.cheny.toolbox.proxy;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * 翻译重试代理
 *
 * @date 2021/1/14
 * @Created by chenyi
 */
@Slf4j
public class MethodRetryProxy implements InvocationHandler {

    private final static String[] ALL_METHODS = new String[]{};

    private final Object actual;

    private final String[] proxyMethods;

    private Class<Throwable>[] stopErrors;

    private int timeout;

    private int maximumTry;

    public MethodRetryProxy(Object actual, int maximumTry, String... retryMethods) {
        this(actual, maximumTry, Integer.MAX_VALUE, null, retryMethods);
    }

    public MethodRetryProxy(Object actual, int maximumTry, int timeout, Class<Throwable>[] stopErrors, String... retryMethods) {
        this.actual = actual;
        this.maximumTry = maximumTry;
        this.timeout = timeout;
        this.stopErrors = stopErrors;
        this.proxyMethods = retryMethods == null || retryMethods.length == 0 ? ALL_METHODS : retryMethods;
    }

    public <I> I newProxy(Class<I> interfaceClass) {
        if (!interfaceClass.isInterface()) {
            throw new IllegalArgumentException("Target of jdk proxy must be a interface");
        }
        if (!interfaceClass.isAssignableFrom(actual.getClass())) {
            throw new IllegalArgumentException(String.format("Actual [%s] not implement [%s]",
                    actual.getClass().getName(), interfaceClass.getName()));
        }
        return (I) Proxy.newProxyInstance(interfaceClass.getClassLoader(), new Class[]{interfaceClass}, this);
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (!isTargetMethod(method)) {
            try {
                return method.invoke(actual, args);
            } catch (InvocationTargetException e) {
                throw e.getTargetException();
            }
        }
        // 代理重试机制
        long firstTime = System.currentTimeMillis();
        int c = 0;
        Throwable throwable;
        do {
            try {
                return method.invoke(actual, args);
            } catch (Throwable t) {
                // 出现指定异常直接抛出，不做重试
                throwable = t;
                if (isStopError(throwable)) {
                    break;
                }
            }
        } while (!timeout(firstTime) && c++ < maximumTry);
        if (throwable instanceof InvocationTargetException) {
            throwable = ((InvocationTargetException) throwable).getTargetException();
        }
        if (c > 0) {
            log.error("translation service:{} retry fail,try time：{},error msg:{}",
                    actual.getClass().getSimpleName(), c, throwable.getMessage());
        }
        throw throwable;
    }

    private boolean isTargetMethod(Method method) {
        return proxyMethods == ALL_METHODS || ArrayUtils.contains(proxyMethods, method.getName());
    }

    private boolean isStopError(Throwable error) {
        if (error instanceof InvocationTargetException) {
            error = ((InvocationTargetException) error).getTargetException();
        }
        return stopErrors != null && ArrayUtils.contains(stopErrors, error.getClass());
    }

    private boolean timeout(long firstTime) {
        return (System.currentTimeMillis() - firstTime) / 1000 >= timeout;
    }

    public int getTimeout() {
        return timeout;
    }

    public MethodRetryProxy timeout(int timeout) {
        this.timeout = timeout;
        return this;
    }

    public int getMaximumTry() {
        return maximumTry;
    }

    public MethodRetryProxy maximumTry(int maximumTry) {
        this.maximumTry = maximumTry;
        return this;
    }

    public Class<Throwable>[] getStopErrors() {
        return stopErrors;
    }

    public MethodRetryProxy stopErrors(Class<Throwable>[] stopErrors) {
        this.stopErrors = stopErrors;
        return this;
    }
}
