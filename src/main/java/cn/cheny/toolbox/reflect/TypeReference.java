package cn.cheny.toolbox.reflect;

import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Type;

/**
 * 泛型信息接口
 *
 * @author cheney
 * @date 2019/6/27
 */
@Slf4j
public abstract class TypeReference<T> {

    private final Type actualType;

    public TypeReference() {
        this.actualType = getActualType(this.getClass());
    }

    private Type getActualType(Class<?> clazz) {
        Type[] actualType = TypeUtils.getActualType(clazz, TypeReference.class);
        return actualType[0];
    }

    public Type getActualType() {
        return actualType;
    }
}
