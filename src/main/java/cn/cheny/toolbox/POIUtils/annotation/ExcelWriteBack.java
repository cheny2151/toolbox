package cn.cheny.toolbox.POIUtils.annotation;

import org.apache.poi.hssf.util.HSSFColor;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * excel回写注解
 * 在需要回写数据字段上标注此注解，并赋值
 *
 * @author cheney
 * @date 2019-12-19
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ExcelWriteBack {

    /**
     * 标题
     */
    String title();

    /**
     * 标题颜色
     */
    HSSFColor.HSSFColorPredefined titleColor() default HSSFColor.HSSFColorPredefined.BLACK;

    /**
     * 排序
     */
    int sort() default 0;

    /**
     * 颜色
     */
    HSSFColor.HSSFColorPredefined color() default HSSFColor.HSSFColorPredefined.BLACK;

}
