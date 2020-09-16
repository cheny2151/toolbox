package cn.cheny.toolbox.reflect;

import cn.cheny.toolbox.reflect.methodHolder.ReadWriteMethodHolder;
import cn.cheny.toolbox.reflect.methodHolder.factory.ReadWriteMethodHolderFactory;
import org.apache.commons.lang3.ArrayUtils;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 操作对象工具类
 *
 * @author cheney
 * @date 2020-09-14
 */
public class BeanUtils {

    private BeanUtils() {
    }

    /**
     * Copy Bean Property to a new Map
     *
     * @param bean bean to copy
     * @return Map
     */
    public static Map<String, Object> beanToMap(Object bean) {
        Map<String, Object> map = new HashMap<>();
        ReadWriteMethodHolder methodHolder = ReadWriteMethodHolderFactory.getInstance().getMethodHolder(bean.getClass());
        for (String property : methodHolder.getAllProperties()) {
            try {
                Object val = methodHolder.read(bean, property);
                map.put(property, val);
            } catch (Exception e) {
                // ignore
            }
        }
        return map;
    }

    /**
     * copy source properties ot target
     *
     * @param target           copy target
     * @param source           copy source
     * @param ignoreProperties ignore properties
     */
    public static void copyProperties(Object target, Object source, String... ignoreProperties) {
        ReadWriteMethodHolder targetMethodHolder = ReadWriteMethodHolderFactory.getInstance().getMethodHolder(target.getClass());
        ReadWriteMethodHolder sourceMethodHolder = ReadWriteMethodHolderFactory.getInstance().getMethodHolder(source.getClass());
        Collection<String> targetProperties = targetMethodHolder.getAllProperties();
        Collection<String> sourceProperties = sourceMethodHolder.getAllProperties().stream()
                .filter(field -> !ArrayUtils.contains(ignoreProperties, field))
                .collect(Collectors.toList());
        if (targetProperties.size() == 0 || sourceProperties.size() == 0) {
            return;
        }
        for (String sourceProperty : sourceProperties) {
            if (targetProperties.contains(sourceProperty)) {
                Object value = sourceMethodHolder.read(source, sourceProperty);
                targetMethodHolder.write(target, sourceProperty, value);
            }
        }
    }

}
