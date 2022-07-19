package cn.cheny.toolbox.redis;


import java.util.List;

/**
 * redis lua脚本返回类型
 *
 * @author by chenyi
 * @date 2022/7/1
 */
public enum ReturnType {
    BOOLEAN(Boolean.class),
    INTEGER(Long.class),
    MULTI(List.class),
    STATUS(null),
    VALUE(String.class);

    private final Class<?> type;

    ReturnType(Class<?> type) {
        this.type = type;
    }

    public Class<?> getType() {
        return type;
    }
}
