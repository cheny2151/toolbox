package cn.cheny.toolbox.spring;

import cn.cheny.toolbox.other.filter.Filter;

/**
 * 静态属性环境变量设置
 *
 * @author by chenyi
 * @date 2021/4/21
 */
public class StaticPropertySpringUtils implements SpringUtilsAware {

    @Override
    public void after() {
        Boolean underline = SpringUtils.getEnvironment().getProperty("toolbox.filter.underline", Boolean.class);
        if (underline != null) {
            Filter.setDefaultUseUnderline(underline);
        }
    }

}
