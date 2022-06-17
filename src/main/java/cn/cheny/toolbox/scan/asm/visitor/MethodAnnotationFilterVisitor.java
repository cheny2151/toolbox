package cn.cheny.toolbox.scan.asm.visitor;

import cn.cheny.toolbox.scan.asm.AnnotationDesc;
import cn.cheny.toolbox.scan.asm.MethodDesc;
import cn.cheny.toolbox.scan.filter.FilterResult;
import cn.cheny.toolbox.scan.filter.ScanFilter;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author by chenyi
 * @date 2021/12/27
 */
public class MethodAnnotationFilterVisitor extends ClassFilterVisitor {

    private final Map<String, Class<?>> methodAnnotations;
    private Map<MethodDesc, List<AnnotationDesc>> methodAnnotationDesc;

    public MethodAnnotationFilterVisitor(ScanFilter filter) {
        super(filter);
        this.methodAnnotations = filter.getExistMethodAnnotations().stream()
                .collect(Collectors.toMap(this::getDesc, clazz -> clazz));
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
        return new MethodVisitor(Opcodes.ASM5) {

            private final List<AnnotationValuesVisitor> annotationValuesVisitors = new ArrayList<>();

            @Override
            public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
                // 存在注解
                Class<?> annotation = methodAnnotations.get(descriptor);
                if (annotation != null) {
                    AnnotationValuesVisitor annotationValuesVisitor = new AnnotationValuesVisitor(annotation, descriptor);
                    annotationValuesVisitors.add(annotationValuesVisitor);
                    return annotationValuesVisitor;
                }
                return null;
            }

            @Override
            public void visitEnd() {
                if (annotationValuesVisitors.size() > 0) {
                    List<AnnotationDesc> descs = annotationValuesVisitors.stream()
                            .map(AnnotationValuesVisitor::getTargetAnnotationDesc)
                            .collect(Collectors.toList());
                    Map<MethodDesc, List<AnnotationDesc>> methodAnnotationDesc = MethodAnnotationFilterVisitor.this.methodAnnotationDesc;
                    if (methodAnnotationDesc == null) {
                        methodAnnotationDesc = new HashMap<>();
                        MethodAnnotationFilterVisitor.this.methodAnnotationDesc = methodAnnotationDesc;
                    }
                    methodAnnotationDesc.put(new MethodDesc(name, descriptor), descs);
                }
            }
        };
    }

    public Map<MethodDesc, List<AnnotationDesc>> getMethodAnnotationDesc() {
        return methodAnnotationDesc;
    }

    @Override
    public FilterResult getFilterResult() {
        FilterResult filterResult = super.getFilterResult();
        if (filterResult.isPass()) {
            Map<MethodDesc, List<AnnotationDesc>> methodAnnotationDesc = getMethodAnnotationDesc();
            if (methodAnnotationDesc != null) {
                filterResult.setAnnotationDescMap(methodAnnotationDesc);
            } else {
                filterResult.setPass(false);
            }
        }
        return filterResult;
    }

}
