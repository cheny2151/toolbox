package cn.cheny.toolbox.pagingTask.function;

import cn.cheny.toolbox.other.page.Limit;

/**
 * @author cheney
 * @date 2020-01-14
 */
@FunctionalInterface
public interface PagingTask {
    void execute(Limit limit);
}
