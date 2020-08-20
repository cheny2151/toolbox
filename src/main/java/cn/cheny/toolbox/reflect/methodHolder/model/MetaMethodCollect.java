package cn.cheny.toolbox.reflect.methodHolder.model;

import cn.cheny.toolbox.reflect.ReflectUtils;
import cn.cheny.toolbox.reflect.methodHolder.exception.FindNotUniqueMethodException;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 以方法名为分组的方法元数据
 *
 * @author cheney
 * @date 2020-04-01
 */
public class MetaMethodCollect {

    /**
     * 方法key(注意不一定是方法名)
     */
    private String methodKey;

    /**
     * 方法所属类(注意不一定是DeclaringClass)
     */
    private Class<?> owner;

    /**
     * 此方法名下的所有方法
     */
    private Set<MetaMethod> metaMethods;

    public MetaMethodCollect(Class<?> owner, String methodKey) {
        this.owner = owner;
        this.methodKey = methodKey;
        this.metaMethods = new HashSet<>();
    }

    /**
     * 添加方法，方法key为方法名
     *
     * @param method 方法
     * @return 结果
     */
    public boolean add(Method method) {
        return add(method.getName(), method);
    }

    /**
     * 添加方法
     * 相同的方法签名会通过继承关系，方法所属类等选中最优方法
     *
     * @param method 方法
     * @return 添加结果
     * @throws IllegalArgumentException 添加方法，出现不合法的相同签名的方法时抛出异常
     */
    public boolean add(String methodKey, Method method) {
        if (equalMethodKey(methodKey) && isOwnerMethod(method)) {
            MetaMethod newMethod = new MetaMethod(method, methodKey);
            Class<?> returnType = newMethod.getReturnType();
            Class<?> declaringClass = method.getDeclaringClass();
            boolean add = true;
            for (Iterator<MetaMethod> iterator = metaMethods.iterator(); iterator.hasNext(); ) {
                MetaMethod next = iterator.next();
                if (next.getSignature().equals(newMethod.getSignature())) {
                    // 签名相同(方法名，参数类型相同)
                    Class<?> nextReturnType = next.getReturnType();
                    if (nextReturnType.equals(returnType)) {
                        // 返回类型相同,证明存在方法重写，优先取方法所属类是子类重写的方法
                        Class<?> nextDeclaringClass = next.getMethod().getDeclaringClass();
                        if (nextDeclaringClass.isAssignableFrom(declaringClass)) {
                            iterator.remove();
                        } else if (declaringClass.isAssignableFrom(nextDeclaringClass)) {
                            add = false;
                        } else {
                            throw new IllegalArgumentException("Can not add method.Because the method signature and return type is the same");
                        }
                    } else if (nextReturnType.isAssignableFrom(returnType)) {
                        // 返回类型不同并且存在重写方法并修改返回类型为子类，取子类重写的方法
                        iterator.remove();
                    } else if (returnType.isAssignableFrom(nextReturnType)) {
                        add = false;
                    } else {
                        // 一个类中，排除继承方法重写后，不应该出现签名相同，但是返回类型不同的方法
                        throw new IllegalArgumentException("Can not add method.Because the method signature is the same,but the return type has no parent-child relationship");
                    }
                    break;
                }
            }
            return add && metaMethods.add(newMethod);
        }
        return false;
    }

    /**
     * 方式是否为owner或子类的方法
     *
     * @param method 方法
     * @return 是否为owner的方法
     */
    private boolean isOwnerMethod(Method method) {
        Class<?> declaringClass = method.getDeclaringClass();
        return declaringClass.equals(owner) || declaringClass.isAssignableFrom(owner);
    }

    /**
     * 方法key与入参是否相同
     *
     * @param methodKey 方法key
     * @return 是否相同
     */
    public boolean equalMethodKey(String methodKey) {
        return this.methodKey.equals(methodKey);
    }

    /**
     * 通过方法名确切获取方法，获取到多个方法时抛异常
     *
     * @return 方法
     * @throws FindNotUniqueMethodException 获取到多个方法时抛异常
     */
    public Method exactMethodByKey() {
        int size = metaMethods.size();
        if (size > 1) {
            throw new FindNotUniqueMethodException("method key '" + methodKey + "' has not unique method in class :" + this.owner.getName());
        }
        return metaMethods.stream().findFirst().map(MetaMethod::getMethod).orElse(null);
    }

