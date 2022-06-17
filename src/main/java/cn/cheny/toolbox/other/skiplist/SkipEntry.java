package cn.cheny.toolbox.other.skiplist;

import lombok.Data;

/**
 * 跳表Map结构entry
 *
 * @author by chenyi
 * @date 2021/7/30
 */
@Data
public class SkipEntry<T extends Comparable<T>, E> implements Comparable<SkipEntry<T, E>> {

    private T key;

    private E entry;

    public SkipEntry() {
    }

    public SkipEntry(T key, E entry) {
        this.key = key;
        this.entry = entry;
    }

    @Override
    public int compareTo(SkipEntry<T, E> other) {
        return key.compareTo(other.key);
    }
}
