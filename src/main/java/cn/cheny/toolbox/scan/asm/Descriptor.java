package cn.cheny.toolbox.scan.asm;

import cn.cheny.toolbox.scan.PackageUtils;
import cn.cheny.toolbox.scan.PathScanner;

/**
 * @author by chenyi
 * @date 2021/12/27
 */
public interface Descriptor {

    enum Type {
        CLASS,
        ANNOTATION,
        METHOD,
        CONSTRUCTOR
    }

    Type type();

    static Class<?> descToClass(String desc, ClassLoader classLoader) throws ClassNotFoundException {
        if (desc.startsWith("L")) {
            String subs = desc.substring(1, desc.length() - 1);
            String pack = PackageUtils.replaceUrlToPackage(subs);
            return classLoader.loadClass(pack);
        } else if (desc.equals("V")) {
            return Void.class;
        } else if (desc.equals("Z")) {
            return boolean.class;
        } else if (desc.equals("B")) {
            return byte.class;
        } else if (desc.equals("C")) {
            return char.class;
        } else if (desc.equals("I")) {
            return int.class;
        } else if (desc.equals("J")) {
            return long.class;
        } else if (desc.equals("F")) {
            return float.class;
        } else if (desc.equals("D")) {
            return double.class;
        }
        return null;
    }

}
