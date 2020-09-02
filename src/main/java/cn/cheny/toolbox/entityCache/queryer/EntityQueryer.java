package cn.cheny.toolbox.entityCache.queryer;

import cn.cheny.toolbox.entityCache.buffer.model.BufferInfo;
import com.alibaba.fastjson.JSONObject;

import java.util.List;
import java.util.Map;

/**
 * 实体查询器
 *
 * @author cheney
 * @date 2020-08-31
 */
public interface EntityQueryer {

    <T> List<T> query(BufferInfo<T> bufferInfo);

    default <T> T mapToEntity(Map<String, Object> map, Class<T> entityClass) {
        // todo 用反射代替
        return new JSONObject(map).toJavaObject(entityClass);
    }

}
