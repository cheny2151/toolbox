package cn.cheny.toolbox.pagingTask.function;

import cn.cheny.toolbox.other.page.Limit;

import java.util.List;

/**
 * @author cheney
 * @date 2020-01-14
 */
@FunctionalInterface
public interface FindDataFunction<T> {

    List<T> findData(Limit limit);

}
