package cn.cheny.toolbox.asyncTask.function;

import cn.cheny.toolbox.asyncTask.parallel.FutureResultHolder;

/**
 * 一份并行任务消费接口
 *
 * @author by chenyi
 * @date 2021/7/26
 */
@FunctionalInterface
public interface OneTaskConsume<ONE,R> {

    R consume(FutureResultHolder<ONE> one);

}
