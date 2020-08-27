package cn.cheny.toolbox.expression.executor;

import cn.cheny.toolbox.expression.model.Arg;
import cn.cheny.toolbox.expression.model.ParseResult;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static cn.cheny.toolbox.expression.CharConstants.*;


/**
 * 表达式执行器
 *
 * @author cheney
 * @date 2019-12-06
 */
public abstract class BaseExpressionExecutor implements ExpressionExecutor {

    /**
     * 系统创建的环境值key
     */
    public static final String NEW_ENV_KEY = "_ENV_KEY";

    /**
     * 表达式
     */
    protected String express;

    /**
     * 表达式解析结果
     */
    protected ParseResult parseResult;

    BaseExpressionExecutor(String express, ParseResult parseResult) {
        this.express = express;
        this.parseResult = parseResult;
    }

    /**
     * 从env中提取arg对应的参数
     *
     * @param args 表达式参数
     * @param env  实际参数
     * @return 参数数组
     */
    @SuppressWarnings("unchecked")
    protected Object[] loadArgs(List<Arg> args, Map<String, Object> env) {
        return CollectionUtils.isEmpty(args) ? null : args.stream().map(arg -> {
            Object value = arg.getValue();
            short type = arg.getType();
            if (value == null) {
                return null;
            } else if (type == Arg.CONSTANT) {
                return value;
            } else if (type == Arg.FUNC) {
                ParseResult parseResult = (ParseResult) value;
                return executeFunc(parseResult.getFuncName(), parseResult.getArgs(), env);
            } else if (type == Arg.COMBINATION) {
                // 函数嵌套运算
                ArrayList<Arg> funcArgs = (ArrayList<Arg>) value;
                String operatorExpression = "";
                int newEnvIndex = 0;
                for (Arg funcArg : funcArgs) {
                    Object argValue = funcArg.getValue();
                    short funcArgType = funcArg.getType();
                    if (funcArgType == Arg.FUNC) {
                        ParseResult parseResult = (ParseResult) argValue;
                        Object expression = executeFunc(parseResult.getFuncName(), parseResult.getArgs(), env);
                        // 函数执行结果作为env的值，缓存最终形成的表达式解析结果
                        String newEnvKey = NEW_ENV_KEY + newEnvIndex++;
                        env.put(newEnvKey, expression);
                        operatorExpression += newEnvKey;
                    } else if (funcArgType == Arg.CONSTANT) {
                        // 常量则加上'
                        operatorExpression += APOSTROPHE_STRING + argValue + APOSTROPHE_STRING;
                    } else {
                        // 运算符或者原始类型，直接拼接
                        operatorExpression += argValue;
                    }
                }
                return executeOperation(operatorExpression, env, true);
            } else {
                String valueStr = (String) value;
                Object envArg = env.get(valueStr);
                if (envArg != null) {
                    return envArg;
                } else if (CONTAINS_OPERATOR_PATTERN.matcher(valueStr).find()) {
                    // 结合Aviator,将含运算符的arg丢给Aviator执行
                    return executeOperation(valueStr, env, true);
                }
                return castToBasic(valueStr);
            }
        }).toArray();
    }

    /**
     * 尝试将变量转换基本类型数据
     *
     * @param valueStr 待转换的值
     * @return 转换结果
     */
    private static Object castToBasic(String valueStr) {
        if (ArrayUtils.contains(NULL_VALUES, valueStr)) {
            return null;
        } else if ("false".equals(valueStr) || "true".equals(valueStr)) {
            return Boolean.valueOf(valueStr);
        } else if (NUMBER.matcher(valueStr).matches()) {
            if (valueStr.contains(".")) {
                return new BigDecimal(valueStr);
            } else {
                return Integer.valueOf(valueStr);
            }
        }
        return null;
    }

    /**
     * 执行运算表达式
     *
     * @param expression 运算表达式
     * @param env        实际参数
     * @param cache      是否使用缓存
     * @return 表达式执行结果
     */
    protected abstract Object executeOperation(String expression, Map<String, Object> env, boolean cache);

    /**
     * 提供额外的表达式执行方法
     *
     * @param functionName 方法名
     * @param args         参数
     * @param env          变量
     * @return 表达式执行结果
     */
    protected abstract Object executeFunc(String functionName, List<Arg> args, Map<String, Object> env);

}
