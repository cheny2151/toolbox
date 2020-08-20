package cn.cheny.toolbox.designPattern.TypeSwitchChain;

import java.math.BigDecimal;

/**
 * String类型转换器
 */
public class StringSwitch extends BaseTypeSwitch {

    public StringSwitch() {
    }

    public StringSwitch(BaseTypeSwitch next) {
        super(next);
    }

    /**
     * 统一在转换链前做value的非空判断
     */
    @SuppressWarnings("unchecked")
    @Override
    public <T> T transform(Class<T> target, Object value) {
        if (String.class.isAssignableFrom(target)) {
            //对double做特殊处理
            if (value instanceof Double || double.class.equals(value.getClass())) {
                return (T) new BigDecimal((Double) value).toString();
            }
            return (T) value.toString();
        }
        return hasNext() ? getNext().transform(target, value) : null;
    }

}
