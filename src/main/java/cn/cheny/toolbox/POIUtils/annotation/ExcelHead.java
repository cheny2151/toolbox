package cn.cheny.toolbox.POIUtils.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ExcelHead {

    String headTitle() default "";

    String sheetName() default "sheet1";

    int titleRow() default 0;

    int endRow() default -1;

}
