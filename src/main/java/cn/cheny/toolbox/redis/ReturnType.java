package cn.cheny.toolbox.redis;


/**
 * redis lua脚本返回类型
 *
 * @author by chenyi
 * @date 2022/7/1
 */
public enum ReturnType {
    BOOLEAN,
    INTEGER,
    MULTI,
    STATUS,
    VALUE;
}
