package cn.cheny.toolbox.proxy;

/**
 * toolbox代理上下文
 *
 * @author by chenyi
 * @date 2022/2/11
 */
public final class ToolboxAopContext {

    private static final ThreadLocal<Object> currentProxy = new ThreadLocal<>();

    private ToolboxAopContext() {
    }

    @SuppressWarnings("unchecked")
    public static <T> T currentProxy() {
        return (T) currentProxy.get();
    }

    public static Object requiredProxy() throws IllegalStateException {
        Object proxy = currentProxy.get();
        if (proxy == null) {
            throw new IllegalStateException("Cannot find current proxy: Set 'exposeProxy' property on Advised to 'true' to make it available," +
                    " and ensure that AopContext.currentProxy() is invoked in the same thread as the AOP invocation context.");
        }
        return proxy;
    }

    /**
     * 设置当前代理
     *
     * @param proxy 代理类
     * @return 代理类
     */
    public static Object setCurrentProxy(Object proxy) {
        Object old = currentProxy.get();
        if (proxy != null) {
            currentProxy.set(proxy);
        } else {
            currentProxy.remove();
        }
        return old;
    }

}
