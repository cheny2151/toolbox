package cn.cheny.toolbox.spring;

import cn.cheny.toolbox.other.parsing.GenericTokenParser;
import cn.cheny.toolbox.reflect.TypeUtils;
import cn.cheny.toolbox.scan.PathImplementationClassBuilder;
import cn.cheny.toolbox.scan.ScanException;
import cn.cheny.toolbox.spring.properties.ToolboxDefaultProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.env.Environment;

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

    public static boolean isInSpring() {
        return inSpring;
    }

    public static void checkInSpring() {
        if (!inSpring) {
            throw new NotInSpringException();
        }
    }
}