    /**
     * 通过方法名和返回类型，确切获取方法
     *
     * @param returnType 返回类型
     * @return 方法
     * @throws FindNotUniqueMethodException 获取到多个方法时抛异常
     */
    public Method exactMethodByReturn(Class<?> returnType) {
        List<MetaMethod> findResult = metaMethods.stream()
                .filter(metaMethod -> returnType.equals(metaMethod.getReturnType()))
                .collect(Collectors.toList());
        if (findResult.size() > 1) {
            throw new FindNotUniqueMethodException("method key '" + methodKey +
                    "' and return type '" + returnType.getSimpleName() +
                    "' has not unique method in class :" + this.owner.getName());
        }
        return findResult.size() == 0 ? null : findResult.get(0).getMethod();
    }

    /**
     * 通过方法名和参数个数确切获取方法，获取到多个方法时抛异常
     *
     * @param argsNum 参数个数
     * @return 方法
     * @throws FindNotUniqueMethodException 获取到多个方法时抛异常
     */
    public Method exactMethodByArgsNum(int argsNum) {
        List<MetaMethod> findResult = metaMethods.stream()
                .filter(metaMethod -> metaMethod.getArgsNum() == argsNum)
                .collect(Collectors.toList());
        if (findResult.size() > 1) {
            throw new FindNotUniqueMethodException("method key '" + methodKey +
                    "' and args number = " + argsNum +
                    "' has not unique method in class :" + this.owner.getName());
        }
        return findResult.size() == 0 ? null : findResult.get(0).getMethod();
    }

    /**
     * 桥接方法：桥接{@link #exactMethodByArgs(Set, Class[])}
     *
     * @param parameterTypes 参数类型数组
     * @return 匹配的方法
     */
    public Method exactMethodByArgs(Class<?>... parameterTypes) {
        return exactMethodByArgs(metaMethods, parameterTypes);
    }

    /**
     * 完全精确匹配方法:
     * 通过方法名和参数类型确切获取方法
     * 1.完全匹配方法签名（最佳）
     * 2.匹配方法参数是入参类型或入参的父类
     * 3.匹配方法参数小于等于入参个数，并且最后一个参数是array，则猜测为不定参数，尝试匹配最后一个参数之前的所有参数符合2的规则
     * 匹配1成功立刻返回，匹配2/3成功一次后继续尝试匹配1，最终无法匹配1则返回第一次匹配2/3成功的方法
     *
     * @param metaMethods    用于查询的元方法集合
     * @param parameterTypes 参数类型数组
     * @return 匹配的方法
     */
    private Method exactMethodByArgs(Set<MetaMethod> metaMethods, Class<?>... parameterTypes) {
        Method bestMatch = null;
        String targetSignature = mockSignature(methodKey, parameterTypes);
        int paramsCount = parameterTypes == null ? 0 : parameterTypes.length;
        for (MetaMethod metaMethod : metaMethods) {
            if (metaMethod.getSignature().equals(targetSignature)) {
                // 完全匹配为最匹配,参数个数为0也会在此匹配到
                bestMatch = metaMethod.getMethod();
                break;
            } else if (bestMatch == null) {
                // 推测匹配只需匹配过一次
                boolean match = false;
                int argsNum = metaMethod.getArgsNum();
                Class<?>[] curArgTypes = metaMethod.getMethod().getParameterTypes();
                if (argsNum == paramsCount) {
                    match = true;
                    // 判断每个方法参数是否是对应入参的类型或其子类
                    for (int i = 0; i < paramsCount; i++) {
                        if (!matchArgType(curArgTypes[i], parameterTypes[i])) {
                            match = false;
                            break;
                        }
                    }
                }
                if (!match &&
                        parameterTypes != null &&
                        paramsCount >= argsNum &&
                        argsNum != 0 &&
                        curArgTypes[argsNum - 1].isArray()) {
                    // 入参大于方法参数个数并且最后一个参数为数组，则存在不定参数的可能性
                    match = true;
                    int lastArgIndex = argsNum - 1;
                    Class<?> lastArgType = curArgTypes[lastArgIndex].getComponentType();
                    /* 1.判断除最后一个方法参数外的参数是否是对应入参的类型或其子类
                       2.判断最后一个数组参数的成员类型，对应的入参是否是该类型或其子类 */
                    for (int i = 0; i < paramsCount; i++) {
                        Class<?> matchClass = i < lastArgIndex ? curArgTypes[i] : lastArgType;
                        if (!matchArgType(matchClass, parameterTypes[i])) {
                            match = false;
                            break;
                        }
                    }
                }
                if (match) {
                    bestMatch = metaMethod.getMethod();
                }
            }
        }
        return bestMatch;
    }

