package cn.cheny.toolbox.reflect.methodHolder;

import cn.cheny.toolbox.reflect.TypeUtils;
import cn.cheny.toolbox.reflect.methodHolder.exception.MethodHolderInvokeException;
import cn.cheny.toolbox.reflect.methodHolder.exception.NoSuchMethodException;
import cn.cheny.toolbox.reflect.methodHolder.model.MetaMethodCollect;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 基础MethodHolder实现类
 *
 * @author cheney
 * @date 2019-12-06
 */
@Slf4j
public abstract class BaseMethodHolder implements MethodHolder {

    // 持有方法所属类
    private final Class<?> holdClass;

    // 方法缓存Map
    protected ConcurrentHashMap<String, MetaMethodCollect> methodMap;

    public BaseMethodHolder(Class<?> clazz) {
        this.holdClass = clazz;
        this.methodMap = new ConcurrentHashMap<>();
    }

    @Override
    public Object invoke(String methodKey, Object obj, Object... args) {
        // args会包含调用的null,eg:如果调用此方法为invoke(a,b,null,null),则args为[null,null]
        return invoke(null, methodKey, obj, args);
    }

    @Override
    public Object invoke(Class<?> returnType, String methodKey, Object obj, Object... args) {
        Class<?>[] classes = args == null ? null : extractClass(args);
        Optional<Method> methodOpt = speculateMethod(methodKey, returnType, classes);
        Method method = methodOpt.orElseThrow(() -> new NoSuchMethodException(methodKey));
        return invoke(method, obj, args);
    }


    /**
     * 反射调用目标方法
     *
     * @param method 方法
     * @param obj    实例
     * @param args   参数
     * @return 返回值
     */
    public Object invoke(Method method, Object obj, Object... args) {
        try {
            int parameterCount = method.getParameterCount();
            Class<?> parameterType;
            if (args == null) {
                // 参数为null时填充null数组为入参
                return doInvoke(method, obj, createNullArray(parameterCount));
            } else if (parameterCount > 0
                    && (parameterType = method.getParameterTypes()[parameterCount - 1]).isArray()) {
                // 不定参数必在最后一位,对不定参数进行参数修复
                Class<?> componentType = parameterType.getComponentType();
                return doInvoke(method, obj, castToObjectArray(args, componentType, parameterCount));
            } else {
                return doInvoke(method, obj, args);
            }
        } catch (Exception e) {
            throw new MethodHolderInvokeException("执行方法:" + holdClass.getSimpleName() + "#" + method.getName() + "异常，" +
                    "方法入参:" + JSON.toJSONString(args), e);
        }
    }

    /**
     * 执行method
     *
     * @param method 方法
     * @param obj    对象
     * @param args   参数
     * @return 方法执行结果
     * @throws InvocationTargetException 异常
     * @throws IllegalAccessException    异常
     */
    protected Object doInvoke(Method method, Object obj, Object... args) throws InvocationTargetException, IllegalAccessException {
        return method.invoke(obj, args);
    }

    @Override
    public boolean hasMethod(String methodKey) {
        return methodMap.containsKey(methodKey);
    }

    @Override
    public Optional<Method> getMethod(Class<?> returnType, String methodKey, Class<?>... parameterTypes) {
        MetaMethodCollect metaMethodCollect = methodMap.get(methodKey);
        return metaMethodCollect == null ? Optional.empty() : Optional.ofNullable(metaMethodCollect.exactMethod(returnType, parameterTypes));
    }

    @Override
    public Optional<Method> getMethod(String methodKey, Class<?>... parameterTypes) {
        MetaMethodCollect metaMethodCollect = methodMap.get(methodKey);
        return metaMethodCollect == null ? Optional.empty() : Optional.ofNullable(metaMethodCollect.exactMethodByArgs(parameterTypes));
    }

    @Override
    public Optional<Method> getMethod(String name) {
        MetaMethodCollect metaMethodCollect = methodMap.get(name);
        return metaMethodCollect == null ? Optional.empty() : Optional.ofNullable(metaMethodCollect.exactMethodByKey());
    }

