package cn.cheny.toolbox.asyncTask.function;

import cn.cheny.toolbox.asyncTask.parallel.FutureResultHolder;

/**
 * 三份并行任务消费接口
 *
 * @author by chenyi
 * @date 2021/7/26
 */
@FunctionalInterface
public interface ThreeTaskConsume<ONE, TWO, THREE, R> {

    R consume(FutureResultHolder<ONE> one, FutureResultHolder<TWO> two, FutureResultHolder<THREE> three);

}
