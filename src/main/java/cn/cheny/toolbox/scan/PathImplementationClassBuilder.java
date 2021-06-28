package cn.cheny.toolbox.scan;

import cn.cheny.toolbox.reflect.ReflectUtils;
import cn.cheny.toolbox.scan.filter.ScanFilter;
import lombok.extern.slf4j.Slf4j;

import java.lang.annotation.Annotation;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * 扫描项目包并实例化接口实现类/子类
 *
 * @author by chenyi
 * @date 2021/5/10
 */
@Slf4j
public class PathImplementationClassBuilder {

    /**
     * 扫描接口实现类并实例化
     *
     * @return super class实现类集合
     */
    public static <T> Collection<T> createInstances(Class<T> superClass, Class<?>... annotations) throws ScanException {
        return createInstances("", false, null, superClass, annotations);
    }

    /**
     * 扫描接口实现类并实例化
     *
     * @return super class实现类集合
     */
    public static <T> Collection<T> createInstances(String path, Class<T> superClass, Class<?>... annotations) throws ScanException {
        return createInstances(path, false, null, superClass, annotations);
    }


    /**
     * 扫描接口实现类并实例化(扫描所有jar包)
     *
     * @return super class实现类集合
     */
    public static <T> Collection<T> createInstancesInAllJar(Class<T> superClass, Class<?>... annotations) throws ScanException {
        return createInstances("", true, null, superClass, annotations);
    }

    /**
     * 扫描接口实现类并实例化(扫描所有jar包)
     *
     * @return super class实现类集合
     */
    public static <T> Collection<T> createInstancesInAllJar(String path, Class<T> superClass, Class<?>... annotations) throws ScanException {
        return createInstances(path, true, null, superClass, annotations);
    }

    /**
     * 扫描接口实现类并实例化
     *
     * @return super class实现类集合
     */
    @SuppressWarnings("unchecked")
    public static <T> Collection<T> createInstances(String path, boolean scanAllJar, IsLoadingJar isLoadingJar,
                                                    Class<T> superClass, Class<?>... annotations) throws ScanException {
        ScanFilter scanFilter = new ScanFilter();
        scanFilter.setSuperClass(superClass);
        if (annotations != null) {
            Arrays.stream(annotations).forEach(annotation -> {
                if (annotation.isAnnotation()) {
                    scanFilter.addAnnotation((Class<? extends Annotation>) annotation);
                }
            });
        }
        PathScanner pathScanner = new PathScanner(scanFilter);
        List<T> instances = new ArrayList<>();
        List<Class<?>> targetClass = pathScanner.scanAllJar(scanAllJar)
                .isLoadingJar(isLoadingJar)
                .scanClass(path);
        targetClass.forEach(c -> {
            int modifiers = c.getModifiers();
            if (c.isInterface() || (modifiers & Modifier.ABSTRACT) > 0
                    || (modifiers & Modifier.PUBLIC) == 0) {
                return;
            }
            try {
                instances.add((T) ReflectUtils.newObject(c, null, null));
            } catch (Exception e) {
                log.error("can not create class instance,class:{}", c, e);
            }
        });
        return instances;
    }

}
