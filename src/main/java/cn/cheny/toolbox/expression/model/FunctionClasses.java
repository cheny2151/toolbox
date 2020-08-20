package cn.cheny.toolbox.expression.model;

import java.util.Collection;
import java.util.Comparator;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 函数类集合实体
 *
 * @author cheney
 * @date 2020-04-10
 */
public class FunctionClasses extends TreeSet<FunctionClasses.FunctionClass> {

    /**
     * 目前最大排序，原子性
     */
    private AtomicInteger maxOrder = new AtomicInteger(1);

    public FunctionClasses() {
        super(Comparator.comparing(FunctionClass::getOrder));
    }

    public FunctionClasses(Collection<Class<?>> classes) {
        super(Comparator.comparing(FunctionClass::getOrder));
        for (Class<?> cls : classes) {
            new FunctionClass(cls, maxOrder.getAndIncrement());
        }
    }

    /**
     * 添加函数类
     *
     * @param clazz 类
     * @return 添加结果
     */
    public boolean addFunction(Class<?> clazz) {
        return add(new FunctionClass(clazz, maxOrder.getAndIncrement()));
    }

    /**
     * 类+排序实体
     */
    public static class FunctionClass {
        /**
         * 函数类
         */
        private Class<?> funcClass;
        /**
         * 排序
         */
        private int order;

        public FunctionClass(Class<?> funcClass, int order) {
            this.funcClass = funcClass;
            this.order = order;
        }

        public Class<?> getFuncClass() {
            return funcClass;
        }

        public int getOrder() {
            return order;
        }
    }

}
