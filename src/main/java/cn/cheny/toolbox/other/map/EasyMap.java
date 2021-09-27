package cn.cheny.toolbox.other.map;

import cn.cheny.toolbox.exception.NotImplementedException;
import cn.cheny.toolbox.exception.ToolboxRuntimeException;
import cn.cheny.toolbox.property.token.ParseTokenException;
import cn.cheny.toolbox.property.token.TokenParser;
import cn.cheny.toolbox.reflect.ReflectUtils;
import cn.cheny.toolbox.reflect.TypeReference;
import cn.cheny.toolbox.reflect.TypeUtils;
import cn.cheny.toolbox.reflect.TypeVariableParser;

import java.lang.reflect.*;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 简化map获取操作
 *
 * @author by chenyi
 * @date 2021/2/8
 */
@SuppressWarnings("unchecked")
public class EasyMap extends HashMap<String, Object> {

    private final Map<Class<?>, Map<String, Method>> typeWriterCache = new ConcurrentHashMap<>();

    private final Map<Class<?>, Map<String, Method>> typeReaderCache = new ConcurrentHashMap<>();

    public EasyMap(int initialCapacity, float loadFactor) {
        super(initialCapacity, loadFactor);
    }

    public EasyMap(int initialCapacity) {
        super(initialCapacity);
    }

    public EasyMap() {
    }

    public EasyMap(Map<? extends String, ?> m) {
        super(m);
    }

    public <T> T toObject(Class<T> tClass) {
        return caseToObject("", this, tClass);
    }

    public <T> T toObject(TypeReference<T> typeReference) {
        Type actualType = typeReference.getActualType();
        return caseToObject("", this, actualType);
    }

    public String getString(String key) {
        return (String) getObject(key);
    }

    public Integer getInteger(String key) {
        Object val = getObject(key);
        return tryCoverBase(key, val, Integer.class);
    }

    public Long getLong(String key) {
        Object val = getObject(key);
        return tryCoverBase(key, val, Long.class);
    }

    public Boolean getBoolean(String key) {
        Object val = getObject(key);
        return tryCoverBase(key, val, Boolean.class);
    }

    public Short getShort(String key) {
        Object val = getObject(key);
        return tryCoverBase(key, val, Short.class);
    }

    public Character getCharacter(String key) {
        Object val = getObject(key);
        return tryCoverBase(key, val, Character.class);
    }

    public BigDecimal getBigDecimal(String key) {
        Object val = getObject(key);
        return tryCoverBase(key, val, BigDecimal.class);
    }

    public Date getDate(String key) {
        Object val = getObject(key);
        return tryCoverBase(key, val, Date.class);
    }

    public <T> T[] getArray(String key, Class<T> tClass) {
        Object val = getObject(key);
        if (val == null) {
            return null;
        } else if (val.getClass().isArray()) {
            Class<?> componentType = val.getClass().getComponentType();
            if (tClass.isAssignableFrom(componentType)) {
                return (T[]) val;
            } else {
                int length = Array.getLength(val);
                Class<?> class0 = TypeUtils.ifPrimitiveToWrapClass(tClass);
                Object array = Array.newInstance(class0, length);
                for (int i = 0; i < length; i++) {
                    Array.set(array, i, caseToObject(key, Array.get(val, 0), tClass));
                }
                return (T[]) array;
            }
        } else if (val instanceof Collection) {
            Collection<?> collection = (Collection<?>) val;
            return collectionToArray(key, collection, tClass);
        }
        throw new ParseTokenException("property '" + key + "' is " + val.getClass() + " ,expect collection or array");
    }

    public <T> List<T> getList(String key, Class<T> tClass) {
        Object val = getObject(key);
        if (val == null) {
            return null;
        } else if (val.getClass().isArray()) {
            Object[] array = (Object[]) val;
            Class<?> componentType = array.getClass().getComponentType();
            if (tClass.isAssignableFrom(componentType)) {
                return Stream.of((T[]) val).collect(Collectors.toList());
            } else {
                return (List<T>) arrayToList(key, val, ArrayList.class, tClass);
            }
        } else if (val instanceof Collection) {
            Collection<?> collection = (Collection<?>) val;
            List<T> list = new ArrayList<>();
            for (Object o : collection) {
                list.add(caseToObject(key, o, tClass));
            }
            return list;
        }
        throw new ParseTokenException("property '" + key + "' is " + val.getClass() + " ,expect collection or array");
    }

    public EasyMap getMap(String key) {
        Object val = getObject(key);
        return caseToObject(key, val, EasyMap.class);
    }

    public <T> T getObject(String key, Class<T> tClass) {
        Object val = getObject(key);
        return caseToObject(key, val, tClass);
    }

    public <T> T getObject(String key, TypeReference<T> typeReference) {
        Object object = getObject(key);
        Type actualType = typeReference.getActualType();
        return caseToObject(key, object, actualType);
    }

