package cn.cheny.toolbox.exception;

/**
 * toolbox统一root运行时异常
 *
 * @Date 2021/5/8
 * @author by chenyi
 */
public class ToolboxRuntimeException extends RuntimeException {

    public ToolboxRuntimeException() {
    }

    public ToolboxRuntimeException(String message) {
        super(message);
    }

    public ToolboxRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }

    public ToolboxRuntimeException(Throwable cause) {
        super(cause);
    }

    public ToolboxRuntimeException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
    
}
