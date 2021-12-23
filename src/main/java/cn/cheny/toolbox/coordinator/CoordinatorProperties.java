package cn.cheny.toolbox.coordinator;

import org.springframework.boot.context.properties.ConfigurationProperties;

import static cn.cheny.toolbox.coordinator.CoordinatorProperties.PRE;

/**
 * 资源协调器配置
 *
 * @author by chenyi
 * @date 2021/12/23
 */
@ConfigurationProperties(PRE)
public class CoordinatorProperties {

    public final static String PRE = "toolbox.coordinator";

    public String id;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
