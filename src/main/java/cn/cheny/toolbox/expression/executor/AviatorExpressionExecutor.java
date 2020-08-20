package cn.cheny.toolbox.expression.executor;

import com.googlecode.aviator.Expression;

import java.util.Map;

/**
 * Aviator表达式执行器
 * 直接包装Expression执行表达式
 *
 * @author cheney
 * @date 2019-12-10
 */
public class AviatorExpressionExecutor implements ExpressionExecutor {

    /**
     * Aviator的表达式编译结果对象
     */
    private Expression expression;

    public AviatorExpressionExecutor(Expression expression) {
        this.expression = expression;
    }

    @Override
    public Object execute(Map<String, Object> env) {
        return expression.execute(env);
    }

    public Expression getExpression() {
        return expression;
    }
}
