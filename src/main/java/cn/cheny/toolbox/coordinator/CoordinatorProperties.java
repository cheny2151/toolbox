package cn.cheny.toolbox.coordinator;

import org.apache.commons.lang3.StringUtils;
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
    public String namespace;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public String getKey() {
        String namespace = this.namespace;
        if (StringUtils.isEmpty(namespace)) {
            return id;
        } else {
            return id + ":" + namespace;
        }
    }
}
