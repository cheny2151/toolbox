package cn.cheny.toolbox.coordinator;

import cn.cheny.toolbox.coordinator.resource.Resource;

import java.io.Closeable;

/**
 * 资源协调器接口
 *
 * @author by chenyi
 * @date 2021/11/10
 */
public interface ResourceCoordinator<T extends Resource> extends Closeable {

    /**
     * 执行重平衡
     */
    void tryRebalanced();

    /**
     * 刷新当前实例资源
     */
    void refreshCurrentResources();
}
