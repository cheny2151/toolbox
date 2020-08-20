package cn.cheny.toolbox.POIUtils.utils;

import cn.cheny.toolbox.POIUtils.annotation.ExcelCell;
import cn.cheny.toolbox.POIUtils.annotation.ExcelData;
import cn.cheny.toolbox.POIUtils.annotation.ExcelHead;
import javassist.*;
import javassist.bytecode.AnnotationsAttribute;
import javassist.bytecode.ClassFile;
import javassist.bytecode.ConstPool;
import javassist.bytecode.annotation.Annotation;
import javassist.bytecode.annotation.EnumMemberValue;
import javassist.bytecode.annotation.StringMemberValue;

import java.util.concurrent.ConcurrentHashMap;

/**
 * @author cheney
 * @date 2020-07-29
 */
public class DynamicTargetExcelClassBuilder {

    private static final ConcurrentHashMap<String, Class<?>> CLASS_CACHE = new ConcurrentHashMap<>();

    public static Class<?> crateClassForExcel(String resource) {
        Class<?> resultClass = CLASS_CACHE.get(resource);
        if (resultClass != null) {
            return resultClass;
        }
        ClassPool classPool = ClassPool.getDefault();
        ClassClassPath classPath = new ClassClassPath(DynamicTargetExcelClassBuilder.class);
        classPool.insertClassPath(classPath);
        try {
            CtClass newExportClass = classPool.makeClass(DynamicTargetExcelClassBuilder.class.getName() + "$" + resource);

            // 类上新增注解
            addAttributeExcelHeadForClass(newExportClass);

            // 新增字段
            addProperty(classPool, newExportClass);

            //noinspection unchecked
            resultClass = newExportClass.toClass();
            CLASS_CACHE.put(resource, resultClass);
            return resultClass;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }
    }

    /**
     * 新增字段--列表A
     */
    private static void addProperty(ClassPool classPool, CtClass cc) throws NotFoundException, CannotCompileException {
        CtField field = createField(classPool, cc, Integer.class, "columnA");
        // 添加注解
        ClassFile classFile = cc.getClassFile();
        ConstPool constPool = classFile.getConstPool();
        AnnotationsAttribute fieldAttr = new AnnotationsAttribute(constPool, AnnotationsAttribute.visibleTag);

        addAttributeExcelData(fieldAttr, constPool, "列表A");
        addAttributeExcelCell(fieldAttr, constPool, "列表A");

        field.getFieldInfo().addAttribute(fieldAttr);
    }

    /**
     * 创建字段
     */
    private static CtField createField(ClassPool classPool, CtClass cc, Class<?> type, String fieldName)
            throws CannotCompileException, NotFoundException {
        CtField field = new CtField(classPool.get(type.getName()), fieldName, cc);
        field.setModifiers(Modifier.PRIVATE);
        cc.addField(field);
        String name = toUpperFirstLetter(fieldName);
        cc.addMethod(CtNewMethod.setter("set" + name, field));
        cc.addMethod(CtNewMethod.getter("get" + name, field));
        return field;
    }

    /**
     * 为字段创建注解--{@link ExcelData}
     */
    private static void addAttributeExcelData(AnnotationsAttribute fieldAttr, ConstPool constPool, String titleName) {
        Annotation excelData = new Annotation(ExcelData.class.getName(), constPool);
        excelData.addMemberValue("columnTitle", new StringMemberValue(titleName, constPool));
        EnumMemberValue memberValue = new EnumMemberValue(constPool);
        memberValue.setType(ExcelData.SwitchType.class.getName());
        memberValue.setValue("COLUMN_TITLE");
        excelData.addMemberValue("type", memberValue);
        fieldAttr.addAnnotation(excelData);
    }

    /**
     * 为字段创建注解--{@link ExcelCell}
     */
    private static void addAttributeExcelCell(AnnotationsAttribute fieldAttr, ConstPool constPool, String titleName) {
        Annotation min = new Annotation(ExcelCell.class.getName(), constPool);
        min.addMemberValue("name", new StringMemberValue(titleName, constPool));
        fieldAttr.addAnnotation(min);
    }

    /**
     * 为类创建注解--{@link ExcelHead}
     */
    private static void addAttributeExcelHeadForClass(CtClass cc) {
        ClassFile classFile = cc.getClassFile();
        ConstPool constPool = classFile.getConstPool();
        AnnotationsAttribute classAttr = new AnnotationsAttribute(constPool, AnnotationsAttribute.visibleTag);
        Annotation excelHead = new Annotation(ExcelHead.class.getName(), constPool);
        classAttr.addAnnotation(excelHead);
        classFile.addAttribute(classAttr);
    }

    private static String toUpperFirstLetter(String fieldName) {
        return fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
    }
}
