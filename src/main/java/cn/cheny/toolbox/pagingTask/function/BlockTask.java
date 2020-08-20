package cn.cheny.toolbox.pagingTask.function;

import java.util.List;

/**
 * 阻塞任务
 *
 * @author cheney
 * @date 2020-01-14
 */
@FunctionalInterface
public interface BlockTask<T> {

    void execute(List<T> data);

}
