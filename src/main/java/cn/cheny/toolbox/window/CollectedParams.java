package cn.cheny.toolbox.window;

import cn.cheny.toolbox.exception.ToolboxRuntimeException;
import cn.cheny.toolbox.reflect.TypeUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;

import java.lang.reflect.Method;
import java.util.List;

/**
 * 窗口收集方法的参数实例
 *
 * @author by chenyi
 * @date 2022/1/24
 */
public class CollectedParams {

    private final Method method;
    private final int argsCount;
    private final int batchArgIndex;
    private boolean argIsCollection;
    private final Object[] argsTemplate;

    public CollectedParams(Object[] args, BatchConfiguration batchConfiguration) {
        Method method = batchConfiguration.getBatchMethod().getMethod();
        int batchArgIndex = batchConfiguration.getBatchArgIndex();
        CollectedMethod collectedMethod = batchConfiguration.getCollectedMethod();
        checkArgs(args, method, batchArgIndex, collectedMethod);
        int length = args.length;
        if (length > 1) {
            Object[] argsTemplate = new Object[length];
            System.arraycopy(args, 0, argsTemplate, 0, length);
            argsTemplate[batchArgIndex] = null;
            this.argsTemplate = argsTemplate;
        } else {
            this.argsTemplate = null;
        }
        this.method = method;
        this.argsCount = length;
        this.batchArgIndex = batchArgIndex;
    }

    public WindowElement buildElement(Object[] inputs) {
        Object input = inputs[batchArgIndex];
        if (argIsCollection) {
            if (!(input instanceof List)) {
                throw new ToolboxRuntimeException("Batch method target args must be List");
            }
            return new WindowElementCollection((List<?>) input);
        } else {
            return new WindowElement(input);
        }
    }

    public Object[] buildArgs(List<Object> inputs) {
        Object[] args = new Object[argsCount];
        if (argsTemplate != null) {
            System.arraycopy(argsTemplate, 0, args, 0, argsCount);
        }
        args[batchArgIndex] = inputs;
        return args;
    }

    private void checkArgs(Object[] args, Method method, int batchArgIndex, CollectedMethod collectedMethod) {
        if (args.length != method.getParameterCount()) {
            throw new ToolboxRuntimeException("Args length neq with method parameter count");
        }
        Class<?>[] parameterTypes = method.getParameterTypes();
        for (int i = 0; i < parameterTypes.length; i++) {
            if (i == batchArgIndex) {
                if (collectedMethod != null) {
                    Class<?> collectedParamType = collectedMethod.getMethod().getParameterTypes()[i];
                    this.argIsCollection = List.class.isAssignableFrom(collectedParamType);
                } else {
                    this.argIsCollection = true;
                }
                continue;
            }
            Object arg = args[i];
            if (arg != null) {
                Class<?> parameterType = TypeUtils.ifPrimitiveToWrapClass(parameterTypes[i]);
                Class<?> argClass = arg.getClass();
                if (!parameterType.isAssignableFrom(argClass)) {
                    throw new ToolboxRuntimeException("Parameters collectedMethod and batchMethod cannot be matched");
                }
            }
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        CollectedParams collectedParams = (CollectedParams) o;

        return new EqualsBuilder().append(method, collectedParams.method).isEquals();
    }

    @Override
    public int hashCode() {
        return method.hashCode();
    }
}
