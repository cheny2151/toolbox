package cn.cheny.toolbox.spring;

import cn.cheny.toolbox.spring.properties.ToolboxDefaultProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * spring工具类自动配置
 *
 * @author cheney
 * @date 2020-08-25
 */
@Configuration
@EnableConfigurationProperties({ToolboxDefaultProperties.class})
public class SpringToolAutoConfig {

    @Bean("toolbox:springUtils")
    @ConditionalOnMissingBean(name = "toolbox:springUtils")
    public SpringUtils springUtils(ToolboxDefaultProperties toolboxDefaultProperties) {
        return new SpringUtils(toolboxDefaultProperties);
    }

}
