package cn.cheny.toolbox.reflect;

import cn.cheny.toolbox.exception.ToolboxRuntimeException;

/**
 * 将反射的检查类型错误转换为非检查类型错误
 */
public class ReflectException extends ToolboxRuntimeException {

    public ReflectException(String message) {
        super(message);
    }

    public ReflectException(String message, Throwable cause) {
        super(message, cause);
    }
}
