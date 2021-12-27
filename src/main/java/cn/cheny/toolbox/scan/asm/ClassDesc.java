package cn.cheny.toolbox.scan.asm;

/**
 * 类描述
 *
 * @author by chenyi
 * @date 2021/12/27
 */
public class ClassDesc implements Descriptor {

    private final String descriptor;

    public ClassDesc(String descriptor) {
        this.descriptor = descriptor;
    }

    @Override
    public Type type() {
        return Type.CLASS;
    }

    public Class<?> getRealClass() throws ClassNotFoundException {
        return getRealClass(Thread.currentThread().getContextClassLoader());
    }

    public Class<?> getRealClass(ClassLoader classLoader) throws ClassNotFoundException {
        return Descriptor.descToClass(descriptor, classLoader);
    }

}
