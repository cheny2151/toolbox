package cn.cheny.toolbox.other.map;

import cn.cheny.toolbox.other.page.Limit;
import cn.cheny.toolbox.property.token.ParseTokenException;
import cn.cheny.toolbox.property.token.TokenParser;
import cn.cheny.toolbox.reflect.ReflectUtils;
import cn.cheny.toolbox.reflect.TypeReference;
import com.alibaba.fastjson.JSON;
import org.apache.commons.lang3.time.DateUtils;

import java.lang.reflect.*;
import java.math.BigDecimal;
import java.text.ParseException;
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
        try {
            return DateUtils.parseDate(val.toString(), "yyyy-MM-dd", "yyyy-MM-dd HH:mm:ss");
        } catch (ParseException e) {
            throw new IllegalArgumentException("不支持的日期格式:" + key);
        }
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
            } else if (Map.class.isAssignableFrom(componentType)) {
                return Stream.of((Map<String, Object>[]) val)
                        .map(m -> mapToObject(m, tClass))
                        .collect(Collectors.toList());
            } else {
                throw new ParseTokenException("property '" + key + "' array is " + componentType + " ,expect " + tClass);
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
        } else if (obj instanceof Map) {
            return mapToObject((Map<String, Object>) obj, objType);
        } else {
            throw new ParseTokenException("property '" + property + "' is " + obj.getClass() + " ,expect " + objType);
        }
    }

    private <T> T caseToObject(String property, Object obj, ParameterizedType objType) {
        Type rawType = objType.getRawType();
        if (obj == null) {
            return null;
        } else if (false) {
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
    }

}
