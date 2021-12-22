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

    Set<T> getAllResources();

    T buildByFlag(String flag);

    void changeHook();

    String resourceKey();

    Collection<T> getHoldResources();

    void setHoldResources(Collection<T> resources);

}
