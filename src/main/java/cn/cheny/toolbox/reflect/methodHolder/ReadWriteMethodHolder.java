package cn.cheny.toolbox.reflect.methodHolder;

import cn.cheny.toolbox.reflect.ReflectUtils;
import cn.cheny.toolbox.reflect.TypeUtils;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 读写方法持有者
 *
 * @author cheney
 * @date 2020-04-20
 */
public class ReadWriteMethodHolder extends BaseMethodHolder {

    private final List<PropertySignature> readableProperties;
    private final List<PropertySignature> writableProperties;

    public ReadWriteMethodHolder(Class<?> clazz) {
        super(clazz);
        Collection<Method> readMethods = ReflectUtils.getAllReadMethod(clazz, Object.class).values();
        this.readableProperties = cacheMethods(readMethods);
        Collection<Method> writeMethods = ReflectUtils.getAllWriterMethod(clazz, Object.class).values();
        this.writableProperties = cacheMethods(writeMethods);
    }

    private List<PropertySignature> cacheMethods(Collection<Method> methods) {
        return methods
                .stream().map(method -> {
                    String property = ReflectUtils.extractPropertyName(method);
                    cacheMethod(property, method);
                    Type type;
                    if (method.getParameterCount() == 0) {
                        type = method.getGenericReturnType();
                    } else {
                        type = method.getGenericParameterTypes()[0];
                    }
                    return new PropertySignature(property, type);
                }).collect(Collectors.toList());
    }

    /**
     * 读取属性值
     *
     * @param obj      读取的对象
     * @param property 读取的字段名
     * @return 读取值结果
     */
    public Object read(Object obj, String property) {
        return invoke(property, obj);
    }

    /**
     * 对象写入值
     *
     * @param obj      写入的对象
     * @param property 写入的字段名
     * @param value    写入的值
     */
    public void write(Object obj, String property, Object value) {
        invoke(property, obj, value);
    }

    /**
     * 返回所有属性名称
     *
     * @return 属性名称集合
     */
    public Collection<String> getAllProperties() {
        return methodMap.keySet();
    }

    /**
     * 返回所有可写的属性名称
     *
     * @return 属性名称集合
     */
    public Collection<String> getWritablePropertyNames() {
        return writableProperties.stream().map(PropertySignature::getName).collect(Collectors.toList());
    }

    /**
     * 返回所有可读的属性名称
     *
     * @return 属性名称集合
     */
    public Collection<String> getReadablePropertyNames() {
        return readableProperties.stream().map(PropertySignature::getName).collect(Collectors.toList());
    }

    /**
     * 返回所有可写的属性签名
     *
     * @return 属性名称集合
     */
    public List<PropertySignature> getReadableProperties() {
        return readableProperties;
    }

    /**
     * 返回所有可读的属性签名
     *
     * @return 属性名称集合
     */
    public List<PropertySignature> getWritableProperties() {
        return writableProperties;
    }

    /**
     * 方法增强，提供基础类型转换
     */
    protected Object doInvoke(Method method, Object obj, Object... args) throws InvocationTargetException, IllegalAccessException {
        int parameterCount = method.getParameterCount();
        Object[] args0 = args;
        if (parameterCount != 0) {
            int length = args.length;
            args0 = new Object[length];
            System.arraycopy(args, 0, args0, 0, length);
            Class<?>[] types = method.getParameterTypes();
            for (int i = 0; i < types.length; i++) {
                Class<?> type = types[i];
                if (TypeUtils.isBaseClass(type)) {
                    args0[i] = TypeUtils.tryCoverBase(args0[i], type);
                }
            }
        }
        return method.invoke(obj, args0);
    }

    @Data
    @AllArgsConstructor
    public static class PropertySignature {

        private String name;

        private Type type;

    }

}
