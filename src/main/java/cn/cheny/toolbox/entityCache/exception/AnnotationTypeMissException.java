package cn.cheny.toolbox.entityCache.exception;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;

/**
 * 未发现目标注解异常
 *
 * @author cheney
 * @date 2020-08-31
 */
public class AnnotationTypeMissException extends RuntimeException {

    /**
     * 未发现的注解
     */
    private Class<? extends Annotation> target;
    /**
     * 未发现的成员
     */
    private AnnotatedElement member;

    public AnnotationTypeMissException(Class<? extends Annotation> target, AnnotatedElement member) {
        super("Miss annotation type " + target
                + "in member " + member);
        this.target = target;
        this.member = member;
    }

    public Class<? extends Annotation> getTarget() {
        return target;
    }

    public AnnotatedElement getMember() {
        return member;
    }
}
