package cn.cheny.toolbox.pagingTask;

/**
 * 极值字段值
 * 继承此接口，并复写{@link #getExtremumValue}返回分页用极值
 *
 * @author cheney
 * @date 2020-04-17
 */
public interface ExtremumField<V extends Comparable<V>> {

    V getExtremumValue();

}
