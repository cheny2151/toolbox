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

    private Class<?> superClass;

    public ScanFilter() {
        hasAnnotations = new ArrayList<>();
    }

    public void addAnnotation(Class<? extends Annotation> annotation) {
        hasAnnotations.add(annotation);
    }

    public List<Class<? extends Annotation>> getHasAnnotations() {
        return hasAnnotations;
    }

    public Class<?> getSuperClass() {
        return superClass;
    }

    public void setSuperClass(Class<?> superClass) {
        this.superClass = superClass;
    }
}
