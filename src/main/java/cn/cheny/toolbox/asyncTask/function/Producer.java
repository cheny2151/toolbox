package cn.cheny.toolbox.asyncTask.function;

import cn.cheny.toolbox.asyncTask.Publish;

/**
 * 生产者接口
 *
 * @author cheney
 * @date 2021-06-06
 */
@FunctionalInterface
public interface Producer<T> {

    /**
     * 生产数
     */
    void produce(Publish<T> publish) throws InterruptedException;

}
