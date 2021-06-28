package cn.cheny.toolbox.spring;

import cn.cheny.toolbox.spring.properties.ToolboxDefaultProperties;

import java.util.Arrays;
import java.util.List;

/**
 * spring utils 创建初始化后执行
 *
 * @author by chenyi
 * @date 2021/4/21
 */
public interface SpringUtilsAware {

    void after(ToolboxDefaultProperties toolboxDefaultProperties);

    static List<SpringUtilsAware> defaultAware() {
        return Arrays.asList(new StaticPropertySpringUtils());
    }

}
