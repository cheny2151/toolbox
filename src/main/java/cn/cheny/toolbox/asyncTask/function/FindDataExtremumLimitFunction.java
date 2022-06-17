package cn.cheny.toolbox.asyncTask.function;

import cn.cheny.toolbox.other.page.ExtremumLimit;

import java.util.List;

/**
 * @author cheney
 * @date 2020-01-14
 */
@FunctionalInterface
public interface FindDataExtremumLimitFunction<T> {

    List<T> findData(ExtremumLimit limit);

}
