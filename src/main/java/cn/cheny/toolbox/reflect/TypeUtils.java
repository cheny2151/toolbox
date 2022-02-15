package cn.cheny.toolbox.reflect;

import cn.cheny.toolbox.exception.NotImplementedException;
import cn.cheny.toolbox.exception.ToolboxRuntimeException;
import cn.cheny.toolbox.other.DateUtils;
import cn.cheny.toolbox.property.token.ParseTokenException;
import cn.cheny.toolbox.reflect.methodHolder.ReadWriteMethodHolder;
import cn.cheny.toolbox.reflect.methodHolder.factory.ReadWriteMethodHolderFactory;

import java.lang.reflect.*;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 类型工具类
 *
 * @author cheney
 * @date 2019-12-11
 */
public class TypeUtils {

    private final static ReadWriteMethodHolderFactory methodHolderFactory = ReadWriteMethodHolderFactory.getInstance();

    /**
     * object转换为Type对应的类型
     *
     * @param obj     对象
     * @param objType 参数泛型
     * @param <T>     泛型
     * @return 转换后结果
     */
    @SuppressWarnings("unchecked")
    public static <T> T caseToObject(Object obj, Type objType) {
        if (obj == null) {
            return null;
        } else if (objType instanceof Class) {
            return caseToObject(obj, (Class<T>) objType);
        } else if (objType instanceof ParameterizedType) {
            return caseToObject(obj, (ParameterizedType) objType);
        } else if (objType instanceof TypeVariable) {
            return caseToObject(obj, (TypeVariable<?>) objType);
        } else if (objType instanceof WildcardType) {
            return caseToObject(obj, (WildcardType) objType);
        } else {
            throw new ParseTokenException(obj.getClass() + " can not cover to " + objType);
        }
    }

    /**
     * object转换为类型指引类对应的类型
     *
     * @param obj           数据
     * @param typeReference 类型指引类
     * @param <T>           泛型
     * @return 转换后结果
     */
    public static <T> T caseToObject(Object obj, TypeReference<T> typeReference) {
        Type actualType = typeReference.getActualType();
        return caseToObject(obj, actualType);
    }

    /**
     * map转换为object对象
     *
     * @param map     map实例
     * @param objType 对象类型
     * @param <T>     对象泛型
     * @return 对象实例
     */
    public static <T> T mapToObject(Map<String, Object> map, Class<T> objType) {
        ReadWriteMethodHolder methodHolder = methodHolderFactory.getMethodHolder(objType);
        T t = ReflectUtils.newObject(objType, null, null);
        methodHolder.getWritableProperties().forEach(signature -> {
            String name = signature.getName();
            Object fieldVal = map.get(name);
            if (fieldVal != null) {
                fieldVal = caseToObject(fieldVal, signature.getType());
                methodHolder.write(t, name, fieldVal);
            }
        });
        // 泛型处理
        List<Field> variableFields = ReflectUtils.getPropertyFields(objType, Object.class)
                .stream().filter(e -> e.getGenericType() instanceof TypeVariable)
                .collect(Collectors.toList());
        if (variableFields.size() > 0) {
            Map<TypeVariable<?>, Type> typeMap = TypeVariableParser.parse(objType);
            for (Field field : variableFields) {
                TypeVariable<?> fieldGenericType = (TypeVariable<?>) field.getGenericType();
                Type type = typeMap.get(fieldGenericType);
                if (type != null) {
                    Object val = ReflectUtils.getFieldVal(field, t);
                    if (val != null) {
                        Object newVal = caseToObject(val, type);
                        if (val != newVal) {
                            ReflectUtils.setFieldVal(field, t, newVal);
                        }
                    }
                }
            }
        }
        return t;
    }

