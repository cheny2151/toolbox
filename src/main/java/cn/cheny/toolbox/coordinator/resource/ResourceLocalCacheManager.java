package cn.cheny.toolbox.coordinator.resource;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * 本地缓存资源管理器
 *
 * @author by chenyi
 * @date 2021/11/10
 */
public abstract class ResourceLocalCacheManager<T extends Resource> implements ResourceManager<T> {

    private Set<T> resources;

    public ResourceLocalCacheManager() {
        this.resources = new HashSet<>();
    }

    @Override
    public Collection<T> getHoldResources() {
        return resources;
    }

    @Override
    public void setHoldResources(Collection<T> resources) {
        this.resources = new HashSet<>(resources);
    }
}
