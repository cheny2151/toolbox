package cn.cheny.toolbox.reflect.methodHolder;

import cn.cheny.toolbox.reflect.TypeUtils;
import org.junit.Test;

import java.lang.reflect.Type;
import java.util.List;

/**
 * @author by chenyi
 * @date 2021/9/27
 */
public class TypeUtilsTest {

    @Test
    public void test2() {
        Type[] actualType = TypeUtils.getActualType(TestResultAndFlag2.class, Tuple3.class);
        for (Type type : actualType) {
            System.out.println(type.getClass());
        }
    }

    private static class TestResultAndFlag2 extends TestResultAndFlag<List<String>> {

    }

    private static class TestResultAndFlag<T> extends Tuple3<String, Integer, T> {

    }

    public static class Tuple3<T1, T2, T3> {

    }
}
