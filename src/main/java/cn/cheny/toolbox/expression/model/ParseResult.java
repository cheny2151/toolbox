package cn.cheny.toolbox.expression.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

/**
 * 表达式解析结果
 * 例如: ifs(a>b,c) 则
 * args字段：a>b与c {@link Arg}
 * funcName字段：ifs
 * noFunc字段:当表达式不为函数时，该字段为true
 */
@Data
@AllArgsConstructor
public class ParseResult {

    /**
     * 类型枚举值
     */
    public final static short FUNC = 1;
    public final static short ORIGIN = 2;
    public final static short NULL_VALUE = -1;

    /**
     * 解析结果为NULL
     */
    public final static ParseResult NULL_RESULT = new ParseResult(null, null, NULL_VALUE);

    private String funcName;
    private List<Arg> args;
    private short type;

    /**
     * 原始类型时,funcName作为完整的原始类型表达式
     *
     * @param expression 表达式
     */
    public static ParseResult origin(String expression) {
        return new ParseResult(expression, null, ORIGIN);
    }

    public static ParseResult func(String funcName, List<Arg> args) {
        return new ParseResult(funcName, args, FUNC);
    }

    public boolean isFunc() {
        return FUNC == this.type;
    }
}
