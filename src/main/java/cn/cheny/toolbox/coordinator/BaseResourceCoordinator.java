package cn.cheny.toolbox.coordinator;

import cn.cheny.toolbox.coordinator.resource.Resource;
import cn.cheny.toolbox.coordinator.resource.ResourceManager;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;

import java.io.IOException;
import java.util.Collection;

/**
 * 资源协调器接口-base实现
 *
 * @author by chenyi
 * @date 2021/11/10
 */
@Slf4j
public abstract class BaseResourceCoordinator<T extends Resource> implements ResourceCoordinator<T> {

    private final ResourceManager<T> resourceManager;

    public BaseResourceCoordinator(ResourceManager<T> resourceManager) {
        this.resourceManager = resourceManager;
    }

    protected ResourceManager<T> getResourceManager() {
        return resourceManager;
    }

    /**
     * 设置当前实例收集到的新资源
     *
     * @param allocated 收集到的资源列表
     */
    protected void allocateNewResources(Collection<T> allocated) {
        Collection<T> origin = resourceManager.getHoldResources();
        if (origin == null || !CollectionUtils.isEqualCollection(allocated, origin)) {
            log.info("[Coordinator] 当前实例资源分配结果:{}", allocated);
            resourceManager.setHoldResources(allocated);
            resourceManager.changeHook();
        }
    }

    @Override
    public void close() throws IOException {

    }

}
