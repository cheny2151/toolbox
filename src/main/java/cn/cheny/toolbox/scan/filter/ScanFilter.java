package cn.cheny.toolbox.scan.filter;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;

/**
 * class扫描过滤器
 *
 * @author cheney
 * @date 2020-08-10
 */
public class ScanFilter {

    private List<Class<? extends Annotation>> hasAnnotations;

    private List<Class<? extends Annotation>> existMethodAnnotations;

    private Class<?> superClass;

    public ScanFilter addAnnotation(Class<? extends Annotation> annotation) {
        if (hasAnnotations == null) {
            hasAnnotations = new ArrayList<>();
        }
        hasAnnotations.add(annotation);
        return this;
    }

    public ScanFilter existMethodAnnotations(Class<? extends Annotation> annotation) {
        if (existMethodAnnotations == null) {
            existMethodAnnotations = new ArrayList<>();
        }
        existMethodAnnotations.add(annotation);
        return this;
    }

    public ScanFilter superClass(Class<?> superClass) {
        this.superClass = superClass;
        return this;
    }

    public List<Class<? extends Annotation>> getHasAnnotations() {
        return hasAnnotations;
    }

    public List<Class<? extends Annotation>> getExistMethodAnnotations() {
        return existMethodAnnotations;
    }

    public Class<?> getSuperClass() {
        return superClass;
    }

    public void setSuperClass(Class<?> superClass) {
        this.superClass = superClass;
    }
}
