package cn.cheny.toolbox.other.filter;

import cn.cheny.toolbox.property.PropertyNameUtils;
import org.apache.commons.lang3.StringUtils;

public class Filter {

    // 转为下划线
    private final static boolean USE_UNDERLINE = true;

    // 过滤符号
    private String symbol;

    // 过滤值
    private Object value;

    // 过滤属性
    private String property;

    public Filter() {
    }

    public Filter(String symbol) {
        this.symbol = symbol;
    }

    public Filter(String symbol, String property, Object value) {
        this.symbol = symbol;
        this.property = property;
        this.value = value;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public String getProperty() {
        if (USE_UNDERLINE && StringUtils.isNotEmpty(property)) {
            property = PropertyNameUtils.underline(property);
        }
        return property;
    }

    public void setProperty(String property) {
        this.property = property;
    }

}
