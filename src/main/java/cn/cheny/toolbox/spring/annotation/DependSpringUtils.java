package cn.cheny.toolbox.spring.annotation;

import org.springframework.context.annotation.DependsOn;

import java.lang.annotation.*;

/**
 * spring依赖{@link cn.cheny.toolbox.spring.SpringUtils}
 *
 * @author by chenyi
 * @date 2021/7/20
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@DependsOn("toolbox:springUtils")
public @interface DependSpringUtils {
}
