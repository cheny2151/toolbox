package cn.cheny.toolbox.sort;

import com.alibaba.fastjson.JSON;

/**
 * 最小堆
 *
 * @author cheney
 * @date 2020-03-06
 */
public class MinHeap<T extends Comparable<T>> extends BaseHeap<T> {

    public MinHeap(T[] array) {
        this(array, array.length);
    }

    public MinHeap(T[] array, int size) {
        super(array, size);
        heapSort.sortAsc();
    }

    public boolean push(T val) {
        if (check(val)) {
            heapSort.getArray()[0] = val;
            heapSort.fixMaxHeap(0);
        }
        return false;
    }

    public boolean pushAndSort(T val) {
        if (check(val)) {
            heapSort.getArray()[0] = val;
            heapSort.sortAsc();
        }
        return false;
    }

    private boolean check(T val) {
        T[] sortArray = heapSort.getArray();
        T max = sortArray[0];
        return val.compareTo(max) > 0;
    }

    public static void main(String[] args) {
        Integer[] integers = {2, 7, 9, 21, 13, 17, 3, 10};
        MinHeap<Integer> heap = new MinHeap<>(integers, 7);
        System.out.println(JSON.toJSONString(heap.getResult()));
        heap.pushAndSort(-1);
        System.out.println(JSON.toJSONString(heap.getResult()));
        heap.pushAndSort(88);
        System.out.println(JSON.toJSONString(heap.getResult()));
    }

}
