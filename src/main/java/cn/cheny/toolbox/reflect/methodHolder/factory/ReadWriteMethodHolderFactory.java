package cn.cheny.toolbox.reflect.methodHolder.factory;

import cn.cheny.toolbox.reflect.methodHolder.ReadWriteMethodHolder;

/**
 * MethodHolder工厂类
 *
 * @author cheney
 * @date 2019-12-05
 */
public class ReadWriteMethodHolderFactory extends DefaultMethodHolderFactory {

    // 全局静态工厂
    private final static ReadWriteMethodHolderFactory GLOBAL_READ_WRITE_METHOD_HOLDER_FACTORY = new ReadWriteMethodHolderFactory();

    public ReadWriteMethodHolder getMethodHolder(Class<?> clazz) {
        return (ReadWriteMethodHolder) methodHolderCache.computeIfAbsent(clazz, key -> registeredClass(clazz));
    }

    public ReadWriteMethodHolder registeredClass(Class<?> clazz) {
        return (ReadWriteMethodHolder) registeredClass(clazz, ReadWriteMethodHolder.class);
    }

    /**
     * 获取全局实例
     *
     * @return 工厂实例
     */
    public static ReadWriteMethodHolderFactory getInstance() {
        return GLOBAL_READ_WRITE_METHOD_HOLDER_FACTORY;
    }

}
