package cn.cheny.toolbox.scan.asm.visitor;

import cn.cheny.toolbox.scan.asm.AnnotationDesc;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Opcodes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 注解key，value获取
 *
 * @author by chenyi
 * @date 2021/12/27
 */
public class AnnotationValuesVisitor extends AnnotationVisitor {

    private final Class<?> annotation;
    private final String descriptor;

    private final Map<String, Object> values = new HashMap<>();

    private String curName;

    public AnnotationValuesVisitor(Class<?> annotation, String descriptor) {
        super(Opcodes.ASM5);
        this.annotation = annotation;
        this.descriptor = descriptor;
    }

    @Override
    public void visit(String name, Object value) {
        if (curName == null) {
            curName = name;
        }
        values.compute(curName, (k, oldV) -> {
            if (oldV == null) {
                return value;
            } else if (value instanceof List) {
                ((List<Object>) oldV).add(value);
                return oldV;
            } else {
                ArrayList<Object> newV = new ArrayList<>();
                newV.add(oldV);
                newV.add(value);
                return newV;
            }
        });
    }

    @Override
    public AnnotationVisitor visitArray(String name) {
        curName = name;
        return this;
    }

    @Override
    public void visitEnd() {
        curName = null;
    }

    public AnnotationDesc getTargetAnnotationDesc() {
        return new AnnotationDesc(annotation, descriptor, values);
    }

}
