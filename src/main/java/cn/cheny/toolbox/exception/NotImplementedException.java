package cn.cheny.toolbox.exception;

/**
 * 未实现异常
 *
 * @author cheney
 * @date 2021-05-09
 */
public class NotImplementedException extends ToolboxRuntimeException{

    public NotImplementedException() {
    }

    public NotImplementedException(String message) {
        super(message);
    }
}