    /**
     * object对象转换为Map
     *
     * @param obj      对象实例
     * @param mapClass map类型
     * @return map集合
     */
    public static Map<String, Object> objectToMap(Object obj, Class<? extends Map<String, Object>> mapClass) {
        Class<?> objClass = obj.getClass();
        ReadWriteMethodHolder methodHolder = methodHolderFactory.getMethodHolder(objClass);
        Map<String, Object> map = newMap(mapClass);
        methodHolder.getReadablePropertyNames().forEach(property -> map.put(property, methodHolder.read(obj, property)));
        return map;
    }

    /**
     * 将数组转换为集合
     *
     * @param array           数组
     * @param collectionClass 集合类型
     * @param tClass          元素类型
     * @param <T>             元素泛型
     * @return 集合
     */
    @SuppressWarnings("unchecked")
    public static <T> Collection<T> arrayToCollection(Object array, Class<?> collectionClass, Class<T> tClass) {
        Class<?> componentType = array.getClass().getComponentType();
        Collection<T> rs = newCollection((Class<? extends Collection<T>>) collectionClass);
        if (tClass.isAssignableFrom(componentType)) {
            rs.addAll(Arrays.asList(((T[]) array)));
        } else {
            int length = Array.getLength(array);
            for (int i = 0; i < length; i++) {
                Object o = Array.get(array, i);
                rs.add(caseToObject(o, tClass));
            }
        }
        return rs;
    }

    /**
     * 将集合转换为数组
     *
     * @param collection 集合
     * @param tClass     元素类型
     * @param <T>        元素泛型
     * @return 数组
     */
    @SuppressWarnings("unchecked")
    public static <T> T[] collectionToArray(Collection<?> collection, Class<T> tClass) {
        Class<?> class0 = TypeUtils.ifPrimitiveToWrapClass(tClass);
        Object array = Array.newInstance(class0, collection.size());
        int i = 0;
        for (Object o : collection) {
            T t = (T) caseToObject(o, class0);
            Array.set(array, i++, t);
        }
        return (T[]) array;
    }

