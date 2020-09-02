package cn.cheny.toolbox.entityCache.buffer.model;

import lombok.Getter;
import cn.cheny.toolbox.property.PropertyNameUtils;

import java.lang.reflect.Method;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 缓存信息
 *
 * @author cheney
 * @date 2020-09-01
 */
@Getter
public class BufferInfo<T> {

    private final Class<T> entityClass;
    private final String tableName;
    private final String[] fields;
    private final String sqlCondition;
    private final String[] idFields;
    private final Method[] idReadMethods;
    private boolean underline;

    public BufferInfo(Class<T> entityClass, String tableName, String[] idFields,
                      String[] fields, String sqlCondition, Method[] idReadMethods,
                      boolean underline) {
        this.entityClass = entityClass;
        this.tableName = tableName;
        this.idFields = idFields;
        this.fields = fields;
        this.sqlCondition = sqlCondition;
        this.idReadMethods = idReadMethods;
        this.underline = underline;
    }

    public String splitFields() {
        return Stream.of(fields)
                .map(f -> underline ? PropertyNameUtils.underline(f) : f)
                .collect(Collectors.joining(","));
    }

    public void setUnderline(boolean underline) {
        this.underline = underline;
    }
}
