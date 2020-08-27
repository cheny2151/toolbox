package cn.cheny.toolbox.reflect.methodHolder;

import cn.cheny.toolbox.reflect.ReflectUtils;

import java.util.Collection;

/**
 * 读写方法持有者
 *
 * @author cheney
 * @date 2020-04-20
 */
public class ReadWriteMethodHolder extends BaseMethodHolder {

    public ReadWriteMethodHolder(Class<?> clazz) {
        super(clazz);
        ReflectUtils.getAllReadMethod(clazz, Object.class).values().forEach(method -> cacheMethod(ReflectUtils.extractPropertyName(method), method));
        ReflectUtils.getAllWriterMethod(clazz, Object.class).values().forEach(method -> cacheMethod(ReflectUtils.extractPropertyName(method), method));
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

}
