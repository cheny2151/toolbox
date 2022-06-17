package cn.cheny.toolbox.scan.asm;

import lombok.ToString;

import java.util.Objects;

/**
 * 方法描述
 *
 * @author by chenyi
 * @date 2021/12/27
 */
@ToString
public class MethodDesc implements Descriptor {

    private final String name;
    private final String descriptor;

    public MethodDesc(String name, String descriptor) {
        this.name = name;
        this.descriptor = descriptor;
    }

    @Override
    public Type type() {
        return Type.METHOD;
    }

    public String getName() {
        return name;
    }

    public String getDescriptor() {
        return descriptor;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MethodDesc that = (MethodDesc) o;
        return Objects.equals(name, that.name) && Objects.equals(descriptor, that.descriptor);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, descriptor);
    }
}
