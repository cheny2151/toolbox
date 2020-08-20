package cn.cheny.toolbox.expression.executor;

import java.util.Map;

/**
 * 空值表达式解析结果执行器
 * 固定返回null
 * <p>
 * 1.6 新增
 *
 * @author cheney
 * @version 1.6
 * @date 2019-12-30
 */
public class NullExpressionExecutor implements ExpressionExecutor {

    private final static NullExpressionExecutor NULL_EXPRESSION_EXECUTOR = new NullExpressionExecutor();

    @Override
    public Object execute(Map<String, Object> env) {
        return null;
    }

    /**
     * 获取实例
     *
     * @return 实例
     */
    public static NullExpressionExecutor getInstance() {
        return NULL_EXPRESSION_EXECUTOR;
    }
}
