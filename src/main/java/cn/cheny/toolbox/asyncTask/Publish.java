package cn.cheny.toolbox.asyncTask;

import java.util.List;

/**
 * 数据发布接口
 *
 * @author cheney
 * @date 2021-06-06
 */
public interface Publish<T> {

    void push(T t) throws InterruptedException;

    void pushMulti(List<T> t) throws InterruptedException;

}
