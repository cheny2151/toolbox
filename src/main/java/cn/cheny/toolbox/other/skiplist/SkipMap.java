package cn.cheny.toolbox.other.skiplist;

/**
 * 底层为跳表构建的Map结构
 *
 * @author by chenyi
 * @date 2021/7/30
 */
public class SkipMap<K extends Comparable<K>, V> extends SkipList<SkipEntry<K, V>> {

    public SkipMap() {
        super();
    }

    public SkipMap(int maxHigh) {
        super(maxHigh);
    }

    public void put(K key, V value) {
        if (key == null) {
            throw new IllegalArgumentException("key can not be null");
        }
        SkipEntry<K, V> keyEntry = new SkipEntry<>(key, value);
        super.add(keyEntry);
    }

    public V getByKey(K key) {
        if (key == null) {
            return null;
        }
        SkipEntry<K, V> keyEntry = new SkipEntry<>(key, null);
        Node<SkipEntry<K, V>> target = super.getNode(keyEntry);
        if (target != null) {
            SkipEntry<K, V> entry = target.getValue();
            return entry.getEntry();
        } else {
            return null;
        }
    }

    public V getTargetOrPreByKey(K key) {
        if (key == null) {
            return null;
        }
        SkipEntry<K, V> keyEntry = new SkipEntry<>(key, null);
        Node<SkipEntry<K, V>> target = super.getTargetOrPreNode(keyEntry);
        if (target != null) {
            SkipEntry<K, V> entry = target.getValue();
            return entry.getEntry();
        } else {
            return null;
        }
    }

    @Override
    protected boolean replaceWhileExists() {
        return true;
    }

}
