package cn.cheny.toolbox.expression.executor;

import java.util.Map;

/**
 * 表达式执行器
 *
 * @author cheney
 * @date 2019-12-10
 */
public interface ExpressionExecutor {

    /**
     * 执行表达式
     *
     * @param env 参数
     * @return 结果
     */
    Object execute(Map<String, Object> env);

}
