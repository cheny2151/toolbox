package cn.cheny.toolbox.scan.asm.visitor;

import cn.cheny.toolbox.scan.PackageUtils;
import cn.cheny.toolbox.scan.filter.FilterResult;
import cn.cheny.toolbox.scan.filter.ScanFilter;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Opcodes;

import java.lang.annotation.Annotation;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * asm ClassVisitor实现类，根据字节码文件过滤类，避免直接Load Class
 *
 * @author by chenyi
 * @date 2021/12/27
 */
public class ClassFilterVisitor extends ClassVisitor {

    /**
     * 字节码类描述符前缀: 'L'
     */
    private final static String CLASS_SIGNATURE_PRE = "L";

    /**
     * 字节码类描述符后缀: ';'
     */
    private final static String CLASS_SIGNATURE_TAIL = ";";

    private boolean passVisit;

    private int passVisitAnnotation;

    private String superClass;

    private List<String> annotations;

    public ClassFilterVisitor(ScanFilter filter) {
        super(Opcodes.ASM5);
        this.passVisit = false;
        this.passVisitAnnotation = 0;
        List<Class<? extends Annotation>> hasAnnotations = filter.getHasAnnotations();
        if (CollectionUtils.isNotEmpty(hasAnnotations)) {
            this.annotations = hasAnnotations.stream()
                    .map(this::getDesc)
                    .collect(Collectors.toList());
        } else {
            this.annotations = Collections.emptyList();
        }
        Class<?> superClass = filter.getSuperClass();
        if (superClass != null) {
            this.superClass = superClass.getName();
        }
    }

    @Override
    public void visit(int i, int access, String className, String signature, String superClass, String[] interfaces) {
        boolean accessPass = Modifier.isPublic(access);
        boolean superClassFilter = this.superClass == null;
        if (!superClassFilter) {
            String superClassUrl = PackageUtils.replacePackageToUrl(this.superClass);
            if (superClass != null &&
                    superClass.equals(superClassUrl)) {
                superClassFilter = true;
            } else if (interfaces.length > 0 && ArrayUtils.contains(interfaces, superClassUrl)) {
                superClassFilter = true;
            }
        }
        this.passVisit = accessPass && superClassFilter;
    }

    @Override
    public AnnotationVisitor visitAnnotation(String annotationName, boolean b) {
        if (annotations.contains(annotationName)) {
            this.passVisitAnnotation++;
        }
        return null;
    }

    protected String getDesc(Class<? extends Annotation> clazz) {
        return CLASS_SIGNATURE_PRE + PackageUtils.replacePackageToUrl(clazz.getName()) + CLASS_SIGNATURE_TAIL;
    }

    public FilterResult getFilterResult() {
        boolean pass = passVisit && passVisitAnnotation == annotations.size();
        return new FilterResult(pass);
    }
}
