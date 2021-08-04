package cn.cheny.toolbox.other.skiplist;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.Random;

/**
 * 数据结构：跳表
 *
 * @author cheney
 */
public class SkipList<T extends Comparable<T>> {

    /**
     * 默认层数
     */
    protected static final int DEFAULT_MAX_HIGH = 32;

    /**
     * 第0层头节点
     */
    private Node<T> head;

    /**
     * 第0层尾节点
     */
    private Node<T> tail;

    private int size;

    private int high;

    private final int maxHigh;

    protected static class Node<T extends Comparable<T>> {

        private final int high;

        private T value;

        private Node<T> pre;

        private Node<T>[] next;

        @SuppressWarnings("unchecked")
        Node(T value, int high) {
            this.value = value;
            this.high = high;
            this.next = new Node[high];
        }

        public T getValue() {
            return value;
        }

        public void setValue(T value) {
            this.value = value;
        }

        public Node<T> getPre() {
            return pre;
        }

        public void setPre(Node<T> pre) {
            this.pre = pre;
        }

        public Node<T>[] getNext() {
            return next;
        }

        public void setNext(Node<T>[] next) {
            this.next = next;
        }

        public void setNext(Node<T> next, int high) {
            this.next[high] = next;
        }

        public int getHigh() {
            return high;
        }

        public void clear() {
            this.value = null;
            this.next = null;
            this.pre = null;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;

            if (o == null || getClass() != o.getClass()) return false;

            Node<?> node = (Node<?>) o;

            return new EqualsBuilder().append(high, node.high).append(value, node.value).isEquals();
        }

        @Override
        public int hashCode() {
            return new HashCodeBuilder(17, 37).append(high).append(value).toHashCode();
        }

        @Override
        public String toString() {
            return "Node{" +
                    "value=" + value +
                    '}';
        }
    }

    public SkipList() {
        this(DEFAULT_MAX_HIGH);
    }

    public SkipList(int maxHigh) {
        if (maxHigh <= 1) {
            throw new IllegalArgumentException("illegal arg max high,must gt 1");
        }
        this.maxHigh = maxHigh;
        init();
    }

    private void init() {
        this.size = this.high = 0;
        // 初始化head
        this.head = new Node<>(null, this.maxHigh);
    }

    /**
     * 往跳表新增值
     *
     * @param value 新增值
     */
    public void add(T value) {
        if (value == null) {
            throw new IllegalArgumentException("value can not be null");
        }
        int level = randomLevel();
        Node<T> n = new Node<>(value, level);
        Node<T> cur = this.head;
        for (int i = level - 1; i >= 0; i--) {
            Node<T> next;
            while ((next = cur.getNext()[i]) != null &&
                    next.value.compareTo(value) <= 0) {
                cur = next;
            }
            if (cur.value != null && cur.value.compareTo(value) == 0
                    && replaceWhileExists()) {
                cur.value = value;
                return;
            }
            // 新节点插入到update之后，originNext之前
            Node<T> originNext = cur.getNext()[i];
            n.setNext(originNext, i);
            cur.setNext(n, i);
        }
        // 更新pre,tail
        n.setPre(cur);
        Node<T> next = n.getNext()[0];
        if (next == null) {
            this.tail = n;
        } else {
            next.setPre(n);
        }
        // 更新high,size
        if (level > this.high) {
            this.high = level;
        }
        size++;
    }

    /**
     * 通过调表查询与入参值相等的数据
     *
     * @param target 查找的数据
     * @return 是否存在
     */
    public T get(T target) {
        Node<T> node = getNode(target);
        return node == null ? null : node.getValue();
    }

    /**
     * 通过调表查询与入参值相等的数据，不存在时返回其上一个数据
     *
     * @param target 查找的数据
     * @return 是否存在
     */
    public T getTargetOrPre(T target) {
        Node<T> node = getTargetOrPreNode(target);
        return node == null ? null : node.getValue();
    }

    /**
     * 通过调表查询是否存在数据
     *
     * @param target 查找的数据
     * @return 是否存在
     */
    public boolean contains(T target) {
        return getNode(target) != null;
    }

