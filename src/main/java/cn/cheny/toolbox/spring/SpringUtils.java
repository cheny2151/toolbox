package cn.cheny.toolbox.spring;

import cn.cheny.toolbox.exception.ToolboxRuntimeException;
import cn.cheny.toolbox.other.parsing.GenericTokenParser;
import cn.cheny.toolbox.reflect.TypeUtils;
import cn.cheny.toolbox.scan.PathImplementationClassBuilder;
import cn.cheny.toolbox.scan.ScanException;
import cn.cheny.toolbox.spring.properties.ToolboxDefaultProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * 创建ApplicationContext时的钩子
 * <p>
 * 配合此类可以把spring容器管理的bean注入到静态变量里，构造一个静态工具类
 * 关键词：@Component，@DependsOn("springUtils")，@PostConstruct
 * 原理：@Component会创建一个类，@DependsOn("springUtils")会使该类依赖于SpringUtils创建
 * ，@PostConstruct通过创建类后将spring管理的bean通过SpringUtils获取并写入静态变量中
 */
@Slf4j
public class SpringUtils implements ApplicationContextAware {

    public static final String OPEN_TOKEN = "${";
    public static final String CLOSE_TOKEN = "}";

    private static final GenericTokenParser TOKEN_PARSER = new GenericTokenParser(OPEN_TOKEN, CLOSE_TOKEN);

    private static final ExpressionParser parser = new SpelExpressionParser();

    private static final StandardEvaluationContext evaluationContext = new StandardEvaluationContext();

    private static ApplicationContext applicationContext;

    private static Environment env;

    private static boolean inSpring = false;

    private final ToolboxDefaultProperties toolboxDefaultProperties;


    public SpringUtils(ToolboxDefaultProperties toolboxDefaultProperties) {
        this.toolboxDefaultProperties = toolboxDefaultProperties;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        SpringUtils.applicationContext = applicationContext;
        SpringUtils.env = applicationContext.getEnvironment();
        SpringUtils.inSpring = true;
        evaluationContext.setBeanResolver((context, beanName) -> applicationContext.getBean(beanName));
        new Thread(() -> {
            SpringUtilsAware.defaultAware().forEach(aware -> aware.after(toolboxDefaultProperties));
            List<SpringUtilsAware> awares = new ArrayList<>();
            try {
                Collection<SpringUtilsAware> customized =
                        PathImplementationClassBuilder.createInstances(toolboxDefaultProperties.getScannerPath(),
                                toolboxDefaultProperties.isScannerInAllJar(), SpringUtilsAware.class);
                if (customized.size() > 0) {
                    awares.addAll(customized);
                }
            } catch (ScanException e) {
                log.error("扫描SpringUtilsAware实现类失败", e);
            }
            awares.forEach(aware -> aware.after(toolboxDefaultProperties));
        }, "springUtils-init").start();
    }

    public static ApplicationContext getSpringApplicationContext() {
        checkInSpring();
        return applicationContext;
    }

    public static <T> T getBean(Class<T> tClass) {
        checkInSpring();
        return applicationContext.getBean(tClass);
    }

    public static <T> T getBean(String name, Class<T> tClass) {
        checkInSpring();
        return applicationContext.getBean(name, tClass);
    }

    public static Object getBean(String name) {
        checkInSpring();
        return applicationContext.getBean(name);
    }

    public static <T> Collection<T> getBeansOfType(Class<T> tClass) {
        checkInSpring();
        return applicationContext.getBeansOfType(tClass).values();
    }

    public static Object registerBean(String beanName, BeanDefinitionBuilder builder) {
        checkInSpring();
        DefaultListableBeanFactory beanFactory = requiredDefaultListableBeanFactory();
        AbstractBeanDefinition definition = builder.getRawBeanDefinition();
        if (beanFactory.containsBean(beanName)) {
            throw new ToolboxRuntimeException(String.format("Bean:'%s'已注册，若需覆盖请强制执行", beanName));
        }
        beanFactory.registerBeanDefinition(beanName, definition);
        return applicationContext.getBean(beanName);
    }

    public static Object forceRegisterBean(String beanName, BeanDefinitionBuilder builder) {
        checkInSpring();
        DefaultListableBeanFactory beanFactory = requiredDefaultListableBeanFactory();
        AbstractBeanDefinition definition = builder.getRawBeanDefinition();
        if (beanFactory.containsBean(beanName)) {
            log.info("Remove old BeanDefinition '{}' to register new", beanName);
            beanFactory.removeBeanDefinition(beanName);
        }
        beanFactory.registerBeanDefinition(beanName, definition);
        return applicationContext.getBean(beanName);
    }

    public static Environment getEnvironment() {
        checkInSpring();
        return env;
    }

    public static <T> T getProperty(String key, Class<T> type) {
        String property = getProperty(key);
        return TypeUtils.caseToObject(property, type);
    }

    public static String getProperty(String key) {
        checkInSpring();
        if (key.startsWith(OPEN_TOKEN) && key.endsWith(CLOSE_TOKEN)) {
            key = TOKEN_PARSER.parse(key);
        }
        return env.getProperty(key);
    }

    public static Object execute(String expressionString) {
        Expression expression = parser.parseExpression(expressionString);
        return expression.getValue(evaluationContext);
    }

    public static <T> T execute(String expressionString, Class<T> type) {
        Expression expression = parser.parseExpression(expressionString);
        return expression.getValue(evaluationContext, type);
    }

    public static boolean isInSpring() {
        return inSpring;
    }

    public static void checkInSpring() {
        if (!inSpring) {
            throw new NotInSpringException();
        }
    }

    private static DefaultListableBeanFactory requiredDefaultListableBeanFactory() {
        if (applicationContext instanceof ConfigurableApplicationContext) {
            ConfigurableApplicationContext configurableApplicationContext = (ConfigurableApplicationContext) applicationContext;
            ConfigurableListableBeanFactory beanFactory = configurableApplicationContext.getBeanFactory();
            if (beanFactory instanceof DefaultListableBeanFactory) {
                return (DefaultListableBeanFactory) beanFactory;
            }
        }
        throw new ToolboxRuntimeException("Can not get DefaultListableBeanFactory in spring context");
    }

}
