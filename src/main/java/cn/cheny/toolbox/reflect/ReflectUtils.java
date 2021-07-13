package cn.cheny.toolbox.reflect;

import cn.cheny.toolbox.other.fun.FilterFunction;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 反射工具类
 */
@Slf4j
public class ReflectUtils {

    private final static String GET_PRE = "get";

    private final static String SET_PRE = "set";

    private final static String IS_PRE = "is";

    private final static Method LOOKUP_DEFINE_CLASS_METHOD;

    static {
        Method lookupDefineClass0 = null;
        try {
            lookupDefineClass0 = (Method) AccessController.doPrivileged(new PrivilegedExceptionAction() {
                public Object run() throws Exception {
                    try {
                        return MethodHandles.Lookup.class.getMethod("defineClass", byte[].class);
                    } catch (NoSuchMethodException var2) {
                        return null;
                    }
                }
            });
        } catch (PrivilegedActionException e) {
            e.printStackTrace();
        }
        LOOKUP_DEFINE_CLASS_METHOD = lookupDefineClass0;
    }

    private ReflectUtils() {
    }

    /**
     * 反射获取字段值
     */
    public static Object readValue(Object bean, String property) {
        try {
            return getReadMethod(bean.getClass(), property).invoke(bean);
        } catch (Exception e) {
            throw new ReflectException("反射获取字段值错误", e);
        }
    }

    /**
     * 反射获取字段值
     * 转换为运行时异常
     */
    public static Object readValue(Object bean, Method readMethod) {
        try {
            return readMethod.invoke(bean);
        } catch (Exception e) {
            throw new ReflectException("反射获取字段值错误", e);
        }
    }

    /**
     * 获取read方法
     * 找不到对应方法不抛出异常，返回null
     */
    public static Method getReadMethod(Class<?> clazz, String property) {
        if (StringUtils.isEmpty(property)) {
            throw new IllegalArgumentException("property must not empty");
        }
        String[] withToTry = {property, null};
        if (!property.startsWith("get") && !property.startsWith("is")) {
            buildGetMethodName(withToTry, property);
        }
        Class<?> currentClass = clazz;
        while (!Object.class.equals(currentClass)) {
            for (String toTry : withToTry) {
                try {
                    if (toTry == null)
                        continue;
                    Method method = currentClass.getDeclaredMethod(toTry);
                    method.setAccessible(true);
                    return method;
                } catch (NoSuchMethodException e) {
                    // try next
                }
            }
            currentClass = currentClass.getSuperclass();
        }
        throw new ReflectException("can not find read method with '" + property + "' in " + clazz.getName());
    }

    /**
     * 获取待尝试的read方法名
     *
     * @param withToTry 待尝试方法名
     * @param name      属性名
     */
    private static void buildGetMethodName(String[] withToTry, String name) {
        name = toUpperFirstLetter(name);
        withToTry[0] = GET_PRE + name;
        withToTry[1] = IS_PRE + name;
    }

    /**
     * 获取所有read方法
     *
     * @return k:属性名,v:readMethod
     */
    public static Map<String, Method> getAllReadMethod(Class<?> targetClass, Class<?> stopClass) {
        Class<?> currentClass = targetClass;
        Map<String, Method> methods = new HashMap<>();
        while (!currentClass.equals(stopClass)) {
            Method[] declaredMethods = currentClass.getDeclaredMethods();
            for (Method method : declaredMethods) {
                if (isReadMethod(method)) {
                    method.setAccessible(true);
                    methods.put(extractPropertyName(method), method);
                }
            }
            currentClass = currentClass.getSuperclass();
        }
        return methods;
    }


    /**
     * 反射写入字段值
     */
    public static void writeValue(Object bean, String property, Object value) {
        try {
            getWriterMethod(bean.getClass(), property, value.getClass()).invoke(bean, value);
        } catch (Exception e) {
            throw new ReflectException("反射写入字段值错误", e);
        }
    }

