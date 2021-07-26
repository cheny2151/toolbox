package cn.cheny.toolbox.asyncTask.function;

/**
 * 并行任务
 *
 * @author by chenyi
 * @date 2021/7/26
 */
@FunctionalInterface
public interface Task<R> {

    R doTask();

}
