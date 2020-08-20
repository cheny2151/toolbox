package cn.cheny.toolbox.reflect.methodHolder;

import cn.cheny.toolbox.reflect.ReflectUtils;
import cn.cheny.toolbox.reflect.methodHolder.factory.DefaultMethodHolderFactory;
import org.junit.Test;

/**
 * 测试类
 *
 * @author cheney
 * @date 2019-12-06
 */
public class TestMain {

    @Test
    public void test() {
        DefaultMethodHolderFactory holderFactory = new DefaultMethodHolderFactory();
        MethodHolder methodHolder = holderFactory.getMethodHolder(ReflectUtils.class, StaticMethodHolder.class);
        System.out.println(methodHolder.invoke("field", null, DefaultMethodHolderFactory.class, "methodHolderCache"));
    }

}
