package cn.cheny.toolbox.entityCache.spring;

import cn.cheny.toolbox.entityCache.BufferType;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 实体缓存配置
 *
 * @author cheney
 * @date 2020-09-02
 */
@ConfigurationProperties(prefix = EntityBufferProperties.CACHE_PREFIX)
public class EntityBufferProperties {

    public static final String CACHE_PREFIX = "toolbox.entity-cache";

    /**
     * 是否启用
     */
    private boolean enable;

    /**
     * buffer类型
     */
    private BufferType type = BufferType.DEFAULT;

    /**
     * 数据库字段是否转下划线
     */
    private boolean underline;

    /**
     * 实体包目录
     */
    private String[] entityPaths;

    /**
     * 是否开启定时自动刷新缓存
     */
    private boolean autoRefresh;

    /**
     * 自动刷新间隔(分钟)
     */
    private Long period;

    public boolean isEnable() {
        return enable;
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
    }

    public BufferType getType() {
        return type;
    }

    public void setType(BufferType type) {
        this.type = type;
    }

    public boolean isUnderline() {
        return underline;
    }

    public void setUnderline(boolean underline) {
        this.underline = underline;
    }

    public String[] getEntityPaths() {
        return entityPaths;
    }

    public void setEntityPaths(String[] entityPaths) {
        this.entityPaths = entityPaths;
    }

    public boolean isAutoRefresh() {
        return autoRefresh;
    }

    public void setAutoRefresh(boolean autoRefresh) {
        this.autoRefresh = autoRefresh;
    }

    public Long getPeriod() {
        return period;
    }

    public void setPeriod(Long period) {
        this.period = period;
    }
}
