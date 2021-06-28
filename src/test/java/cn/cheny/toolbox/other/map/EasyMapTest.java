package cn.cheny.toolbox.other.map;

import cn.cheny.toolbox.other.page.Limit;
import cn.cheny.toolbox.reflect.TypeReference;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author by chenyi
 * @Date 2021/5/28
 */
public class EasyMapTest {

    public static void main(String[] args) {
        HashMap<String, Object> test = new HashMap<>();
        HashMap<String, Object> test2 = new HashMap<>();
        HashMap<String, Object> test3 = new HashMap<>();
        test.put("test", new Map[]{test2, test3});
        test2.put("size", 4);
        test3.put("size", 5);
        EasyMap easyMap = new EasyMap(test);
        System.out.println(easyMap.getInteger("test[0].size"));
        System.out.println(easyMap.getInteger("test[1].size"));
        System.out.println(easyMap.getObject("test", new TypeReference<List<Limit>>() {
        }));
        System.out.println(easyMap.getObject("test[0]", Limit.class));
    }

}
