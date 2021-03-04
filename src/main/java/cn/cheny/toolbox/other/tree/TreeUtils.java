package cn.cheny.toolbox.other.tree;

import cn.cheny.toolbox.reflect.BeanUtils;
import cn.cheny.toolbox.reflect.ReflectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

import java.util.*;

import static cn.cheny.toolbox.other.tree.TreeType.CODE_SEPARATOR;


/**
 * 树形结构工具类
 *
 * @author cheney
 * @date 2019/4/23
 */
public class TreeUtils {

    private TreeUtils() {
    }

    public static <T extends TreeType<T>> List<T> toList(Collection<T> tree) {
        List<T> result = new ArrayList<>();
        toList(tree, null, result);
        return result;
    }

    /**
     * 将树形层级转化为标记,x,y,z,字符串的list
     *
     * @param tree   树结构数据
     * @param parent 该树的上层
     * @param result 结果集
     * @param <T>    T extends TreeType
     */
    private static <T extends TreeType<T>> void toList(Collection<T> tree, T parent, List<T> result) {
        for (T t : tree) {
            t.setCodeSequence(parent == null ?
                    initSequence(t.getCode()) : addCode(parent.getCodeSequence(), t.getCode()));
            Collection<T> children = t.getChildren();
            if (CollectionUtils.isEmpty(children)) {
                //到达最底层
                result.add(t);
            } else {
                toList(children, t, result);
            }
        }
    }

    public static <T extends TreeType<T>> List<T> toTree(Collection<T> list, Class<T> clazz) {

        if (CollectionUtils.isEmpty(list)) {
            return Collections.emptyList();
        }

        HashMap<String, T> roots = new HashMap<>();
        HashMap<String, T> nodes = new HashMap<>();
        for (T t : list) {
            String codeSequence = t.getCodeSequence();
            if (StringUtils.isEmpty(codeSequence)) {
                continue;
            }
            String[] codes = splitSequence(codeSequence);

            int length = codes.length;
            T parent = getOrInitByKey(initSequence(codes[0]), roots, clazz);
            if (length == 1) {
                BeanUtils.copyProperties(parent, t, "children", "code", "codeSequence", "parent");
                continue;
            }
            for (int i = 1; i < length; i++) {
                String key = addCode(parent.getCodeSequence(), codes[i]);
                T next = getOrInitByKey(key, nodes, clazz);
                //若为尾节点则复制所有数据
                if (i == length - 1) {
                    BeanUtils.copyProperties(next, t, "children", "code", "codeSequence", "parent");
                }
                Collection<T> children = parent.getChildren();
                if (children == null){
                    children = new ArrayList<>();
                    parent.setChildren(children);
                }
                if (!children.contains(next)) {
                    children.add(next);
                }
                //next
                parent = next;
            }
        }
        return new ArrayList<>(roots.values());
    }

    /**
     * 初始化序列
     */
    private static String initSequence(String code) {
        if (StringUtils.isEmpty(code)) {
            throw new IllegalArgumentException("树形结构编排code不能为空");
        }
        return CODE_SEPARATOR + code + CODE_SEPARATOR;
    }

    /**
     * 增加序列code
     */
    private static String addCode(String origin, String code) {
        if (StringUtils.isEmpty(code)) {
            throw new IllegalArgumentException("树形结构编排code不能为空");
        }
        return origin + code + CODE_SEPARATOR;
    }

    private static <T extends TreeType<T>> T getOrInitByKey(String key, Map<String, T> map, Class<T> clazz) {
        T treeType = map.get(key);
        if (treeType == null) {
            //map中不存在则初始化并put入map
            treeType = ReflectUtils.newObject(clazz, null, null);
            String[] codes = splitSequence(key);
            treeType.setCodeSequence(key);
            treeType.setCode(codes[codes.length - 1]);
            map.put(key, treeType);
        }
        return treeType;
    }


    private static String[] splitSequence(String sequence) {
        int startIndex = 0;
        if (sequence.startsWith(CODE_SEPARATOR)) {
            startIndex = 1;
        }
        return sequence.substring(startIndex).split(CODE_SEPARATOR);
    }

}
