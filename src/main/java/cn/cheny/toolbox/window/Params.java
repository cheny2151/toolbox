package cn.cheny.toolbox.window;

import cn.cheny.toolbox.exception.ToolboxRuntimeException;
import cn.cheny.toolbox.reflect.TypeUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;

import java.lang.reflect.Method;
import java.util.List;

/**
 * @author by chenyi
 * @date 2022/1/24
 */
public class Params {

    private final Method method;
    private final int argsCount;
    private final int batchArgIndex;
    private final boolean argIsCollection;
    private final Object[] argsTemplate;

    public Params(Object[] args, BatchConfiguration batchConfiguration) {
        Method method = batchConfiguration.getBatchMethod().getMethod();
        int batchArgIndex = batchConfiguration.getBatchArgIndex();
        checkArgs(args, method, batchArgIndex);
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
        this.argIsCollection = batchConfiguration.isArgIsCollection();
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

    private void checkArgs(Object[] args, Method method, int batchArgIndex) {
        if (args.length != method.getParameterCount()) {
            throw new ToolboxRuntimeException("Args length neq with method parameter count");
        }
        Class<?>[] parameterTypes = method.getParameterTypes();
        for (int i = 0; i < parameterTypes.length; i++) {
            if (i == batchArgIndex) {
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

        Params params = (Params) o;

        return new EqualsBuilder().append(method, params.method).isEquals();
    }

    @Override
    public int hashCode() {
        return method.hashCode();
    }
}
