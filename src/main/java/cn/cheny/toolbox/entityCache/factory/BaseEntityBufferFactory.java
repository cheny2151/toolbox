package cn.cheny.toolbox.entityCache.factory;

/**
 * 基础Entity Buffer工厂，提供判断数据库字段是否驼峰转下划线的字段
 *
 * @author cheney
 * @date 2020-08-31
 */
public abstract class BaseEntityBufferFactory implements EntityBufferFactory {

    private boolean underline;

    public BaseEntityBufferFactory(boolean underline) {
        this.underline = underline;
    }

    public boolean isUnderline() {
        return underline;
    }

    public void setUnderline(boolean underline) {
        this.underline = underline;
    }
}
