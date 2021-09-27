package cn.cheny.toolbox.reflect;

import cn.cheny.toolbox.exception.ToolboxRuntimeException;
import cn.cheny.toolbox.other.DateUtils;
import cn.cheny.toolbox.property.token.ParseTokenException;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * 类型工具类
 *
 * @author cheney
 * @date 2019-12-11
 */
public class TypeUtils {


    /**
     * 判断是否是基础类型，或者基础类型的包装类，或者BigDecimal，Date
     *
     * @param aClass 类型
     * @return 是否基础类型
     */
    public static boolean isBaseClass(Class<?> aClass) {
        return aClass.isPrimitive()
                || aClass.equals(Integer.class)
                || aClass.equals(Short.class)
                || aClass.equals(Double.class)
                || aClass.equals(Float.class)
                || aClass.equals(String.class)
                || aClass.equals(Boolean.class)
                || aClass.equals(Long.class)
                || aClass.equals(Byte.class)
                || aClass.equals(BigDecimal.class)
                || aClass.equals(Date.class);
    }

    /**
     * 尝试基础类型数据的转换
     *
     * @param obj         对应的值
     * @param targetClass 目标转换类型
     * @param <T>         对应泛型
     * @return 转换结果
     */
    @SuppressWarnings("unchecked")
    public static <T> T tryCoverBase(Object obj, Class<T> targetClass) {
        if (obj == null) {
            return null;
        }
        if (targetClass.equals(obj.getClass())) {
            return (T) obj;
        }
        try {
            if (targetClass.equals(int.class) || targetClass.equals(Integer.class)) {
                return (T) Integer.valueOf(obj.toString());
            } else if (targetClass.equals(short.class) || targetClass.equals(Short.class)) {
                return (T) Short.valueOf(obj.toString());
            } else if (targetClass.equals(double.class) || targetClass.equals(Double.class)) {
                return (T) Double.valueOf(obj.toString());
            } else if (targetClass.equals(float.class) || targetClass.equals(Float.class)) {
                return (T) Float.valueOf(obj.toString());
            } else if (targetClass.equals(boolean.class) || targetClass.equals(Boolean.class)) {
                if (obj instanceof String) {
                    return (T) Boolean.valueOf((String) obj);
                }
            } else if (targetClass.equals(long.class) || targetClass.equals(Long.class)) {
                return (T) Long.valueOf(obj.toString());
            } else if (targetClass.equals(byte.class) || targetClass.equals(Byte.class)) {
                return (T) Byte.valueOf(obj.toString());
            } else if (targetClass.equals(char.class) || targetClass.equals(Character.class)) {
                return (T) Character.valueOf((char) obj);
            } else if (targetClass.equals(String.class)) {
                return (T) obj.toString();
            } else if (targetClass.equals(BigDecimal.class)) {
                return (T) new BigDecimal(obj.toString());
            } else if (targetClass.equals(Date.class)) {
                return (T) DateUtils.toDate(obj);
            }
        } catch (Exception e) {
            // do nothing
        }
        throw new ParseTokenException(obj.getClass() + " can not cover to " + targetClass);
    }

    /**
     * 判断是否是基础类型
     *
     * @param pc 类型
     * @return
     */
    public static Class<?> ifPrimitiveToWrapClass(Class<?> pc) {
        if (int.class.equals(pc)) {
            return Integer.class;
        } else if (long.class.equals(pc)) {
            return Long.class;
        } else if (float.class.equals(pc)) {
            return Float.class;
        } else if (double.class.equals(pc)) {
            return Double.class;
        } else if (char.class.equals(pc)) {
            return Character.class;
        } else if (byte.class.equals(pc)) {
            return Byte.class;
        } else if (boolean.class.equals(pc)) {
            return Boolean.class;
        } else if (short.class.equals(pc)) {
            return Short.class;
        }
        return pc;
    }

    public static Type[] getActualType(Class<?> clazz, Class<?> typeReference) {
        if (!typeReference.isAssignableFrom(clazz)) {
            throw new IllegalArgumentException(clazz + " is not a subclass of " + typeReference);
        }
        if (typeReference.getTypeParameters().length == 0) {
            throw new IllegalArgumentException(typeReference + " has not type variable");
        }
        Map<TypeVariable<?>, Type> typeMap = new HashMap<>();
        return getActualType(clazz, typeReference, typeMap);
    }

    private static Type[] getActualType(Class<?> clazz, Class<?> typeReference, Map<TypeVariable<?>, Type> typeMap) {
        Type genericSuperclass = clazz.getGenericSuperclass();
        Class<?> next = null;
        if (genericSuperclass instanceof ParameterizedType) {
            // genericSuperclass为ParameterizedType，尝试提取泛型参数
            ParameterizedType typeAsParameterized = (ParameterizedType) genericSuperclass;
            extractTypeMap(typeAsParameterized, typeMap);
            next = (Class<?>) (typeAsParameterized).getRawType();
            if (typeReference.equals(next)) {
                // genericSuperclass命中TypeReference，尝试从typeMap中获取actualType
                TypeVariable<?>[] typeParameters = typeReference.getTypeParameters();
                Type[] actualTypes = new Type[typeParameters.length];
                for (int i = 0; i < typeParameters.length; i++) {
                    TypeVariable<?> typeParameter = typeParameters[i];
                    actualTypes[i] = typeMap.get(typeParameter);
                }
                return actualTypes;
            }
        } else if (genericSuperclass instanceof Class) {
            next = (Class<?>) genericSuperclass;
        }

        if (next == null) {
            throw new ToolboxRuntimeException("TypeReference fail to get actualType");
        }

        return getActualType(next, typeReference, typeMap);
    }

    /**
     * 提取ParameterizedType的TypeArguments与ActualTypeArguments之间的映射关系
     *
     * @param classAsParameterized ParameterizedType
     * @param typeMap              已知泛型映射
     */
    static void extractTypeMap(ParameterizedType classAsParameterized, Map<TypeVariable<?>, Type> typeMap) {
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

}
