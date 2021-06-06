package cn.cheny.toolbox.asyncTask.function;

import cn.cheny.toolbox.other.page.Limit;

/**
 * 业务任务函数式接口
 */
@FunctionalInterface
public interface PagingTaskWithResult<T> {
    T execute(Limit limit);
}
