package cn.cheny.toolbox.expression.exception;

import cn.cheny.toolbox.exception.ToolboxRuntimeException;

/**
 * 表达式解析异常类
 *
 * @author cheney
 * @date 2019-12-09
 */
public class ExpressionParseException extends ToolboxRuntimeException {

    public ExpressionParseException() {
    }

    public ExpressionParseException(String message) {
        super(message);
    }

}
