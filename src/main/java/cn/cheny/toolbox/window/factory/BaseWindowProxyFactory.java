package cn.cheny.toolbox.window.factory;

import cn.cheny.toolbox.reflect.ReflectUtils;
import cn.cheny.toolbox.window.Batch;
import cn.cheny.toolbox.window.BatchConfiguration;
import cn.cheny.toolbox.window.Collected;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 窗口代理基础类
 *
 * @author by chenyi
 * @date 2021/9/22
 */
public abstract class BaseWindowProxyFactory implements WindowProxyFactory {

    /**
     * 扫描实例上的@Batch/@Collected配置
     *
     * @param target 实例
     * @return 配置集合
     */
    protected Map<Method, BatchConfiguration> scanBatchConfiguration(Object target) {
        Set<Method> batchMethods = ReflectUtils.getAllMethodHasAnnotation(target.getClass(), Batch.class);
        Set<Method> collectedMethods = ReflectUtils.getAllMethodHasAnnotation(target.getClass(), Collected.class);
        Map<String, collectedMethod> collectGroup = collectedMethods.stream().map(m -> {
            Collected collected = m.getDeclaredAnnotation(Collected.class);
            return new collectedMethod(m, collected);
        }).collect(Collectors.toMap(cm -> cm.collected.group(), cm -> cm));
        Map<Method, BatchConfiguration> results = new HashMap<>();
        for (Method batchMethod : batchMethods) {
            Method key = batchMethod;
            Batch batch = batchMethod.getDeclaredAnnotation(Batch.class);
            String group = batch.group();
            collectedMethod collectedMethod = collectGroup.get(group);
            Collected collected = null;
            if (collectedMethod != null) {
                key = collectedMethod.method;
                collected = collectedMethod.collected;
            }
            BatchConfiguration value = new BatchConfiguration(batch, batchMethod, collected);
            results.put(key, value);
        }
        return results;
    }

    private static class collectedMethod {
        private final Method method;
        private final Collected collected;

        public collectedMethod(Method method, Collected collected) {
            this.method = method;
            this.collected = collected;
        }
    }

}
