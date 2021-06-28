package cn.cheny.toolbox.spring;

import cn.cheny.toolbox.other.filter.Filter;
import cn.cheny.toolbox.spring.properties.ToolboxDefaultProperties;

/**
 * 静态属性环境变量设置
 *
 * @author by chenyi
 * @date 2021/4/21
 */
public class StaticPropertySpringUtils implements SpringUtilsAware {

    @Override
    public void after(ToolboxDefaultProperties toolboxDefaultProperties) {
        Boolean underline = toolboxDefaultProperties.getFilterUnderline();
        if (underline != null) {
            Filter.setDefaultUseUnderline(underline);
        }
    }

}
