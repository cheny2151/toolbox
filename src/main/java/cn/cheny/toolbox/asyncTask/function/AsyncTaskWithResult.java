package cn.cheny.toolbox.asyncTask.function;

import java.util.List;

/**
 * 阻塞任务带返回值
 *
 * @author cheney
 * @date 2020-01-14
 */
public interface AsyncTaskWithResult<T, R> {

    List<R> execute(List<T> data);

}
