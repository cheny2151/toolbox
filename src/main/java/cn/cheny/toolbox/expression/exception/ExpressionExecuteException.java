package cn.cheny.toolbox.expression.exception;

/**
 * 表达式执行异常
 *
 * @author cheney
 * @date 2020-01-07
 */
public class ExpressionExecuteException extends RuntimeException {

    public ExpressionExecuteException(String message) {
        super(message);
    }

    public ExpressionExecuteException(String expression, Throwable cause) {
        super("表达式 " + expression + " 执行异常:" + cause.getMessage(), cause.getCause());
    }
}
