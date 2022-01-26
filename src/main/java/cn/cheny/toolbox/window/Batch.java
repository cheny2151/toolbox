package cn.cheny.toolbox.window;

import cn.cheny.toolbox.window.output.BatchResultSplitter;
import cn.cheny.toolbox.window.output.DefaultBatchResultSplitter;
import org.apache.pdfbox.multipdf.Splitter;

import java.lang.annotation.*;

/**
 * @author by chenyi
 * @date 2022/1/20
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Batch {

    String group() default "";

    int batchArgIndex() default 0;

    long winTime() default 100;

    int threshold() default 64;

    int threadPoolSize() default 1;

    Class<? extends BatchResultSplitter> splitter() default DefaultBatchResultSplitter.class;

    String splitterName() default "";

}