    /**
     * 删除所有与目标数据相同的节点
     *
     * @param target 删除的数据值
     * @return 是否执行删除
     */
    public boolean remove(T target) {
        if (target == null) {
            throw new IllegalArgumentException("value can not be null");
        }
        boolean flag = false;
        int curLevel = high - 1;
        Node<T> cur = head;
        while (curLevel >= 0) {
            Node<T> next;
            while ((next = cur.getNext()[curLevel]) != null) {
                int compare = next.value.compareTo(target);
                if (compare < 0) {
                    cur = next;
                } else if (compare == 0) {
                    // remove next
                    Node<T> delNext = next.getNext()[curLevel];
                    if (delNext != null && delNext.pre == next) {
                        delNext.setPre(cur);
                    }
                    cur.setNext(delNext, curLevel);
                    flag = true;
                } else {
                    break;
                }
            }
            curLevel--;
        }
        return flag;
    }

    /**
     * 查找与目标数据相同的节点，然后找出其偏移量为offset的数据
     *
     * @param target 删除的数据值
     * @return 是否执行删除
     */
    public T offset(T target, int offset) {
        Node<T> targetOrPreNode = getTargetOrPreNode(target);
        if (targetOrPreNode == null) {
            return null;
        }
        int curOffset = offset;
        Node<T> offsetNode = targetOrPreNode;
        while (curOffset != 0 && offsetNode != null) {
            if (curOffset > 0) {
                offsetNode = offsetNode.getNext()[0];
                curOffset--;
            } else {
                if (curOffset != -1) {
                    offsetNode = offsetNode.pre;
                }
                curOffset++;
            }
        }
        return offsetNode == null ? null : offsetNode.value;
    }

    /**
     * 返回跳表长度
     *
     * @return 跳表长度
     */
    public int size() {
        return size;
    }

    /**
     * 通过跳表查询数据
     *
     * @param target 查找的数据
     * @return 数据节点
     */
    protected Node<T> getNode(T target) {
        Node<T> targetOrPreNode = getTargetOrPreNode(target);
        if (targetOrPreNode == null || targetOrPreNode.value.compareTo(target) != 0) {
            return null;
        }
        return targetOrPreNode;
    }

    /**
     * 通过跳表查询数据,不存在时返回其上一个节点
     *
     * @param target 查找的数据
     * @return 数据节点
     */
    protected Node<T> getTargetOrPreNode(T target) {
        if (target == null) {
            return null;
        }
        int curLevel = high - 1;
        Node<T> cur = head;
        while (curLevel >= 0) {
            Node<T> next;
            while ((next = cur.getNext()[curLevel]) != null) {
                int compare = next.value.compareTo(target);
                if (compare < 0) {
                    cur = next;
                } else if (compare == 0) {
                    return next;
                } else {
                    break;
                }
            }
            curLevel--;
        }
        return cur;
    }

    /**
     * 存在时是否替换
     *
     * @return 默认返回false
     */
    protected boolean replaceWhileExists() {
        return false;
    }

    /**
     * 抛硬币，每抛一次正面增加一层
     * 最大MAX_HIGH层
     *
     * @return 层级
     */
    private int randomLevel() {
        Random random = new Random();
        int level = 1;
        int maxHigh = this.maxHigh;
        for (int i = 1; i < maxHigh; i++) {
            if ((random.nextInt(2) & 1) == 1) {
                level++;
            }
        }
        return level;
    }

    @Override
    public String toString() {
        StringBuilder print = new StringBuilder();
        for (int i = high - 1; i >= 0; i--) {
            if (i == 0) {
                print.append("head--->");
            } else {
                print.append("\t\t");
            }
            Node<T> cur = this.head;
            int l = print.length();
            while ((cur = cur.getNext()[i]) != null) {
                print.append(cur.value).append("-----");
            }
            if (print.length() > l) {
                print.setLength(print.length() - 5);
            }
            if (i == 0) {
                print.append("--->tail");
            } else {
                print.append("\r\n");
            }
        }
        return print.toString();
    }

}
