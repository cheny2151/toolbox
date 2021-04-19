package cn.cheny.toolbox.sort;

import java.lang.reflect.Array;

/**
 * 堆基础类
 *
 * @Date 2021/4/16
 * @Created by chenyi
 */
public abstract class BaseHeap<T extends Comparable<T>> {

    protected HeapSort<T> heapSort;

    protected int size;

    public BaseHeap(T[] array) {
        this(array, array.length);
    }

    public BaseHeap(T[] array, int size) {
        this.size = size;
        T[] array0 = (T[]) Array.newInstance(array.getClass().getComponentType(), size);
        int len = Math.min(array.length, size);
        System.arraycopy(array, 0, array0, 0, len);
        heapSort = new HeapSort<>(array0);
        heapSort.sortDesc();
    }

    public T[] getResult() {
        return heapSort.getArray();
    }

    abstract boolean push(T newVal);

    abstract boolean pushAndSort(T newVal);
}
