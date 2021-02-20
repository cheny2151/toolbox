package cn.cheny.toolbox.reflect;

import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * 泛型信息接口
 *
 * @author cheney
 * @date 2019/6/27
 */
@Slf4j
public abstract class TypeReference<T> {

    private final Type actualType;

    private final Map<TypeVariable<?>, Type> typeMap;

    public TypeReference() {
        this.typeMap = new HashMap<>();
        this.actualType = getActualType(this.getClass());
    }

    private Type getActualType(Class<?> clazz) {
        Type genericSuperclass = clazz.getGenericSuperclass();
        Class<?> next = null;
        if (genericSuperclass instanceof ParameterizedType) {
            // genericSuperclass为ParameterizedType，尝试提取泛型参数
            ParameterizedType typeAsParameterized = (ParameterizedType) genericSuperclass;
            extractTypeMap(typeAsParameterized);
            next = (Class<?>) (typeAsParameterized).getRawType();
            if (TypeReference.class.equals(next)) {
                // genericSuperclass命中TypeReference，尝试从typeMap中获取actualType
                TypeVariable<?> typeParameter = next.getTypeParameters()[0];
                Optional<Map.Entry<TypeVariable<?>, Type>> actualType =
                        typeMap.entrySet().stream().filter(entry -> entry.getKey().equals(typeParameter)).findFirst();
                if (actualType.isPresent()) {
                    return actualType.get().getValue();
                }
            }
        } else if (genericSuperclass instanceof Class) {
            next = (Class<?>) genericSuperclass;
        }

        if (next == null) {
            throw new RuntimeException("TypeReference fail to get actualType");
        }

        return getActualType(next);
    }

    /**
     * 提取ParameterizedType的TypeArguments与ActualTypeArguments之间的映射关系
     *
     * @param classAsParameterized ParameterizedType
     */
    private void extractTypeMap(ParameterizedType classAsParameterized) {
        Type[] actualTypeArguments = classAsParameterized.getActualTypeArguments();
        Class<?> rawType = (Class<?>) classAsParameterized.getRawType();
        TypeVariable<? extends Class<?>>[] typeParameters = rawType.getTypeParameters();
        for (int i = 0; i < typeParameters.length; i++) {
            Type actualTypeArgument = actualTypeArguments[i];
            if (actualTypeArgument instanceof TypeVariable) {
                // 若真实类型actualTypeArgument为TypeVariable，则尝试在typeMap中查找Key为该TypeVariable的value替换actualTypeArgument
                // 这种情景为：父类ParameterizedType的真实泛型(ActualTypeArgument)是子类ParameterizedType的泛型参数(TypeParameter),
                // 通过这个泛型参数(TypeParameter)传递子类真实参数给父类
                Optional<Type> typeChange = Optional.empty();
                for (Map.Entry<TypeVariable<?>, Type> entry : typeMap.entrySet()) {
                    if (entry.getKey().equals(actualTypeArgument)) {
                        typeChange = Optional.of(entry.getValue());
                        log.debug(classAsParameterized.getTypeName() + ":change before:" + actualTypeArgument + " after:" + entry.getValue());
                        break;
                    }
                }
                if (typeChange.isPresent()) {
                    actualTypeArgument = typeChange.get();
                }
            }
            typeMap.put(typeParameters[i], actualTypeArgument);
        }
    }

    public Type getActualType() {
        return actualType;
    }
}
