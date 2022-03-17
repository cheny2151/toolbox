package cn.cheny.toolbox.coordinator.resource;

import java.util.Collection;
import java.util.Set;

/**
 * 资源管理器接口
 *
 * @author by chenyi
 * @date 2021/11/10
 */
public interface ResourceManager<T extends Resource> {

    /**
     * 获取所有资源
     *
     * @return 资源集合
     */
    Set<T> getAllResources();

    /**
     *  根据资源标识创建资源
     *
     * @param flag 资源标识
     * @return 资源实例
     */
    T buildByFlag(String flag);

    /**
     * 资源变更勾子
     */
    void changeHook();

    /**
     * 资源key：
     * 用于唯一资源标识
     *
     * @return 资源key
     */
    String resourceKey();

    /**
     * 获取当前实例持有的资源实例
     *
     * @return 资源实例列表
     */
    Collection<T> getHoldResources();

    /**
     * 设置当前持有的资源实例
     *
     * @param resources 资源实例列表
     */
    void setHoldResources(Collection<T> resources);

}
