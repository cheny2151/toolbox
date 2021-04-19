package cn.cheny.toolbox.sort;

/**
 * 二叉堆
 *
 * @author cheney
 * @date 2020-03-06
 */
public class HeapSort<T extends Comparable<T>> {

    /**
     * 目标数组
     */
    private final T[] array;

    /**
     * 最大堆比较函数
     */
    private final CompareFunction<T> maxHeapCompare;

    /**
     * 最小堆比较函数
     */
    private final CompareFunction<T> minHeapCompare;

    public HeapSort(T[] array) {
        this.array = array;
        this.maxHeapCompare = (a, b) -> a.compareTo(b) > 0;
        this.minHeapCompare = (a, b) -> a.compareTo(b) < 0;
    }

    /**
     * 升序
     */
    public T[] sortAsc() {
        makeMaxHeap();
        for (int i = array.length - 1; i > 0; i--) {
            swap(0, i);
            makeHeap(0, i, maxHeapCompare);
        }
        return array;
    }

    /**
     * 降序
     */
    public T[] sortDesc() {
        makeMinHeap();
        for (int i = array.length - 1; i > 0; i--) {
            swap(0, i);
            makeHeap(0, i, minHeapCompare);
        }
        return array;
    }

    /**
     * 转化为最大堆
     */
    public T[] makeMaxHeap() {
        int firstIndex = firstNonLeafNodeIndex();
        for (int i = firstIndex; i >= 0; i--) {
            fixMaxHeap(i);
        }
        return array;
    }

    /**
     * 转化为最小堆
     */
    public T[] makeMinHeap() {
        int firstIndex = firstNonLeafNodeIndex();
        for (int i = firstIndex; i >= 0; i--) {
            fixMinHeap(i);
        }
        return array;
    }

    public T[] getArray() {
        return array;
    }

    public void fixMaxHeap(int index) {
        this.makeHeap(index, array.length, maxHeapCompare);
    }

    public void fixMinHeap(int index) {
        this.makeHeap(index, array.length, minHeapCompare);
    }

    /**
     * 堆化
     *
     * @param index      执行堆化的位置
     * @param len        执行堆化的长度
     * @param comparator 对比方法
     */
    private void makeHeap(int index, int len, CompareFunction<T> comparator) {
        int leftIdx, rightIdx, maxIdx;
        T rightVal, maxVal;
        maxIdx = leftIdx = leftNodeIndex(index);
        if (leftIdx >= len) {
            // 左节点大于目标len
            return;
        }
        maxVal = array[leftIdx];
        rightIdx = rightNodeIndex(index);
        rightVal = array[rightIdx];
        if (rightIdx < len && comparator.compare(rightVal, maxVal)) {
            // 右节点小于目标len并且值compare左节点为true
            maxIdx = rightIdx;
            maxVal = rightVal;
        }
        T curVal = array[index];
        if (comparator.compare(maxVal, curVal)) {
            swap(index, maxIdx);
            // 影响到子节点，继续堆化
            makeHeap(maxIdx, len, comparator);
        }
    }

    private int leftNodeIndex(int index) {
        return 2 * index + 1;
    }

    private T leftNodeVal(int index) {
        return array[2 * index + 1];
    }

    private int rightNodeIndex(int index) {
        return 2 * index + 2;
    }

    private T rightNodeVal(int index) {
        return array[2 * index + 2];
    }

    /**
     * 交换位置值
     *
     * @param i1 位置1
     * @param i2 位置2
     */
    private void swap(int i1, int i2) {
        T temp = array[i1];
        array[i1] = array[i2];
        array[i2] = temp;
    }

    /**
     * 第一个非叶子节点位置
     * 即为末尾叶子节点的父节点 = (末尾index-1)/2
     *
     * @return 数组index
     */
    private int firstNonLeafNodeIndex() {
        return (array.length - 2) >> 1;
    }

    private interface CompareFunction<T> {
        boolean compare(T a, T b);
    }

}
