package cn.cheny.toolbox.window.factory.javassist;

import cn.cheny.toolbox.exception.ToolboxRuntimeException;
import cn.cheny.toolbox.window.BatchConfiguration;
import cn.cheny.toolbox.window.WindowElement;
import cn.cheny.toolbox.window.coordinator.WindowCoordinator;
import cn.cheny.toolbox.window.factory.BaseWindowProxyFactory;
import javassist.util.proxy.MethodHandler;
import javassist.util.proxy.ProxyFactory;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * javassist实现窗口代理
 *
 * @author by chenyi
 * @date 2021/9/22
 */
public class JavassistWindowProxyFactory extends BaseWindowProxyFactory {

    @Override
    public <T> T createProxy(T target) {
        return createProxy(target, new Class[0], new Object[0]);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T createProxy(T target, Class<?>[] classes, Object[] args) {
        ProxyFactory proxyFactory = new ProxyFactory();
        proxyFactory.setSuperclass(target.getClass());
        WindowInferenceHandler handler = new WindowInferenceHandler(target);
        Map<Method, BatchConfiguration> batchConfigMap = scanBatchConfiguration(target);
        if (batchConfigMap.size() > 0) {
            Map<Method, WindowCoordinator> coordinatorMap = batchConfigMap.entrySet().stream()
                    .collect(Collectors.toMap(Map.Entry::getKey, entry -> new WindowCoordinator(target, entry.getValue())));
            handler.putAll(coordinatorMap);
        }
        try {
            return (T) proxyFactory.create(classes, args, handler);
        } catch (Exception e) {
            throw new ToolboxRuntimeException("创建代理失败", e);
        }
    }

    static class WindowInferenceHandler implements MethodHandler {

        private final Object target;

        private final Map<Method, WindowCoordinator> windowCoordinatorMap;

        public WindowInferenceHandler(Object target) {
            this.target = target;
            this.windowCoordinatorMap = new HashMap<>();
        }

        @Override
        public Object invoke(Object proxy, Method method, Method methodProxy, Object[] args) throws Throwable {
            WindowCoordinator windowCoordinator = windowCoordinatorMap.get(method);
            if (windowCoordinator != null) {
                // 收集窗口进行批量推理
                WindowElement element = windowCoordinator.addElement(args);
                return element.getOutput();
            }
            return method.invoke(target, args);
        }

        public void put(Method method, WindowCoordinator coordinator) {
            windowCoordinatorMap.put(method, coordinator);
        }

        public void putAll(Map<Method, WindowCoordinator> coordinatorMap) {
            windowCoordinatorMap.putAll(coordinatorMap);
        }
    }

}
