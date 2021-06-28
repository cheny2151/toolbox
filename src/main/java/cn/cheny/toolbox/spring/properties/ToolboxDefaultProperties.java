package cn.cheny.toolbox.spring.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * toolbox默认配置
 *
 * @author by chenyi
 * @date 2021/6/28
 */
@ConfigurationProperties(prefix = ToolboxDefaultProperties.PRE)
public class ToolboxDefaultProperties {

    public static final String PRE = "toolbox.default";

    private final static String DEFAULT_SCANNER_PATH = "";

    private String scannerPath = DEFAULT_SCANNER_PATH;

    private Boolean filterUnderline;

    public String getScannerPath() {
        return scannerPath;
    }

    public void setScannerPath(String scannerPath) {
        this.scannerPath = scannerPath;
    }

    public Boolean getFilterUnderline() {
        return filterUnderline;
    }

    public void setFilterUnderline(Boolean filterUnderline) {
        this.filterUnderline = filterUnderline;
    }
}
