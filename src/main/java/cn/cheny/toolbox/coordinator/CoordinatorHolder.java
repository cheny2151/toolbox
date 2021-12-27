package cn.cheny.toolbox.coordinator;

import com.joyy.shopline.badcase.handle.BadCase;
import lombok.extern.slf4j.Slf4j;

import java.io.Closeable;
import java.io.IOException;
import java.util.Map;

/**
 * 资源协调器持有类
 *
 * @author by chenyi
 * @date 2021/12/22
 */
@Slf4j
public class CoordinatorHolder implements Closeable {

    private final Map<String, ? extends ResourceCoordinator<?>> coordinatorMap;

    public CoordinatorHolder(Map<String, ? extends ResourceCoordinator<?>> coordinatorMap) {
        this.coordinatorMap = coordinatorMap;
    }

    public ResourceCoordinator<?> get(String resourceKey) {
        return coordinatorMap.get(resourceKey);
    }

    @Override
    @BadCase(code = "test")
    public void close() throws IOException {
        log.info("[Coordinator] 释放资源");
        for (ResourceCoordinator<?> resourceCoordinator : coordinatorMap.values()) {
            try {
                resourceCoordinator.close();
            } catch (Exception e) {
                // do nothing
            }
        }
    }

}
