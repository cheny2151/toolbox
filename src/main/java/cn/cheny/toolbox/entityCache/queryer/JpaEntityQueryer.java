package cn.cheny.toolbox.entityCache.queryer;

import cn.cheny.toolbox.entityCache.buffer.model.BufferInfo;

import java.util.List;

/**
 * 实体查询器--jpa实现
 *
 * @author cheney
 * @date 2020-09-01
 */
public class JpaEntityQueryer implements EntityQueryer {

    @Override
    public <T> List<T> query(BufferInfo<T> bufferInfo) {
        // javax.persistence.EntityManager
        return null;
    }
}
