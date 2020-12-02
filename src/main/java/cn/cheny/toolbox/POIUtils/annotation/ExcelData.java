package cn.cheny.toolbox.POIUtils.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ExcelData {

    int column() default 0;

    String columnTitle() default "";

    SwitchType type();

    enum SwitchType{
        // 根据列号选中
        COLUMN_NUM,
        // 根据列名选中
        COLUMN_TITLE
    }

}
