package cn.cheny.toolbox.reflect;

import java.util.Map;

/**
 * 类型工具类
 *
 * @author cheney
 * @date 2019-12-11
 */
public class TypeUtils {

    public static <T> T castToBasicClass(Object obj, Class<T> type) {
        if (obj == null) {
            return null;
        } else if (type == null) {
            throw new IllegalArgumentException("clazz is null");
        } else if (type == obj.getClass()) {
            return (T) obj;
        } else if (obj instanceof Map) {
            if (type == Map.class) {
                return (T) obj;
            } else {
                Map map = (Map) obj;
            }
        } else {
        }
        return null;
    }

}
