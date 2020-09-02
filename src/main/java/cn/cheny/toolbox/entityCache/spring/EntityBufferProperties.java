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

    private BufferType type = BufferType.DEFAULT;

    private boolean underline;

    private String path;

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

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
