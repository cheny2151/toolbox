package cn.cheny.toolbox.entityCache.factory;

import cn.cheny.toolbox.entityCache.buffer.EntityBuffer;

/**
 * @author cheney
 * @date 2020-08-31
 */
public interface EntityBufferFactory {

    <T> EntityBuffer<T> createBuffer(Class<T> clazz);

}
