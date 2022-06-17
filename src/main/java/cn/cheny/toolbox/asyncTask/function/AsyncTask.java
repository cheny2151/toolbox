package cn.cheny.toolbox.asyncTask.function;

import java.util.List;

/**
 * 阻塞任务
 *
 * @author cheney
 * @date 2020-01-14
 */
@FunctionalInterface
public interface AsyncTask<T> {

    void execute(List<T> data);

}
