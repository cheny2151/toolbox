package cn.cheny.toolbox.pagingTask.function;

import java.util.List;

/**
 * 阻塞任务带返回值
 *
 * @author cheney
 * @date 2020-01-14
 */
public interface BlockTaskWithResult<T, R> {

    List<R> execute(List<T> data);

}
