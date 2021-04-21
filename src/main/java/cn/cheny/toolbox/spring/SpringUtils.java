package cn.cheny.toolbox.spring;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.env.Environment;

import java.util.Collection;

/**
 * 创建ApplicationContext时的钩子
 * <p>
 * 配合此类可以把spring容器管理的bean注入到静态变量里，构造一个静态工具类
 * 关键词：@Component，@DependsOn("springUtils")，@PostConstruct
 * 原理：@Component会创建一个类，@DependsOn("springUtils")会使该类依赖于SpringUtils创建
 * ，@PostConstruct通过创建类后将spring管理的bean通过SpringUtils获取并写入静态变量中
 */
public class SpringUtils implements ApplicationContextAware {

    private static ApplicationContext applicationContext;

    private static Environment env;

    private static boolean inSpring = false;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        SpringUtils.applicationContext = applicationContext;
        SpringUtils.env = applicationContext.getEnvironment();
        SpringUtils.inSpring = true;
        getBeansOfType(SpringUtilsAware.class).forEach(SpringUtilsAware::after);
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

    public static boolean isInSpring() {
        return inSpring;
    }

    public static void checkInSpring() {
        if (!inSpring) {
            throw new NotInSpringException();
        }
    }
}
