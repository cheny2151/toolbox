package cn.cheny.toolbox.spring;

import cn.cheny.toolbox.other.filter.Filter;

/**
 * 静态属性环境变量设置
 *
 * @Date 2021/4/21
 * @author by chenyi
 */
public class StatusPropertySpringUtils implements SpringUtilsAware {

    @Override
    public void after() {
        Boolean underline = SpringUtils.getEnvironment().getProperty("toolbox.filter.underline", Boolean.class);
        if (underline != null) {
            Filter.setUseUnderline(underline);
        }
    }

}
