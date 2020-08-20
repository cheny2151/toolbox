package cn.cheny.toolbox.expression.exception;

/**
 * 表达式解析异常类
 *
 * @author cheney
 * @date 2019-12-09
 */
public class ExpressionParseException extends RuntimeException {

    public ExpressionParseException() {
    }

    public ExpressionParseException(String message) {
        super(message);
    }

}
