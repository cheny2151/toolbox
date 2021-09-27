package cn.cheny.toolbox.reflect;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.HashMap;
import java.util.Map;

/**
 * 用于提取泛型与其擦除后的实际类型的映射关系
 *
 * @author by chenyi
 * @date 2021/3/8
 */
public class TypeVariableParser {

    /**
     * 解析泛型与其擦除后实际类型的映射
     *
     * @param classToParse class
     * @return 泛型，实际类型映射关系
     */
    public static Map<TypeVariable<?>, Type> parse(Class<?> classToParse) {
        Map<TypeVariable<?>, Type> typeMap = new HashMap<>();
        parse(classToParse, typeMap);
        return typeMap;
    }

    private static void parse(Class<?> classToParse, Map<TypeVariable<?>, Type> typeMap) {
        Type genericSuperclass = classToParse.getGenericSuperclass();
        if (genericSuperclass instanceof ParameterizedType) {
            parse((ParameterizedType) genericSuperclass, typeMap);
        }
        Type[] genericInterfaces = classToParse.getGenericInterfaces();
        for (Type genericInterface : genericInterfaces) {
            if (genericInterface instanceof ParameterizedType) {
                parse((ParameterizedType) genericInterface, typeMap);
            }
        }
    }

    private static void parse(ParameterizedType parameterizedType, Map<TypeVariable<?>, Type> typeMap) {
        TypeUtils.extractParameterizedType(parameterizedType, typeMap);
    }

}
