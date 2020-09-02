package cn.cheny.toolbox.entityCache.factory;

import cn.cheny.toolbox.entityCache.buffer.EntityBuffer;
import cn.cheny.toolbox.entityCache.buffer.MemoryEntityBuffer;

/**
 * 默认entity buffer工厂实现 -- 内存模式
 *
 * @author cheney
 * @date 2020-09-02
 */
public class DefaultEntityBufferFactory extends BaseEntityBufferFactory {

    public DefaultEntityBufferFactory(boolean underline) {
        super(underline);
    }

    public <T> EntityBuffer<T> createBuffer(Class<T> clazz) {
        return new MemoryEntityBuffer<>(clazz, isUnderline());
    }

}