    /**
     * 获取key对应的值（支持a.b.c直接获取c）
     *
     * @param key 属性key（支持多级：a.b.c）
     * @return 获取结果
     */
    public Object getObject(String key) {
        TokenParser tokenParser = new TokenParser(key);
        Object cur = this;
        do {
            Class<?> curClass = cur.getClass();
            String property = tokenParser.getProperty();
            if (cur instanceof Map) {
                cur = ((Map<Object, Object>) cur).get(property);
            } else if (cur instanceof Collection || curClass.isArray() || curClass.isPrimitive()) {
                throw new ParseTokenException("property '" + property + "' is " + curClass + " ,expect map or object");
            } else {
                cur = ReflectUtils.readValue(cur, property);
            }
            if (tokenParser.isCollection()) {
                for (Integer index : tokenParser.getCollectionIndexes()) {
                    if (cur instanceof Collection) {
                        cur = ((Collection<?>) cur).toArray()[index];
                    } else if (cur.getClass().isArray()) {
                        cur = ((Object[]) cur)[index];
                    } else {
                        throw new ParseTokenException("property '" + property + "' is " + cur.getClass() + " ,expect collection or array");
                    }
                }
            }
            if (cur == null) {
                return null;
            }
        } while ((tokenParser = tokenParser.next()) != null);
        return cur;
    }

    /**
     * object转换为Type对应的类型
     *
     * @param property 属性key
     * @param obj      对象
     * @param objType  参数泛型
     * @param <T>      泛型
     * @return 转换后结果
     */
    private <T> T caseToObject(String property, Object obj, Type objType) {
        if (obj == null) {
            return null;
        } else if (objType instanceof Class) {
            return caseToObject(property, obj, (Class<T>) objType);
        } else if (objType instanceof ParameterizedType) {
            return caseToObject(property, obj, (ParameterizedType) objType);
        } else if (objType instanceof TypeVariable) {
            return caseToObject(property, obj, (TypeVariable<?>) objType);
        } else if (objType instanceof WildcardType) {
            return caseToObject(property, obj, (WildcardType) objType);
        } else {
            throw new ParseTokenException("property '" + property + "' is " + obj.getClass() + " ,expect " + objType);
        }
    }

    /**
     * object转换为Class<T>类型
     *
     * @param property 属性key
     * @param obj      对象
     * @param objType  参数泛型
     * @param <T>      泛型
     * @return 转换后结果
     */
    private <T> T caseToObject(String property, Object obj, Class<T> objType) {
        if (obj == null) {
            return null;
        }
        Class<?> class0 = obj.getClass();
        if (objType.isAssignableFrom(class0)) {
            return (T) obj;
        } else if (TypeUtils.isBaseClass(class0) && TypeUtils.isBaseClass(objType)) {
            return tryCoverBase(property, obj, objType);
        } else if (obj instanceof Map) {
            if (Map.class.isAssignableFrom(objType)) {
                return (T) coverMapInstance((Map<Object, Object>) obj, (Class<? extends Map<Object, Object>>) objType);
            } else if (!objType.isArray() &&
                    !Collection.class.isAssignableFrom(objType) &&
                    !TypeUtils.isBaseClass(objType)) {
                return mapToObject((Map<String, Object>) obj, objType);
            }
            throw new ParseTokenException("property '" + property + "' is " + class0 + " ,expect " + objType);
        } else if (Map.class.isAssignableFrom(objType) &&
                !class0.isArray() &&
                !Collection.class.isAssignableFrom(class0) &&
                !TypeUtils.isBaseClass(class0)) {
            return (T) objectToMap(obj, (Class<? extends Map<String, Object>>) objType);
        } else if (obj instanceof Collection && objType.isArray()) {
            return (T) collectionToArray(property, (Collection<?>) obj, objType.getComponentType());
        } else if (class0.isArray() && Collection.class.isAssignableFrom(objType)) {
            return (T) arrayToList(property, obj, objType, class0.getComponentType());
        }
        throw new ParseTokenException("property '" + property + "' is " + class0 + " ,expect " + objType);
    }

