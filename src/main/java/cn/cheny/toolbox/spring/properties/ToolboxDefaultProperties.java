package cn.cheny.toolbox.spring.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * toolbox默认配置
 *
 * @author by chenyi
 * @date 2021/6/28
 */
@ConfigurationProperties(prefix = ToolboxDefaultProperties.PRE)
@Data
public class ToolboxDefaultProperties {

    public static final String PRE = "toolbox.default";

    private final static String DEFAULT_SCANNER_PATH = "";

    private final static boolean DEFAULT_SCANNER_IN_ALL_JAR = false;

    private String scannerPath = DEFAULT_SCANNER_PATH;

    private boolean scannerInAllJar = DEFAULT_SCANNER_IN_ALL_JAR;

    private Boolean filterUnderline;

}
