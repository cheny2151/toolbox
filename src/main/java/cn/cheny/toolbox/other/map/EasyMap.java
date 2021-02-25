package cn.cheny.toolbox.other.map;

import cn.cheny.toolbox.other.DateUtils;
import cn.cheny.toolbox.property.token.ParseTokenException;
import cn.cheny.toolbox.property.token.TokenParser;
import cn.cheny.toolbox.reflect.ReflectUtils;
import cn.cheny.toolbox.reflect.TypeReference;
import com.alibaba.fastjson.JSON;

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
        if (val instanceof Integer || val == null) {
            return (Integer) val;
        }
        return Integer.valueOf(val.toString());
    }

    public Long getLong(String key) {
        Object val = getObject(key);
        if (val instanceof Long || val == null) {
            return (Long) val;
        }
        return Long.valueOf(val.toString());
    }

    public Boolean getBoolean(String key) {
        Object val = getObject(key);
        if (val instanceof Boolean || val == null) {
            return (Boolean) val;
        }
        return Boolean.valueOf(val.toString());
    }

    public Short getShort(String key) {
        Object val = getObject(key);
        if (val instanceof Short || val == null) {
            return (Short) val;
        }
        return Short.valueOf(val.toString());
    }

    public Character getCharacter(String key) {
        Object val = getObject(key);
        if (val instanceof Character || val == null) {
            return (Character) val;
        }
        throw new ParseTokenException("property '" + key + "' is " + val.getClass() + " ,expect char");
    }

    public BigDecimal getBigDecimal(String key) {
        Object val = getObject(key);
        if (val instanceof BigDecimal || val == null) {
            return (BigDecimal) val;
        }
        return new BigDecimal(val.toString());
    }

    public Date getDate(String key) {
        Object val = getObject(key);
        if (val instanceof Date || val == null) {
            return (Date) val;
        }
        return DateUtils.parseDate(val.toString());
    }

    public <T> T[] getArray(String key, Class<T> tClass) {
        Object val = getObject(key);
        if (val == null) {
            return null;
        } else if (val.getClass().isArray()) {
            Object[] array = (Object[]) val;
            Class<?> componentType = array.getClass().getComponentType();
            if (tClass.isAssignableFrom(componentType)) {
                return (T[]) val;
            } else if (Map.class.isAssignableFrom(componentType)) {
                return Stream.of((Map<String, Object>[]) val)
                        .map(m -> mapToObject(m, tClass))
                        .toArray(len -> (T[]) Array.newInstance(tClass, len));
            } else {
                throw new ParseTokenException("property '" + key + "' array is " + componentType + " ,expect " + tClass);
            }
        } else if (val instanceof Collection) {
            Collection<?> collection = (Collection<?>) val;
            int size = collection.size();
            T[] array = (T[]) Array.newInstance(tClass, size);
            int i = 0;
            for (Object o : collection) {
                array[i++] = caseToObject(key, o, tClass);
            }
            return array;
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
        if (object instanceof Map) {

        } else if (object instanceof Collection) {

        } else if (object.getClass().isArray()) {

        }
        if (actualType instanceof ParameterizedType) {

        } else if (actualType instanceof TypeVariable) {

        }
        return null;
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
        } else {
            throw new ParseTokenException("property '" + property + "' is " + obj.getClass() + " ,expect " + objType);
        }
    }

    private <T> T caseToObject(String property, Object obj, Class<T> objType) {
        if (obj == null) {
            return null;
        } else if (objType.isAssignableFrom(obj.getClass())) {
            return (T) obj;
        } else if ((obj instanceof Map) && !(Map.class.isAssignableFrom(objType))) {
            return mapToObject((Map<String, Object>) obj, objType);
        } else if (!(obj instanceof Map) && (Map.class.isAssignableFrom(objType))) {
            return (T) objectToMap(obj);
        } else if (obj instanceof Collection && objType.isArray()) {

            return null;
        } else if (isBaseClass(obj.getClass()) && isBaseClass(objType)) {
            return tryCoverBase(property, obj, objType);
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

    private <T> T caseToObject(String property, Object obj, ParameterizedType objType) {
        Type rawType = objType.getRawType();
        Object obj0 = caseToObject(property, obj, rawType);
        if (obj0 == null) {
            return null;
        } else if (obj0 instanceof Map) {
            Type[] argTypes = objType.getActualTypeArguments();
            return null;
        } else {
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
        Map<String, Object> map = new LinkedHashMap<>();
        writerMethod.forEach((f, m) -> {
            map.put(f, ReflectUtils.readValue(obj, m));
        });
        return map;
    }

    private <T> List<T> arrayToList(String property, Object array, Class<T> tClass) {
        Class<?> componentType = array.getClass().getComponentType();
        if (tClass.isAssignableFrom(componentType)) {
            return Stream.of((T[]) array).collect(Collectors.toList());
        } else if (array instanceof int[]) {
            ArrayList<T> rs = new ArrayList<>();
            for (int i : ((int[]) array)) {
                rs.add(tryCoverBase(property, i, tClass));
            }
            return rs;
            // todo
        } else {
            return Stream.of(array).map(e -> caseToObject(property, e, tClass)).collect(Collectors.toList());
        }
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
        TypeReference<ArrayList<String>> typeReference = new TypeReference<ArrayList<String>>() {
        };
//        ArrayList<HashMap<String, Object>> list =
//                easyMap.getObject("test.test1", typeReference);
//        System.out.println(JSON.toJSONString(list));
        Type actualType = typeReference.getActualType();
        System.out.println(actualType.getClass());

        int i = 2;
        test(i);
    }

    private static void test(Object c) {

    }


}