    /**
     * object转换为ParameterizedType对应的类型
     *
     * @param property 属性key
     * @param obj      对象
     * @param objType  参数泛型
     * @param <T>      泛型
     * @return 转换后结果
     */
    private <T> T caseToObject(String property, Object obj, ParameterizedType objType) {
        Type rawType = objType.getRawType();
        Object obj0 = caseToObject(property, obj, rawType);
        if (obj0 == null) {
            return null;
        } else if (obj0 instanceof Map) {
            Type[] argTypes = objType.getActualTypeArguments();
            Map<Object, Object> map = (Map<Object, Object>) obj0;
            Map<Object, Object> results = newMap((Class<? extends Map<Object, Object>>) map.getClass());
            for (Entry<?, ?> entry : map.entrySet()) {
                results.put(caseToObject(property, entry.getKey(), argTypes[0]),
                        caseToObject(property, entry.getValue(), argTypes[1]));
            }
            return (T) results;
        } else if (obj0 instanceof Collection) {
            Type[] argTypes = objType.getActualTypeArguments();
            Collection<Object> col = (Collection<Object>) obj0;
            Collection<Object> results = newCollection((Class<? extends Collection<Object>>) col.getClass());
            for (Object e : col) {
                Object o = caseToObject(property, e, argTypes[0]);
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
                    Object newVal = caseToObject(property, fv, argTypes[typeIdx]);
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
    private <T> T caseToObject(String property, Object obj, TypeVariable<?> objType) {
        throw new NotImplementedException("not implement");
    }

    /**
     * todo
     */
    private <T> T caseToObject(String property, Object obj, WildcardType objType) {
        throw new NotImplementedException("not implement");
    }

    private <T> T mapToObject(Map<String, Object> map, Class<T> objType) {
        Map<String, Method> writerMethod =
                typeWriterCache.computeIfAbsent(objType, k -> ReflectUtils.getAllWriterMethod(objType, Object.class));
        T t = ReflectUtils.newObject(objType, null, null);
        writerMethod.forEach((f, m) -> {
            Object fieldVal = map.get(f);
            if (fieldVal != null) {
                ReflectUtils.writeValue(t, m, fieldVal);
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
                        Object newVal = caseToObject(field.getName(), val, type);
                        if (val != newVal) {
                            ReflectUtils.setFieldVal(field, t, newVal);
                        }
                    }
                }
            }
        }
        return t;
    }

    private Map<String, Object> objectToMap(Object obj, Class<? extends Map<String, Object>> mapClass) {
        Class<?> objClass = obj.getClass();
        Map<String, Method> writerMethod =
                typeReaderCache.computeIfAbsent(objClass, k -> ReflectUtils.getAllReadMethod(objClass, Object.class));
        Map<String, Object> map = newMap(mapClass);
        writerMethod.forEach((f, m) -> map.put(f, ReflectUtils.readValue(obj, m)));
        return map;
    }

    /**
     * 转换Map实现类
     */
    private <T extends Map<Object, Object>> T coverMapInstance(Map<Object, Object> obj, Class<T> objType) {
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
    private <K, V> Map<K, V> newMap(Class<? extends Map<K, V>> mapClass) {
        if (mapClass.isInterface()) {
            if (!mapClass.equals(Map.class)) {
                throw new ToolboxRuntimeException("not support type interface " + mapClass.getName());
            } else {
                return new HashMap<>();
            }
        }
        try {
            return ReflectUtils.newObject(mapClass, null, null);
        } catch (Exception e) {
            return new HashMap<>();
        }
    }

    private <T> Collection<T> arrayToList(String property, Object array, Class<?> collectionClass, Class<T> tClass) {
        Class<?> componentType = array.getClass().getComponentType();
        Collection<T> rs = newCollection((Class<? extends Collection<T>>) collectionClass);
        if (tClass.isAssignableFrom(componentType)) {
            rs.addAll(Arrays.asList(((T[]) array)));
        } else {
            int length = Array.getLength(array);
            for (int i = 0; i < length; i++) {
                Object o = Array.get(array, i);
                rs.add(caseToObject(property, o, tClass));
            }
        }
        return rs;
    }

    /**
     * Collection子类实例化
     *
     * @param collectionClass Collection class
     * @return Collection实例
     */
    private <T> Collection<T> newCollection(Class<? extends Collection<T>> collectionClass) {
        if (collectionClass.isInterface()) {
            if (collectionClass.equals(List.class)) {
                return new ArrayList<>();
            } else if (collectionClass.equals(Set.class)) {
                return new HashSet<>();
            } else {
                throw new ToolboxRuntimeException("not support type interface " + collectionClass.getName());
            }
        }
        return ReflectUtils.newObject(collectionClass, null, null);
    }

    private <T> T[] collectionToArray(String property, Collection<?> collection, Class<T> tClass) {
        Class<?> class0 = TypeUtils.ifPrimitiveToWrapClass(tClass);
        Object array = Array.newInstance(class0, collection.size());
        int i = 0;
        for (Object o : collection) {
            T t = (T) caseToObject(property, o, class0);
            Array.set(array, i++, t);
        }
        return (T[]) array;
    }

    /**
     * 尝试基础类型数据的转换
     *
     * @param property    属性key
     * @param obj         对应的值
     * @param targetClass 目标转换类型
     * @param <T>         对应泛型
     * @return 转换结果
     */
    private static <T> T tryCoverBase(String property, Object obj, Class<T> targetClass) {
        try {
            return TypeUtils.tryCoverBase(obj, targetClass);
        } catch (ParseTokenException e) {
            throw new ParseTokenException("property '" + property + "' is " + obj.getClass() + " ,expect " + targetClass);
        }
    }

}