    /**
     * 完全精确匹配方法：
     * 通过方法名、返回类型和参数类型，确切获取方法
     * 此方法必定匹配一个或者匹配不到
     *
     * @param parameterTypes 参数类型
     * @param returnType     返回类型
     * @return 方法
     */
    public Method exactMethod(Class<?> returnType, Class<?>... parameterTypes) {
        Set<MetaMethod> selectMethods = metaMethods.stream().filter(e -> e.getReturnType().equals(returnType)).collect(Collectors.toSet());
        return selectMethods.size() == 0 ? null : exactMethodByArgs(selectMethods, parameterTypes);
    }

    /**
     * 根据用户提供的信息推测方式匹配方法，不保证结果百分比准确
     * 除了方法名外 其他非必填
     *
     * @param returnType 返回类型，可为null
     * @param args       参数类型，可为空
     * @return 推测匹配的方法
     */
    public Method speculateMethod(Class<?> returnType, Class<?>... args) {
        // 尝试精准匹配返回类型
        if (returnType != null) {
            // 通过返回类型和参数类型精准匹配,null当作空处理
            Method result = exactMethod(returnType, args == null ? new Class[]{} : args);
            if (result != null) {
                // 完全精确匹配成功
                return result;
            } else {
                try {
                    // 匹配不中时尝试通过返回值匹配唯一方法
                    return exactMethodByReturn(returnType);
                } catch (FindNotUniqueMethodException e) {
                    // if args type contains Object.class,try by args number
                    if (args != null && containsObject(args)) {
                        try {
                            result = exactMethodByArgsNum(args.length);
                            if (returnType.equals(result.getReturnType())) {
                                return result;
                            }
                        } catch (FindNotUniqueMethodException e2) {
                            // try next;
                        }
                    }
                }
            }
        } else if (args != null) {
            // 通过参数类型精准匹配
            Method result;
            result = exactMethodByArgs(args);
            if (result != null) {
                return result;
            } else {
                try {
                    if (containsObject(args)) {
                        return exactMethodByArgsNum(args.length);
                    }
                } catch (FindNotUniqueMethodException e) {
                    // try next
                }
            }
        }

        // 无法通过返回值或者参数类型推断,直接匹配方法名,当有多个方法时抛出异常，信息不足，推断失败。
        return exactMethodByKey();
    }

    /**
     * 通过方法名与参数类型拼接方法签名，不包含返回类型
     *
     * @param methodKey      方法key
     * @param parameterTypes 参数类型
     * @return 方法签名
     */
    public String mockSignature(String methodKey, Class<?>... parameterTypes) {
        StringBuilder builder = new StringBuilder();
        builder.append(methodKey);
        if (parameterTypes != null && parameterTypes.length > 0) {
            builder.append(":");
            String args = Stream.of(parameterTypes)
                    .map(Class::getName)
                    .collect(Collectors.joining(","));
            builder.append(args);
        }
        return builder.toString();
    }

    public String getMethodKey() {
        return methodKey;
    }

    public Class<?> getOwner() {
        return owner;
    }

    public Set<MetaMethod> getMetaMethods() {
        return metaMethods;
    }

    /**
     * 是否包含Object
     *
     * @param classes 类型数组
     * @return 是否包含
     */
    private boolean containsObject(Class<?>[] classes) {
        for (Class<?> c : classes) {
            if (Object.class.equals(c)) {
                return true;
            }
        }
        return false;
    }


    /**
     * 匹配参数类型
     * 匹配入参是方法参数类型或者方法参数类型的子类,或者基础包装类
     *
     * @param curArgType    方法入参类型
     * @param parameterType 参数类型
     * @return 是否匹配
     */
    private boolean matchArgType(Class<?> curArgType, Class<?> parameterType) {
        return curArgType.isAssignableFrom(parameterType) || (curArgType.isPrimitive() && ReflectUtils.isWrapForm(parameterType, curArgType));
    }

}
