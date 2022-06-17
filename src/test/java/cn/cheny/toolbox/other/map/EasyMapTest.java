package cn.cheny.toolbox.other.map;

import cn.cheny.toolbox.other.page.Limit;
import cn.cheny.toolbox.reflect.TypeReference;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.junit.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author by chenyi
 * @Date 2021/5/28
 */
public class EasyMapTest {

    @Test
    public void test() {
        HashMap<String, Object> test = new HashMap<>();
        HashMap<String, Object> test2 = new HashMap<>();
        HashMap<String, Object> test3 = new HashMap<>();
        test.put("test", Collections.singletonMap("1", new Map[]{test2, test3}));
        test2.put("size", 4);
        test3.put("size", 5);
        EasyMap easyMap = new EasyMap(test);
        System.out.println(easyMap.getInteger("test.1[0].size"));
//        System.out.println(easyMap.getInteger("test[1].size"));
        System.out.println(easyMap.getObject("test", new TypeReference<Map<String, List<Limit>>>() {
        }));
        System.out.println(easyMap.getObject("test.1", List.class));
    }

    @Test
    public void test2() {
        HashMap<String, Object> test = new HashMap<>();
        HashMap<String, Object> test2 = new HashMap<>();
        HashMap<String, Object> test3 = new HashMap<>();
        test.put("test", Collections.singletonMap("1", new Map[]{test2, test3}));
        test2.put("size", 4);
        test2.put("t", 1);
        test3.put("size", 5);
        EasyMap easyMap = new EasyMap(test);
        LimitTestString object = easyMap.getObject("test.1[0]", new TypeReference<LimitTestString>() {
        });
        System.out.println(object.getT());
    }

    @EqualsAndHashCode(callSuper = true)
    @ToString
    @Data
    public static class LimitTest<T> extends Limit {
        private T t;
    }

    @EqualsAndHashCode(callSuper = true)
    @ToString
    @Data
    public static class LimitTestString extends LimitTest<String> {
    }

}
