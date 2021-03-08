package cn.cheny.toolbox.other.fun;

/**
 * 过滤函数
 *
 * @Date 2021/3/8
 * @Created by chenyi
 */
@FunctionalInterface
public interface FilterFunction<T> {

    boolean filter(T t);

}