    /**
     * 反射写入字段值
     * 转换为运行时异常
     */
    public static void writeValue(Object bean, Method method, Object value) {
        try {
            method.invoke(bean, value);
        } catch (Exception e) {
            throw new ReflectException("反射写入字段值错误", e);
        }
    }

    /**
     * 获取writer方法
     */
    public static Method getWriterMethod(Class<?> clazz, String property, Class<?> propertyType) {
        if (StringUtils.isEmpty(property)) {
            throw new IllegalArgumentException("property must not empty");
        }
        String methodName = SET_PRE + toUpperFirstLetter(property);
        Method method = getMethod(clazz, methodName, propertyType);
        if (method == null) {
            throw new ReflectException("can not find write method '" + methodName + "' in " + clazz.getName());
        }
        return method;
    }

    /**
     * 获取所有write方法
     *
     * @return k:属性名,v:writeMethod
     */
    public static Map<String, Method> getAllWriterMethod(Class<?> targetClass, Class<?> stopClass) {
        Class<?> currentClass = targetClass;
        Map<String, Method> methods = new HashMap<>();
        while (!currentClass.equals(stopClass)) {
            Method[] declaredMethods = currentClass.getDeclaredMethods();
            for (Method method : declaredMethods) {
                if (isWriteMethod(method)) {
                    method.setAccessible(true);
                    methods.put(extractPropertyName(method), method);
                }
            }
            currentClass = currentClass.getSuperclass();
        }
        return methods;
    }


    /**
     * 获取方法，找不到对应方法时返回null
     *
     * @param clazz         方法所在类
     * @param methodName    方法名
     * @param propertyTypes 参数类型
     * @return 方法实例
     */
    public static Method getMethod(Class<?> clazz, String methodName, Class<?>... propertyTypes) {
        Class<?> currentClass = clazz;
        while (!Object.class.equals(currentClass)) {
            try {
                Method method = currentClass.getDeclaredMethod(methodName, propertyTypes);
                method.setAccessible(true);
                return method;
            } catch (NoSuchMethodException e) {
                // try next superClass
            }
            currentClass = currentClass.getSuperclass();
        }
        return null;
    }

    /**
     * 获取含有注解的方法
     * 注意返回Map的key为方法名
     *
     * @param clazz           实体类
     * @param annotationClass 注解类
     * @return k:方法名,v:方法
     */
    public static Map<String, Method> getAllMethodHasAnnotation(Class<?> clazz, Class<? extends Annotation> annotationClass) {
        HashMap<String, Method> result = new HashMap<>();
        Class<?> currentClass = clazz;
        while (!Object.class.equals(currentClass)) {
            Method[] declaredMethods = currentClass.getDeclaredMethods();
            for (Method method : declaredMethods) {
                Annotation declaredAnnotation = method.getDeclaredAnnotation(annotationClass);
                if (declaredAnnotation != null) {
                    method.setAccessible(true);
                    result.put(method.getName(), method);
                }
            }
            currentClass = currentClass.getSuperclass();
        }

        return result;
    }

    /**
     * 获取含有注解的write方法
     *
     * @param clazz           类
     * @param annotationClass 注解类
     * @return
     */
    public static Map<String, Method> getAllWriteMethodHasAnnotation(Class<?> clazz, Class<? extends Annotation> annotationClass) {
        Map<String, Method> methodMap = getAllMethodHasAnnotation(clazz, annotationClass);
        HashMap<String, Method> result = new HashMap<>();
        for (Map.Entry<String, Method> methodEntry : methodMap.entrySet()) {
            Method method = methodEntry.getValue();
            if (isWriteMethod(method)) {
                result.put(extractPropertyName(method), method);
            }
        }
        methodMap.clear();
        return result;
    }

