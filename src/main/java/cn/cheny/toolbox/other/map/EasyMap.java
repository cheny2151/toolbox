package cn.cheny.toolbox.other.map;

import cn.cheny.toolbox.property.token.ParseTokenException;
import cn.cheny.toolbox.property.token.TokenParser;
import cn.cheny.toolbox.reflect.ReflectUtils;
import com.alibaba.fastjson.JSON;
import org.apache.commons.lang3.time.DateUtils;

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.text.ParseException;
import java.util.*;
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

    @SuppressWarnings("SuspiciousToArrayCall")
    public <T> T[] getArray(String key, Class<T> tClass) {
        Object val = getObject(key);
        if (val == null) {
            return null;
        } else if (val.getClass().isArray()) {
            return (T[]) val;
        } else if (val instanceof Collection) {
            Collection<?> collection = (Collection<?>) val;
            int size = collection.size();
            T[] array = (T[]) Array.newInstance(tClass, size);
            if (size == 0) {
                return array;
            }
            try {
                return collection.toArray(array);
            } catch (ArrayStoreException e) {
                throw new ParseTokenException("property '" + key + "' array is " + collection.iterator().next().getClass() + " ,expect " + tClass);
            }
        }
        throw new ParseTokenException("property '" + key + "' is " + val.getClass() + " ,expect collection or array");
    }

    public <T> ArrayList<T> getArrayList(String key) {
        Object val = getObject(key);
        if (val == null) {
            return null;
        } else if (val.getClass().isArray()) {
            return (ArrayList<T>) Stream.of((T[]) val).collect(Collectors.toList());
        } else if (val instanceof Collection) {
            return new ArrayList<>((Collection<? extends T>) val);
        }
        throw new ParseTokenException("property '" + key + "' is " + val.getClass() + " ,expect collection or array");
    }

    public Map<String, Object> getMap(String key) {
        Object val = getObject(key);
        if (val == null) {
            return null;
        } else if (val instanceof Map) {
            Map<String, Object> map = (Map<String, Object>) val;
            if (map instanceof EasyMap) {
                return map;
            }
            return new EasyMap(map);
        }
        throw new ParseTokenException("property '" + key + "' is " + val.getClass() + " ,expect map");
    }

    public <T> T getObject(String key, Class<T> tClass) {
        Object val = getObject(key);
        if (val == null) {
            return null;
        } else if (tClass.isAssignableFrom(val.getClass())) {
            return (T) val;
        } else if (val instanceof Map) {
            Map<String, Object> map = (Map<String, Object>) val;
            Map<String, Method> writerMethod = ReflectUtils.getAllWriterMethod(tClass, Object.class);
            T t = ReflectUtils.newObject(tClass, null, null);
            writerMethod.forEach((f, m) -> {
                Object fieldVal = map.get(f);
                if (fieldVal != null) {
                    ReflectUtils.writeValue(t, m, fieldVal);
                }
            });
            return t;
        }
        throw new ParseTokenException("property '" + key + "' is " + val.getClass() + " ,expect map");
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

    public static void main(String[] args) {
        EasyMap easyMap = new EasyMap();
        HashMap<String, Object> test = new HashMap<>();
        ArrayList<String> test1 = new ArrayList<>();
        test1.add("test0");
        test1.add("test1");
        test1.add("test2");
        test1.add("test3");
        test1.add("test4");
        test1.add("test5");
        String[] test2 = test1.toArray(new String[test.size()]);
        test.put("test1", test2);
        easyMap.put("test", test);
        Integer[] array = easyMap.getArray("test.test1", Integer.class);
        System.out.println(JSON.toJSONString(array));
    }

}
