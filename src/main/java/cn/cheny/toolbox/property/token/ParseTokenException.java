package cn.cheny.toolbox.property.token;

/**
 * 字符串token规则解析异常
 *
 * @Date 2021/2/8
 * @Created by chenyi
 */
public class ParseTokenException extends RuntimeException{

    public ParseTokenException() {
    }

    public ParseTokenException(String message) {
        super(message);
    }
}