    /**
     * 获取含有注解的read方法
     *
     * @param clazz           类
     * @param annotationClass 注解类
     * @return
     */
    public static Map<String, Method> getAllReadMethodHasAnnotation(Class<?> clazz, Class<? extends Annotation> annotationClass) {
        Map<String, Method> methodMap = getAllMethodHasAnnotation(clazz, annotationClass);
        HashMap<String, Method> result = new HashMap<>();
        for (Map.Entry<String, Method> methodEntry : methodMap.entrySet()) {
            Method method = methodEntry.getValue();
            if (isReadMethod(method)) {
                result.put(extractPropertyName(method), method);
            }
        }
        methodMap.clear();
        return result;
    }

    /**
     * 获取所有字段
     *
     * @param clazz 所属对象class
     * @param stop  终止递归父类
     * @return
     */
    public static List<Field> getAllFields(Class<?> clazz, Class<?> stop) {
        return getFields(clazz, stop, null);
    }

    /**
     * 获取对象属性字段(除find & status)
     *
     * @param clazz 所属对象class
     * @param stop  终止递归父类
     * @return 字段
     */
    public static List<Field> getPropertyFields(Class<?> clazz, Class<?> stop) {
        return getFields(clazz, stop, field -> (field.getModifiers() & Modifier.FINAL) == 0 ||
                (field.getModifiers() & Modifier.STATIC) == 0);
    }

    /**
     * 获取所有字段名
     *
     * @param clazz 所属对象class
     * @return
     */
    public static Set<String> getAllFieldNames(Class<?> clazz) {
        List<Field> fields = getAllFields(clazz, Object.class);
        TreeSet<String> names = new TreeSet<>();
        fields.forEach(field ->
                names.add(field.getName())
        );
        return names;
    }

    /**
     * 通过反射字段获取字段值
     *
     * @param object 实例对象
     * @param name   字段名
     * @param <T>    返回类型
     * @return
     */
    @SuppressWarnings("unchecked")
    public static <T> T reflectValueByField(Object object, String name) {
        Field field = field(object.getClass(), name);
        try {
            return (T) field.get(object);
        } catch (Exception e) {
            //change to RuntimeException
            throw new RuntimeException("reflect value error", e);
        }
    }

    /**
     * 获取字段
     *
     * @param clazz 所属class
     * @param name  字段名
     * @return
     */
    public static Field field(Class<?> clazz, String name) {
        if (name == null || "".equals(name)) {
            throw new IllegalArgumentException();
        }
        Class<?> currentClass = clazz;
        // 尝试查找Field直到Object类
        while (!Object.class.equals(currentClass)) {
            try {
                Field field = currentClass.getDeclaredField(name);
                field.setAccessible(true);
                return field;
            } catch (NoSuchFieldException e) {
                // try to find field on superClass
            }
            currentClass = currentClass.getSuperclass();
        }
        throw new ReflectException("can not find field '" + name + "' in " + clazz.getName());
    }

    /**
     * 获取所有包含注解的字段
     *
     * @param clazz           类
     * @param annotationClass 注解类
     * @return K:属性名,v:字段
     */
    public static Map<String, Field> getAllFieldHasAnnotation(Class<?> clazz, Class<? extends Annotation> annotationClass) {
        return getFields(clazz, Object.class, field -> field.getDeclaredAnnotation(annotationClass) != null)
                .stream().collect(Collectors.toMap(Field::getName, field -> field));
    }

    /**
     * 获取并过滤字段
     *
     * @param clazz               所属对象class
     * @param stop                终止递归父类
     * @param fieldFilterFunction 过滤函数,返回true则添加到结果
     * @return 字段集合
     */
    public static List<Field> getFields(Class<?> clazz, Class<?> stop, FilterFunction<Field> fieldFilterFunction) {
        List<Field> fields = new ArrayList<>();
        for (; clazz != stop; clazz = clazz.getSuperclass()) {
            for (Field field : clazz.getDeclaredFields()) {
                if (field.getName().equalsIgnoreCase("serialVersionUID")
                        || (fieldFilterFunction != null
                        && !fieldFilterFunction.filter(field))) continue;
                field.setAccessible(true);
                fields.add(field);
            }
        }
        return fields;
    }

