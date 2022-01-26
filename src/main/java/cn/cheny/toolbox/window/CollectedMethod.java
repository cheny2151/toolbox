package cn.cheny.toolbox.window;

import java.lang.reflect.Method;

/**
 * 待窗口收集方法
 *
 * @author by chenyi
 * @date 2022/1/24
 */
public class CollectedMethod {

    private final Method method;

    private final Collected collected;

    public CollectedMethod(Method method, Collected collected) {
        this.method = method;
        this.collected = collected;
    }

    public Method getMethod() {
        return method;
    }

    public Collected getCollected() {
        return collected;
    }
}
