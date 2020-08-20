package cn.cheny.toolbox.reflect.methodHolder;

import cn.cheny.toolbox.reflect.ReflectUtils;

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

    public Object read(Object obj, String property) {
        return invoke(property, obj);
    }

    public void write(Object obj, String property, Object value) {
        invoke(property, obj, value);
    }

}
