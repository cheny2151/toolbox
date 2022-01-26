package cn.cheny.toolbox.window.factory;

import cn.cheny.toolbox.reflect.ReflectUtils;
import cn.cheny.toolbox.window.*;

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
        Map<String, CollectedMethod> collectGroup = collectedMethods.stream().map(m -> {
            Collected collected = m.getDeclaredAnnotation(Collected.class);
            return new CollectedMethod(m, collected);
        }).collect(Collectors.toMap(cm -> cm.getCollected().group(), cm -> cm));
        Map<Method, BatchConfiguration> results = new HashMap<>();
        for (Method bm : batchMethods) {
            Method key = bm;
            Batch batch = bm.getDeclaredAnnotation(Batch.class);
            BatchMethod batchMethod = new BatchMethod(bm, batch);
            String group = batch.group();
            CollectedMethod collectedMethod = collectGroup.get(group);
            if (collectedMethod != null) {
                key = collectedMethod.getMethod();
            }
            BatchConfiguration value = new BatchConfiguration(batchMethod, collectedMethod);
            results.put(key, value);
        }
        return results;
    }

}
