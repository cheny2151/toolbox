package cn.cheny.toolbox.redis;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * toolbox redis相关配置
 *
 * @author cheney
 * @date 2022-06-17
 */
@ConfigurationProperties(prefix = ToolboxRedisProperties.PRE)
@Data
public class ToolboxRedisProperties {
    public final static String PRE = "toolbox.redis";
    public static final String DEFAULT_REENTRANT_LOCK_PRE_PATH = "REENTRANT_LOCK";
    public static final String DEFAULT_SECOND_LEVEL_LOCK_PRE_PATH = "SECOND_LEVEL_LOCK";
    public static final String DEFAULT_MULTI_LOCK_PRE_PATH = "MULTI_LOCK";

    private boolean enable = true;

    private String reentryLockPrePath = DEFAULT_REENTRANT_LOCK_PRE_PATH;
    private String secondLevelLockPrePath = DEFAULT_SECOND_LEVEL_LOCK_PRE_PATH;
    private String multiLockPrePath = DEFAULT_MULTI_LOCK_PRE_PATH;
}
