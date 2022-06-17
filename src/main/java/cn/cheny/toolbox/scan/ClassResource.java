package cn.cheny.toolbox.scan;

import cn.cheny.toolbox.scan.asm.AnnotationDesc;
import cn.cheny.toolbox.scan.asm.MethodDesc;
import lombok.Data;
import lombok.ToString;

import java.util.List;
import java.util.Map;

/**
 * @author by chenyi
 * @date 2021/12/27
 */
@Data
@ToString
public class ClassResource {

    private Class<?> clazz;

    private Map<MethodDesc, List<AnnotationDesc>> annotationDesc;

    public ClassResource(Class<?> clazz) {
        this.clazz = clazz;
    }

    public ClassResource(Class<?> clazz, Map<MethodDesc, List<AnnotationDesc>> annotationDesc) {
        this.clazz = clazz;
        this.annotationDesc = annotationDesc;
    }
}
