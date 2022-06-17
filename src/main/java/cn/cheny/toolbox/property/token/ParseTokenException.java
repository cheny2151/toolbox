package cn.cheny.toolbox.property.token;

import cn.cheny.toolbox.exception.ToolboxRuntimeException;

/**
 * 字符串token规则解析异常
 *
 * @date 2021/2/8
 * @author by chenyi
 */
public class ParseTokenException extends ToolboxRuntimeException {

    public ParseTokenException() {
    }

    public ParseTokenException(String message) {
        super(message);
    }
}
