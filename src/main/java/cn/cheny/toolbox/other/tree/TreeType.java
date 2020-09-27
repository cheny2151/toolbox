package cn.cheny.toolbox.other.tree;

import lombok.Data;

import java.util.Collection;
import java.util.Objects;

/**
 * @author cheney
 * @date 2019/4/23
 */
@Data
public class TreeType<T> {

    public final static String CODE_SEPARATOR = ",";

    private String code;

    private String codeSequence;

    private T parent;

    private Collection<T> children;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getCodeSequence() {
        return codeSequence;
    }

    public void setCodeSequence(String codeSequence) {
        this.codeSequence = codeSequence;
    }

    public T getParent() {
        return parent;
    }

    public void setParent(T parent) {
        this.parent = parent;
    }

    public Collection<T> getChildren() {
        return children;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TreeType<?> treeType = (TreeType<?>) o;

        return Objects.equals(codeSequence, treeType.codeSequence);
    }

    @Override
    public int hashCode() {
        return codeSequence != null ? codeSequence.hashCode() : 0;
    }

}