    @Override
    public Optional<Method> speculateMethod(String methodKey, Class<?> returnType, Class<?>... args) {
        MetaMethodCollect metaMethodCollect = methodMap.get(methodKey);
        return metaMethodCollect == null ? Optional.empty() : Optional.ofNullable(metaMethodCollect.speculateMethod(returnType, args));
    }

    /**
     * 获取持有方法的类
     *
     * @return 缓存方法对应的类
     */
    public Class<?> getHoldClass() {
        return holdClass;
    }

    /**
     * 获取方法签名
     *
     * @param method         方法
     * @param methodKey      方法key
     * @param withReturnType 是否包含返回类型
     * @return 方法签名
     */
    public static String getSignature(Method method, String methodKey, boolean withReturnType) {
        StringBuilder builder = new StringBuilder();
        if (withReturnType) {
            Class<?> returnType = method.getReturnType();
            builder.append(returnType.getName()).append("#");
        }
        builder.append(methodKey);
        if (method.getParameterCount() > 0) {
            builder.append(":");
            String args = Stream.of(method.getParameterTypes()).map(Class::getName).collect(Collectors.joining(","));
            builder.append(args);
        }
        return builder.toString();
    }

    /**
     * 缓存方法
     *
     * @param method 方法
     */
    protected void cacheMethod(Method method) {
        if (method == null)
            return;
        cacheMethod(method.getName(), method);
    }

    /**
     * 指定key缓存方法
     *
     * @param methodKey 缓存key
     * @param method    方法
     */
    protected void cacheMethod(String methodKey, Method method) {
        if (method == null)
            return;
        methodMap.computeIfAbsent(methodKey, key -> new MetaMethodCollect(holdClass, key)).add(methodKey, method);
    }

    /**
     * 修复不定参数,将最后一项参数(不定参数)包装为array，其他参数不变copy出新的Object[]
     *
     * @param args           参数
     * @param arrayType      不定参数数组类型
     * @param parameterCount 方法参数个数
     * @return 修复后的数据
     */
    private Object[] castToObjectArray(Object[] args, Class<?> arrayType, int parameterCount) {
        // 如果入参与方法参数长度一致，并且最后一个入参即为array类型/Collection类型，则直接返回原入参
        int lastIndex = parameterCount - 1;
        if (args.length == parameterCount) {
            Object lastArg = args[lastIndex];
            Class<?> lastArgsClass = lastArg.getClass();
            if (lastArgsClass.isArray()) {
                return args;
            } else if (Collection.class.isAssignableFrom(lastArgsClass)) {
                Object[] fixArgs = TypeUtils.collectionToArray((Collection<?>) lastArg, arrayType);
                args[lastIndex] = fixArgs;
                return args;
            }
        }
        Object[] fixArgs = new Object[parameterCount];
        if (lastIndex != 0) {
            // 非不定参数不变，copy
            System.arraycopy(args, 0, fixArgs, 0, lastIndex);
        }
        // 通过反射将不定参数包装到array中，存到fixArgs最后一位
        Object array = Array.newInstance(arrayType, args.length - parameterCount + 1);
        int index = 0;
        for (int i = lastIndex; i < args.length; i++) {
            Array.set(array, index++, args[i]);
        }
        fixArgs[lastIndex] = array;
        return fixArgs;
    }

    /**
     * 提取出对象数组的类型数组
     * args包含的null视为Object.class
     *
     * @param args 参数集合
     * @return 参数类型数组
     */
    private Class<?>[] extractClass(Object[] args) {
        return Stream.of(args)
                // 参数为null时，无法辨别类型，用Object代替
                .map(arg -> arg == null ? Object.class : arg.getClass())
                .toArray(Class[]::new);
    }

    /**
     * 生成空的Object array
     *
     * @param count 个数
     * @return 空array
     */
    private Object[] createNullArray(int count) {
        Object[] nullArray = new Object[count];
        for (int i = 0; i < count; i++) {
            nullArray[i] = null;
        }
        return nullArray;
    }

}
