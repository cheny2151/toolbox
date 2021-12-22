package cn.cheny.toolbox.coordinator;

import java.io.Closeable;
import java.io.IOException;
import java.util.Map;

/**
 * 资源协调器持有类
 *
 * @author by chenyi
 * @date 2021/12/22
 */
public class CoordinatorHolder implements Closeable {

    private final Map<String, ? extends ResourceCoordinator<?>> coordinatorMap;

    public CoordinatorHolder(Map<String, ? extends ResourceCoordinator<?>> coordinatorMap) {
        this.coordinatorMap = coordinatorMap;
    }

    @Override
    public void close() throws IOException {
        for (ResourceCoordinator<?> resourceCoordinator : coordinatorMap.values()) {
            try {
                resourceCoordinator.close();
            } catch (Exception e) {
                // do nothing
            }
        }
    }

}
