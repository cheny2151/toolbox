package cn.cheny.toolbox.other.map;

import cn.cheny.toolbox.property.token.ParseTokenException;
import cn.cheny.toolbox.property.token.TokenParser;
import cn.cheny.toolbox.reflect.ReflectUtils;
import cn.cheny.toolbox.reflect.TypeReference;
import cn.cheny.toolbox.reflect.TypeUtils;

import java.lang.reflect.Array;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.*;
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
        return TypeUtils.caseToObject(this, tClass);
    }

    public <T> T toObject(TypeReference<T> typeReference) {
        Type actualType = typeReference.getActualType();
        return TypeUtils.caseToObject(this, actualType);
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
                    Array.set(array, i, TypeUtils.caseToObject(Array.get(val, i), tClass));
                }
                return (T[]) array;
            }
        } else if (val instanceof Collection) {
            Collection<?> collection = (Collection<?>) val;
            return TypeUtils.collectionToArray(collection, tClass);
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
                return (List<T>) TypeUtils.arrayToCollection(val, ArrayList.class, tClass);
            }
        } else if (val instanceof Collection) {
            Collection<?> collection = (Collection<?>) val;
            List<T> list = new ArrayList<>();
            for (Object o : collection) {
                list.add(TypeUtils.caseToObject(o, tClass));
            }
            return list;
        }
        throw new ParseTokenException("property '" + key + "' is " + val.getClass() + " ,expect collection or array");
    }


    public EasyMap getMap(String key) {
        Object val = getObject(key);
        return TypeUtils.caseToObject(val, EasyMap.class);
    }

    public <T> T getObject(String key, Class<T> tClass) {
        Object val = getObject(key);
        return TypeUtils.caseToObject(val, tClass);
    }

    public <T> T getObject(String key, TypeReference<T> typeReference) {
        Object object = getObject(key);
        Type actualType = typeReference.getActualType();
        return TypeUtils.caseToObject(object, actualType);
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
