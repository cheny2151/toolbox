package cn.cheny.toolbox.window;

import java.lang.annotation.*;

/**
 * 批处理-被收集方法
 *
 * @author by chenyi
 * @date 2022/1/20
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Collected {

    String group();

    boolean argIsCollection() default false;
}