    /**
     * 获取object对应field的值
     *
     * @param field 字段
     * @param obj   对象
     * @return 字段值
     */
    public static Object getFieldVal(Field field, Object obj) {
        try {
            return field.get(obj);
        } catch (IllegalAccessException e) {
            throw new ReflectException("can not get field '" + field.getName() + "' in " + obj.getClass());
        }
    }

    /**
     * 设置object对应field的值
     *
     * @param field 字段
     * @param obj   对象
     */
    public static void setFieldVal(Field field, Object obj, Object val) {
        try {
            field.set(obj, val);
        } catch (IllegalAccessException e) {
            throw new ReflectException("can not get field '" + field.getName() + "' in " + obj.getClass());
        }
    }

    /**
     * 反射构造对象
     *
     * @param clazz          class
     * @param parameterClass 构造函数参数class
     * @param args           构造函数参数
     * @param <T>            返回类型
     * @return
     */
    public static <T> T newObject(Class<T> clazz, Class<?>[] parameterClass, Object[] args) {
        try {
            return parameterClass == null || parameterClass.length == 0 ?
                    clazz.getConstructor().newInstance() :
                    clazz.getConstructor(parameterClass).newInstance(args);
        } catch (Exception e) {
            throw new ReflectException("reflect:can no new object");
        }
    }

    /**
     * 判断是否是read方法
     *
     * @param method 方法
     * @return boolean
     */
    public static boolean isReadMethod(Method method) {
        String methodName = method.getName();
        if (methodName.startsWith(GET_PRE) || methodName.startsWith(IS_PRE)) {
            return method.getParameterCount() == 0 && !void.class.equals(method.getReturnType());
        }
        return false;
    }

    /**
     * 提取方法对应的属性名
     * 不为write/read方法则返回原方法名
     *
     * @param method 方法
     * @return 属性名
     */
    public static String extractPropertyName(Method method) {
        String methodName = method.getName();
        String pre = "";
        if (methodName.startsWith(IS_PRE)) {
            pre = IS_PRE;
        } else if (methodName.startsWith(GET_PRE)) {
            pre = GET_PRE;
        } else if (methodName.startsWith(SET_PRE)) {
            pre = SET_PRE;
        }

        return toLowerFirstLetter(methodName.substring(pre.length()));
    }

    /**
     * 判断是否是write方法
     *
     * @param method 方法
     * @return boolean
     */
    public static boolean isWriteMethod(Method method) {
        String methodName = method.getName();
        if (methodName.startsWith(SET_PRE)) {
            return method.getParameterCount() == 1 && void.class.equals(method.getReturnType());
        }
        return false;
    }

    public static boolean isWrapForm(Class<?> warp, Class<?> base) {
        try {
            Object type = warp.getField("TYPE").get(null);
            return base.equals(type);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            return false;
        }
    }

    /**
     * 判断类是否存在
     *
     * @param className   类名
     * @param classLoader 类加载器
     * @return 是否存在
     */
    public static boolean isPresent(String className, ClassLoader classLoader) {
        if (classLoader == null) {
            classLoader = Thread.currentThread().getContextClassLoader();
        }
        try {
            Class.forName(className, false, classLoader);
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    public static Class<?> defineClass(byte[] c) throws InvocationTargetException, IllegalAccessException {
        return (Class<?>) LOOKUP_DEFINE_CLASS_METHOD.invoke(MethodHandles.lookup(), c);
    }

    private static String toUpperFirstLetter(String fieldName) {
        return fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
    }

    private static String toLowerFirstLetter(String fieldName) {
        return fieldName.substring(0, 1).toLowerCase() + fieldName.substring(1);
    }

}
