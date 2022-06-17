package cn.cheny.toolbox.reflect;

import java.io.Serializable;
import java.util.function.Function;

/**
 * 字段获取函数
 *
 * @author by chenyi
 * @date 2022/6/17
 */
@FunctionalInterface
public interface FieldGetter<T> extends Function<T, Object>, Serializable {

}
