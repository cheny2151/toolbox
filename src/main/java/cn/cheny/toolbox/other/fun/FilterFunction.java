package cn.cheny.toolbox.other.fun;

/**
 * 过滤函数
 *
 * @date 2021/3/8
 * @author by chenyi
 */
@FunctionalInterface
public interface FilterFunction<T> {

    boolean filter(T t);

}
