package cn.cheny.toolbox.reflect;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * 用于提取泛型与其擦除后的实际类型的映射关系
 *
 * @date 2021/3/8
 * @author by chenyi
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
        Class<?> rawClass = (Class<?>) parameterizedType.getRawType();
        TypeVariable<? extends Class<?>>[] typeParameters = rawClass.getTypeParameters();
        Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
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
        parse(rawClass, typeMap);
    }

}
