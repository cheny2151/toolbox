package cn.cheny.toolbox.reflect.methodHolder;

import cn.cheny.toolbox.reflect.ReflectUtils;
import cn.cheny.toolbox.reflect.methodHolder.factory.DefaultMethodHolderFactory;
import cn.cheny.toolbox.reflect.methodHolder.factory.ReadWriteMethodHolderFactory;
import lombok.Data;
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

    @Test
    public void test2() {
        ReadWriteMethodHolderFactory factory = ReadWriteMethodHolderFactory.getInstance();
        ReadWriteMethodHolder methodHolder = factory.getMethodHolder(TestEntity.class);
        TestEntity testEntity = new TestEntity();
        methodHolder.write(testEntity, "l", 9);
        System.out.println(methodHolder.read(testEntity, "l"));
    }

    @Test
    public void test3() {
        TestEntity testEntity = new TestEntity();
        ReflectUtils.writeByMethodHolder(testEntity,9,TestEntity::getL);
        System.out.println(ReflectUtils.readByMethodHolder(testEntity, TestEntity::getL));
    }
    @Test
    public void test4() {
        TestEntity testEntity = new TestEntity();
        ReflectUtils.writeByMethodHolder(testEntity,9,"l");
        System.out.println(ReflectUtils.readByMethodHolder(testEntity, "l"));
    }

    @Data
    public static class TestEntity {
        private int i;
        private long l;
    }

}
