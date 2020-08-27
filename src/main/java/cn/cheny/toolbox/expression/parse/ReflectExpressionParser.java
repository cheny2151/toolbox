package cn.cheny.toolbox.expression.parse;

import cn.cheny.toolbox.expression.executor.ExpressionExecutor;
import cn.cheny.toolbox.expression.executor.NullExpressionExecutor;
import cn.cheny.toolbox.expression.executor.ReflectExpressionExecutor;
import cn.cheny.toolbox.expression.func.InternalFunction;
import cn.cheny.toolbox.expression.model.FunctionClasses;
import cn.cheny.toolbox.expression.model.ParseResult;
import cn.cheny.toolbox.reflect.methodHolder.factory.DefaultMethodHolderFactory;
import cn.cheny.toolbox.reflect.methodHolder.factory.MethodHolderFactory;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collection;

/**
 * 反射表达式解析器
 * 通过解析表达式后用反射的方式结合Aviator执行表达式
 *
 * @author cheney
 * @date 2019-12-06
 */
@Slf4j
public class ReflectExpressionParser extends BaseExpressionParser {

    /**
     * 反射方法工厂
     */
    private MethodHolderFactory methodHolderFactory;

    /**
     * 执行反射的类
     */
    private FunctionClasses functionClasses;

    /**
     * ReflectExpressionParser单例
     */
    private static ReflectExpressionParser defaultReflectExpressionParser;

    /**
     * 内置方法
     */
    private final static FunctionClasses.FunctionClass INTERNAL_FUNCTION = new FunctionClasses.FunctionClass(InternalFunction.class, -999);

    private ReflectExpressionParser() {
        ClassLoader classLoader = ReflectExpressionParser.class.getClassLoader();
        InputStream resourceAsStream = classLoader.getResourceAsStream("func-config.conf");
        functionClasses = new FunctionClasses();
        // 添加内置函数类
        functionClasses.add(INTERNAL_FUNCTION);
        if (resourceAsStream != null) {
            try {
                // 读取func-config.conf配置中的类
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(resourceAsStream));
                String classesStr = bufferedReader.readLine();
                String[] classStrArray = classesStr.split(",");
                for (String s : classStrArray) {
                    Class<?> fClass = classLoader.loadClass(s);
                    functionClasses.addFunction(fClass);
                }
            } catch (Exception e) {
                log.error("ReflectExpressionParser初始化异常", e);
            }
        }
        methodHolderFactory = new DefaultMethodHolderFactory();
    }

    public ReflectExpressionParser(MethodHolderFactory methodHolderFactory, Collection<Class<?>> classes) {
        this.methodHolderFactory = methodHolderFactory;
        FunctionClasses functionClasses = new FunctionClasses(classes);
        functionClasses.add(INTERNAL_FUNCTION);
        this.functionClasses = functionClasses;
    }

    @Override
    public ExpressionExecutor parseExpression(String expression) {
        ParseResult parseResult = parse(expression);
        switch (parseResult.getType()) {
            case ParseResult.FUNC: {
                return new ReflectExpressionExecutor(expression, parseResult, this.methodHolderFactory, this.functionClasses);
            }
            case ParseResult.NULL_VALUE: {
                // 1.6 新增，处理'null'表达式解析结果
                return NullExpressionExecutor.getInstance();
            }
            default: {
                return parseOriginExpressionWithCache(expression);
            }
        }
    }

    /**
     * 动态添加方法反射类
     *
     * @param clazz 类
     */
    public void addFunctionClass(Class<?> clazz) {
        functionClasses.addFunction(clazz);
    }

    /**
     * 获取默认单例
     */
    public static ReflectExpressionParser getInstance() {
        if (defaultReflectExpressionParser == null) {
            synchronized (ReflectExpressionParser.class) {
                if (defaultReflectExpressionParser == null) {
                    defaultReflectExpressionParser = new ReflectExpressionParser();
                }
            }
        }
        return defaultReflectExpressionParser;
    }

    /**
     * 获取新的ReflectExpressionParser解析器实例
     *
     * @param methodHolderFactory 方法持有类工厂
     * @param classes             内置静态方法对应的类
     */
    public static ReflectExpressionParser getInstance(MethodHolderFactory methodHolderFactory, Collection<Class<?>> classes) {
        if (methodHolderFactory == null || classes == null) {
            throw new NullPointerException();
        }
        return new ReflectExpressionParser(methodHolderFactory, classes);
    }

}
