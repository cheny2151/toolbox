package cn.cheny.toolbox.spring;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * spring工具类自动配置
 *
 * @author cheney
 * @date 2020-08-25
 */
@Configuration
public class SpringToolAutoConfig {

    @Bean("toolbox:springUtils")
    @ConditionalOnMissingBean
    public SpringUtils springUtils() {
        return new SpringUtils();
    }

}
