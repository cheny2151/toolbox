package cn.cheny.toolbox.expression.parse;

import com.googlecode.aviator.AviatorEvaluator;
import com.googlecode.aviator.AviatorEvaluatorInstance;
import com.googlecode.aviator.Options;
import com.googlecode.aviator.runtime.function.AbstractVariadicFunction;
import com.googlecode.aviator.runtime.type.AviatorObject;
import com.googlecode.aviator.runtime.type.AviatorString;
import cn.cheny.toolbox.expression.executor.AviatorExpressionExecutor;
import cn.cheny.toolbox.expression.executor.ExpressionExecutor;

import java.util.Map;

/**
 * Aviator表达式解析器
 * 底层直接使用Aviator进行解析
 *
 * @author cheney
 * @date 2019-12-10
 */
public class AviatorExpressionParser implements ExpressionParser {

    /**
     * Aviator解析器
     */
    private AviatorEvaluatorInstance aviatorEvaluator;

    /**
     * AviatorExpressionParser单例
     */
    private static AviatorExpressionParser AviatorExpressionParser;

    private AviatorExpressionParser() {
        aviatorEvaluator = AviatorEvaluator.getInstance();
        aviatorEvaluator.setOption(Options.ALWAYS_PARSE_FLOATING_POINT_NUMBER_INTO_DECIMAL, true);
        aviatorEvaluator.setOption(Options.ALWAYS_PARSE_INTEGRAL_NUMBER_INTO_DECIMAL, true);
        init();
    }

    @Override
    public ExpressionExecutor parseExpression(String expression) {
        return new AviatorExpressionExecutor(aviatorEvaluator.compile(expression, false));
    }

    /**
     * 默认的解析方法不带缓存{@link #parseExpression(String)}，提供此方法解析表达式并缓存解析结果
     *
     * @param expression 表达式
     * @return 表达式解析结果
     */
    public ExpressionExecutor parseExpressionWithCache(String expression) {
        return new AviatorExpressionExecutor(aviatorEvaluator.compile(expression, true));
    }

    /**
     * 获取AviatorExpressionParser实例
     *
     * @return
     */
    public static AviatorExpressionParser getInstance() {
        if (AviatorExpressionParser == null) {
            synchronized (AviatorExpressionParser.class) {
                if (AviatorExpressionParser == null) {
                    AviatorExpressionParser = new AviatorExpressionParser();
                }
            }
        }
        return AviatorExpressionParser;
    }

    /**
     * 初始化方法
     */
    private void init() {
        aviatorEvaluator.addFunction(new IfsAviatorFunction());
    }

    /**
     * Aviator提供实现AbstractVariadicFunction增加函数
     */
    private static class IfsAviatorFunction extends AbstractVariadicFunction {

        @Override
        public AviatorObject variadicCall(Map<String, Object> env, AviatorObject... args) {
            if (args != null && args.length > 0 && (args.length & 1) == 0) {
                for (int i = 0; i < args.length; i++) {
                    if ((Boolean) args[i++].getValue(env)) {
                        return args[i];
                    }
                }
                return new AviatorString(null);
            }
            throw new IllegalArgumentException();
        }

        @Override
        public String getName() {
            return "ifs";
        }
    }

}
