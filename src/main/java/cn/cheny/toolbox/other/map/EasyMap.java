package cn.cheny.toolbox.other.map;

import cn.cheny.toolbox.other.DateUtils;
import cn.cheny.toolbox.other.page.Limit;
import cn.cheny.toolbox.property.token.ParseTokenException;
import cn.cheny.toolbox.property.token.TokenParser;
import cn.cheny.toolbox.reflect.ReflectUtils;
import cn.cheny.toolbox.reflect.TypeReference;

import java.lang.reflect.*;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 简化map获取操作
 *
 * @Date 2021/2/8
 * @Created by chenyi
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
                Class<?> class0 = ifPrimitiveToWrapClass(tClass);
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
                return arrayToList(key, val, tClass);
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
        if (val == null) {
            return null;
        } else if (val instanceof Map) {
            Map<String, Object> map = (Map<String, Object>) val;
            if (map instanceof EasyMap) {
                return (EasyMap) map;
            }
            return new EasyMap(map);
        } else if (!isBaseClass(val.getClass()) &&
                !val.getClass().isArray() &&
                !(val instanceof Collection)) {
            return (EasyMap) objectToMap(val);
        }
        throw new ParseTokenException("property '" + key + "' is " + val.getClass() + " ,expect map");
    }

    public <T> T getObject(String key, Class<T> tClass) {
        Object val = getObject(key);
        return caseToObject(key, val, tClass);
    }

    public Object getObject(String key) {
        TokenParser tokenParser = new TokenParser(key);
        Object cur = this;
        do {
            Class<?> curClass = cur.getClass();
            String property = tokenParser.getProperty();
            if (cur instanceof Map) {
                cur = ((Map<String, Object>) cur).get(property);
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
        } while ((tokenParser = tokenParser.next()) != null);
        return cur;
    }

    public <T> T getObject(String key, TypeReference<T> typeReference) {
        Object object = getObject(key);
        Type actualType = typeReference.getActualType();
        return caseToObject(key, object, actualType);
    }

    private <T> T caseToObject(String property, Object obj, Type objType) {
        if (obj == null) {
            return null;
        } else if (objType instanceof Class) {
            return caseToObject(property, obj, (Class<T>) objType);
        } else if (obj instanceof ParameterizedType) {
            return caseToObject(property, obj, (ParameterizedType) objType);
        } else if (obj instanceof TypeVariable) {
            return caseToObject(property, obj, (TypeVariable<?>) objType);
        } else if (obj instanceof WildcardType) {
            return caseToObject(property, obj, (WildcardType) objType);
        } else {
            throw new ParseTokenException("property '" + property + "' is " + obj.getClass() + " ,expect " + objType);
        }
    }

    private <T> T caseToObject(String property, Object obj, Class<T> objType) {
        if (obj == null) {
            return null;
        } else if (objType.isAssignableFrom(obj.getClass())) {
            return (T) obj;
        } else if (isBaseClass(obj.getClass()) && isBaseClass(objType)) {
            return tryCoverBase(property, obj, objType);
        } else if (obj instanceof Map && !objType.isArray() &&
                !Collection.class.isAssignableFrom(objType) &&
                !isBaseClass(objType)) {
            return mapToObject((Map<String, Object>) obj, objType);
        } else if (Map.class.isAssignableFrom(objType) &&
                !obj.getClass().isArray() &&
                !Collection.class.isAssignableFrom(obj.getClass()) &&
                !isBaseClass(obj.getClass())) {
            return (T) objectToMap(obj);
        } else if (obj instanceof Collection && objType.isArray()) {
            return (T) collectionToArray(property, (Collection<?>) obj, objType.getComponentType());
        } else if (obj.getClass().isArray() && Collection.class.isAssignableFrom(objType)) {
            return (T) arrayToList(property, obj, obj.getClass().getComponentType());
        } else {
            throw new ParseTokenException("property '" + property + "' is " + obj.getClass() + " ,expect " + objType);
        }
    }

    private <T> T caseToObject(String property, Object obj, ParameterizedType objType) {
        Type rawType = objType.getRawType();
        Object obj0 = caseToObject(property, obj, rawType);
        if (obj0 == null) {
            return null;
        } else if (obj0 instanceof Map) {
            Type[] argTypes = objType.getActualTypeArguments();
            Map<?, ?> map = (Map<?, ?>) obj0;
            Map<Object, Object> results = map.entrySet().stream()
                    .collect(Collectors.toMap(entry -> caseToObject(property, entry.getKey(), argTypes[0]),
                            entry -> caseToObject(property, entry.getValue(), argTypes[1])));
            return (T) results;
        } else if (obj0 instanceof Collection) {
            Type[] argTypes = objType.getActualTypeArguments();
            Collection<?> map = (Collection<?>) obj0;
            Collection<Object> results = map.stream()
                    .map(e -> caseToObject(property, e, argTypes[0]))
                    .collect(Collectors.toList());
            return (T) results;
        } else {
            // todo object
            throw new ParseTokenException("property '" + property + "' is " + obj.getClass() + " ,expect " + objType);
        }
    }

    private <T> T caseToObject(String property, Object obj, TypeVariable<?> objType) {
        if (obj == null) {
            return null;
        } else if (false) {
            return null;
        } else {
            throw new ParseTokenException("property '" + property + "' is " + obj.getClass() + " ,expect " + objType);
        }
    }

    private <T> T caseToObject(String property, Object obj, WildcardType objType) {
        if (obj == null) {
            return null;
        } else if (false) {
            return null;
        } else {
            throw new ParseTokenException("property '" + property + "' is " + obj.getClass() + " ,expect " + objType);
        }
    }

    private boolean isBaseClass(Class<?> aClass) {
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

    private static <T> T tryCoverBase(String property, Object obj, Class<T> targetClass) {
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
        throw new ParseTokenException("property '" + property + "' is " + obj.getClass() + " ,expect " + targetClass);
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
        return t;
    }

    private Map<String, Object> objectToMap(Object obj) {
        Class<?> objClass = obj.getClass();
        Map<String, Method> writerMethod =
                typeReaderCache.computeIfAbsent(objClass, k -> ReflectUtils.getAllReadMethod(objClass, Object.class));
        Map<String, Object> map = new EasyMap();
        writerMethod.forEach((f, m) -> map.put(f, ReflectUtils.readValue(obj, m)));
        return map;
    }

    private <T> List<T> arrayToList(String property, Object array, Class<T> tClass) {
        Class<?> componentType = array.getClass().getComponentType();
        if (tClass.isAssignableFrom(componentType)) {
            return Stream.of((T[]) array).collect(Collectors.toList());
        } else {
            ArrayList<T> rs = new ArrayList<>();
            int length = Array.getLength(array);
            for (int i = 0; i < length; i++) {
                Object o = Array.get(array, i);
                rs.add(caseToObject(property, o, tClass));
            }
            return rs;
        }
    }

    private <T> T[] collectionToArray(String property, Collection<?> collection, Class<T> tClass) {
        Class<?> class0 = ifPrimitiveToWrapClass(tClass);
        Object array = Array.newInstance(class0, collection.size());
        int i = 0;
        for (Object o : collection) {
            T t = (T) caseToObject(property, o, class0);
            Array.set(array, i++, t);
        }
        return (T[]) array;
    }

    private Class<?> ifPrimitiveToWrapClass(Class<?> pc) {
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

    public static void main(String[] args) {
        EasyMap easyMap = new EasyMap();
        HashMap<String, Object> test = new HashMap<>();
        ArrayList<HashMap<String, Object>> test1 = new ArrayList<>();
        HashMap<String, Object> limit = new HashMap<>();
        limit.put("num", 0);
        limit.put("size", 10);
        test1.add(limit);
        test.put("test1", test1);
        easyMap.put("test", test);
        TypeReference<Collection<? super Limit>> typeReference = new TypeReference<Collection<? super Limit>>() {
        };
//        ArrayList<HashMap<String, Object>> list =
//                easyMap.getObject("test.test1", typeReference);
//        System.out.println(JSON.toJSONString(list));
        Type actualType = typeReference.getActualType();
        System.out.println(actualType);
        ParameterizedType parameterizedType = ((ParameterizedType) actualType);
        System.out.println(parameterizedType.getRawType());
        System.out.println(((WildcardType) parameterizedType.getActualTypeArguments()[0]).getLowerBounds()[0]);
//        System.out.println(((WildcardType)parameterizedType.getActualTypeArguments()[0]).getUpperBounds()[0]);

        int i = 2;
        test(i);
    }

    private static void test(Object c) {
        Integer[] a = new Integer[]{1, 2};
        List<Integer> list = Arrays.asList(a);
        EasyMap easyMap = new EasyMap();
        Object tests = easyMap.collectionToArray("test", list, int.class);
        System.out.println(tests.getClass());
    }


}