    /**
     * 将集合转换为数组(无泛型)
     *
     * @param collection 集合
     * @param tClass     元素类型
     * @return 数组
     */
    public static Object collectionToArrayObject(Collection<?> collection, Class<?> tClass) {
        Object array = Array.newInstance(tClass, collection.size());
        int i = 0;
        for (Object o : collection) {
            Object element = caseToObject(o, tClass);
            Array.set(array, i++, element);
        }
        return array;
    }

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
                if (obj instanceof Integer || obj instanceof Long) {
                    int ObjAsInt = Integer.parseInt(obj.toString());
                    if (ObjAsInt == 1) {
                        return (T) Boolean.TRUE;
                    } else if (ObjAsInt == 0) {
                        return (T) Boolean.FALSE;
                    }
                } else {
                    return (T) Boolean.valueOf(obj.toString());
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
     * 若为基础类型则返回其包装类
     *
     * @param pc 类型
     * @return 返回对于的基础类型包装类
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

    /**
     * 获取父类typeReference上被子类clazz擦除的泛型实际类型
     *
     * @param childrenClass 子类
     * @param typeReference 存在泛型的父类
     * @return 擦除后的类型
     */
    public static Type[] getActualType(Class<?> childrenClass, Class<?> typeReference) {
        if (!typeReference.isAssignableFrom(childrenClass)) {
            throw new IllegalArgumentException(childrenClass + " is not a subclass of " + typeReference);
        }
        TypeVariable<? extends Class<?>>[] typeParameters = typeReference.getTypeParameters();
        if (typeParameters.length == 0) {
            throw new IllegalArgumentException(typeReference + " has not type variable");
        }

        Map<TypeVariable<?>, Type> typeMap = new HashMap<>();
        // extract all type map
        extractTypeMap(childrenClass, typeReference, typeMap);

        Type[] actualTypes = new Type[typeParameters.length];
        for (int i = 0; i < typeParameters.length; i++) {
            Type actualType = typeMap.get(typeParameters[i]);
            if (actualType == null) {
                throw new ToolboxRuntimeException("TypeReference fail to get actualType");
            }
            actualTypes[i] = actualType;
        }
        return actualTypes;
    }

    public static Class<?> getRawClass(Type type) {
        if (type instanceof Class) {
            return (Class<?>) type;
        } else if (type instanceof ParameterizedType) {
            return getRawClass(((ParameterizedType) type).getRawType());
        }
        throw new IllegalArgumentException();
    }

    private static void extractTypeMap(Class<?> clazz, Class<?> typeReference, Map<TypeVariable<?>, Type> typeMap) {
        if (clazz == null || typeReference.equals(clazz)) {
            return;
        }
        // genericSuperclass
        Type genericSuperclass = clazz.getGenericSuperclass();
        Class<?> next = extractType(genericSuperclass, typeMap);
        extractTypeMap(next, typeReference, typeMap);
        // genericInterfaces
        Type[] genericInterfaces = clazz.getGenericInterfaces();
        for (Type genericInterface : genericInterfaces) {
            next = extractType(genericInterface, typeMap);
            extractTypeMap(next, typeReference, typeMap);
        }
    }

    private static Class<?> extractType(Type type, Map<TypeVariable<?>, Type> typeMap) {
        Class<?> next = null;
        if (type instanceof ParameterizedType) {
            // genericSuperclass为ParameterizedType，尝试提取泛型参数
            ParameterizedType typeAsParameterized = (ParameterizedType) type;
            extractParameterizedType(typeAsParameterized, typeMap);
            next = (Class<?>) (typeAsParameterized).getRawType();
        } else if (type instanceof Class && !type.equals(Object.class)) {
            next = (Class<?>) type;
        }
        return next;
    }

    /**
     * 提取ParameterizedType的TypeArguments与ActualTypeArguments之间的映射关系
     *
     * @param classAsParameterized ParameterizedType
     * @param typeMap              已知泛型映射
     */
    static void extractParameterizedType(ParameterizedType classAsParameterized, Map<TypeVariable<?>, Type> typeMap) {
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

    /**
     * object转换为Class<T>类型
     *
     * @param obj     对象
     * @param objType 参数泛型
     * @param <T>     泛型
     * @return 转换后结果
     */
    @SuppressWarnings("unchecked")
    private static <T> T caseToObject(Object obj, Class<T> objType) {
        if (obj == null) {
            return null;
        }
        Class<?> class0 = obj.getClass();
        if (objType.isAssignableFrom(class0)) {
            return (T) obj;
        } else if (TypeUtils.isBaseClass(class0) && TypeUtils.isBaseClass(objType)) {
            return tryCoverBase(obj, objType);
        } else if (obj instanceof Map) {
            if (Map.class.isAssignableFrom(objType)) {
                return (T) coverMapInstance((Map<Object, Object>) obj, (Class<? extends Map<Object, Object>>) objType);
            } else if (!objType.isArray() &&
                    !Collection.class.isAssignableFrom(objType) &&
                    !TypeUtils.isBaseClass(objType)) {
                return mapToObject((Map<String, Object>) obj, objType);
            }
            throw new ParseTokenException(class0 + " can not cover to " + objType);
        } else if (Map.class.isAssignableFrom(objType) &&
                !class0.isArray() &&
                !Collection.class.isAssignableFrom(class0) &&
                !TypeUtils.isBaseClass(class0)) {
            return (T) objectToMap(obj, (Class<? extends Map<String, Object>>) objType);
        } else if (obj instanceof Collection && objType.isArray()) {
            return (T) collectionToArrayObject((Collection<?>) obj, objType.getComponentType());
        } else if (class0.isArray() && Collection.class.isAssignableFrom(objType)) {
            return (T) arrayToCollection(obj, objType, class0.getComponentType());
        }
        throw new ParseTokenException(class0 + " can not cover to " + objType);
    }

    /**
     * object转换为ParameterizedType对应的类型
     *
     * @param obj     对象
     * @param objType 参数泛型
     * @param <T>     泛型
     * @return 转换后结果
     */
    @SuppressWarnings("unchecked")
    private static <T> T caseToObject(Object obj, ParameterizedType objType) {
        Type rawType = objType.getRawType();
        Object obj0 = caseToObject(obj, rawType);
        if (obj0 == null) {
            return null;
        } else if (obj0 instanceof Map) {
            Type[] argTypes = objType.getActualTypeArguments();
            Map<Object, Object> map = (Map<Object, Object>) obj0;
            Map<Object, Object> results = newMap((Class<? extends Map<Object, Object>>) map.getClass());
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                results.put(caseToObject(entry.getKey(), argTypes[0]),
                        caseToObject(entry.getValue(), argTypes[1]));
            }
            return (T) results;
        } else if (obj0 instanceof Collection) {
            Type[] argTypes = objType.getActualTypeArguments();
            Collection<Object> col = (Collection<Object>) obj0;
            Collection<Object> results = newCollection((Class<? extends Collection<Object>>) col.getClass());
            for (Object e : col) {
                Object o = caseToObject(e, argTypes[0]);
                results.add(o);
            }
            return (T) results;
        } else {
            Type[] argTypes = objType.getActualTypeArguments();
            TypeVariable<? extends Class<?>>[] superTypeVariables = obj0.getClass().getTypeParameters();
            List<Field> fields = ReflectUtils.getPropertyFields(obj0.getClass(), Object.class);
            for (Field field : fields) {
                if (field.getGenericType() instanceof TypeVariable) {
                    Integer typeIdx = null;
                    for (int i = 0; i < superTypeVariables.length; i++) {
                        if (superTypeVariables[i].equals(field.getGenericType())) {
                            typeIdx = i;
                        }
                    }
                    if (typeIdx == null) {
                        continue;
                    }
                    Object fv = ReflectUtils.getFieldVal(field, obj0);
                    Object newVal = caseToObject(fv, argTypes[typeIdx]);
                    if (newVal != fv) {
                        ReflectUtils.setFieldVal(field, obj0, newVal);
                    }
                }
            }
            return (T) obj0;
        }
    }

    /**
     * todo
     */
    private static <T> T caseToObject(Object obj, TypeVariable<?> objType) {
        throw new NotImplementedException("not implement");
    }

    /**
     * todo
     */
    private static <T> T caseToObject(Object obj, WildcardType objType) {
        throw new NotImplementedException("not implement");
    }

    /**
     * 转换Map实现类
     */
    @SuppressWarnings("unchecked")
    private static <T extends Map<Object, Object>> T coverMapInstance(Map<Object, Object> obj, Class<T> objType) {
        Map<Object, Object> newMap = newMap(objType);
        newMap.putAll(obj);
        return (T) newMap;
    }

    /**
     * Map子类实例化
     *
     * @param mapClass map class
     * @param <K>      key
     * @param <V>      value
     * @return Map实例
     */
    private static <K, V> Map<K, V> newMap(Class<? extends Map<K, V>> mapClass) {
        if (!mapClass.isInterface()) {
            try {
                return ReflectUtils.newObject(mapClass, null, null);
            } catch (Exception e) {
                // try next
            }
        }
        if (Map.class.isAssignableFrom(mapClass)) {
            return new HashMap<>();
        } else {
            throw new ToolboxRuntimeException("not support type interface " + mapClass.getName());
        }
    }

    /**
     * Collection子类实例化
     *
     * @param collectionClass Collection class
     * @return Collection实例
     */
    private static <T> Collection<T> newCollection(Class<? extends Collection<T>> collectionClass) {
        if (!collectionClass.isInterface()) {
            try {
                return ReflectUtils.newObject(collectionClass, null, null);
            } catch (Exception e) {
                // try next
            }
        }
        if (List.class.isAssignableFrom(collectionClass)) {
            return new ArrayList<>();
        } else if (Set.class.isAssignableFrom(collectionClass)) {
            return new HashSet<>();
        } else {
            throw new ToolboxRuntimeException("not support type interface " + collectionClass.getName());
        }
    }

}
