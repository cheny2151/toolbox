package cn.cheny.toolbox.scan.asm;

import lombok.ToString;

import java.util.Map;

/**
 * @author by chenyi
 * @date 2021/12/27
 */
@ToString
public class AnnotationDesc implements Descriptor {

    private final Class<?> annotation;
    private final String descriptor;

    private final Map<String, Object> values;

    public AnnotationDesc(Class<?> annotation, String descriptor, Map<String, Object> values) {
        this.annotation = annotation;
        this.descriptor = descriptor;
        this.values = values;
    }

    @Override
    public Type type() {
        return Type.ANNOTATION;
    }

    public Class<?> getAnnotationClass() throws ClassNotFoundException {
        if (annotation == null) {
            return Descriptor.descToClass(descriptor, Thread.currentThread().getContextClassLoader());
        } else {
            return annotation;
        }
    }

    public Map<String, Object> getValues() {
        return values;
    }

}
