package cn.cheny.toolbox.expression.parse;

import cn.cheny.toolbox.expression.executor.ExpressionExecutor;

/**
 * 解析者接口
 *
 * @author cheney
 * @date 2019-12-10
 */
public interface ExpressionParser {

    /**
     * 解析表达式
     *
     * @param expression 表达式
     * @return 解析结果
     */
    ExpressionExecutor parseExpression(String expression);

}
