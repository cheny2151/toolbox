package cn.cheny.toolbox.asyncTask.poolmanager;

import java.io.Closeable;

/**
 * 资源管理器
 *
 * @author by chenyi
 * @date 2021/8/12
 */
public interface ResourceManager<R> extends Closeable {

    /**
     * 存放资源
     *
     * @param resource 资源
     * @return 是否成功
     */
    boolean put(R resource);

    /**
     * 弹出资源
     *
     * @return 资源
     */
    R poll();

    /**
     * 清除失效/过期资源
     */
    void clear();

    /**
     * 关闭所有资源
     */
    @Override
    void close();
}
