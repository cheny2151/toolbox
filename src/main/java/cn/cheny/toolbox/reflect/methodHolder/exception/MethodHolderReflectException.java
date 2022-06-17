package cn.cheny.toolbox.reflect.methodHolder.exception;

import cn.cheny.toolbox.exception.ToolboxRuntimeException;

/**
 * MethodHolder模块通用反射异常
 *
 * @author cheney
 * @date 2019-12-06
 */
public class MethodHolderReflectException extends ToolboxRuntimeException {

    public MethodHolderReflectException(String message) {
        super(message);
    }

    public MethodHolderReflectException(String message, Throwable cause) {
        super(message, cause);
    }
}
