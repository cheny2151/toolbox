package cn.cheny.toolbox.designPattern.TypeSwitchChain;

import java.math.BigDecimal;

/**
 * Integer类型转换器
 */
public class NumberSwitch extends BaseTypeSwitch {

    public NumberSwitch() {
    }

    public NumberSwitch(BaseTypeSwitch next) {
        super(next);
    }

    /**
     * 统一在转换链前做value的非空判断
     */
    @SuppressWarnings("unchecked")
    @Override
    public <T> T transform(Class<T> target, Object value) {
        if (Integer.class == target) {
            String transform = value.toString();
            if (transform.contains(".")) {
                transform = transform.substring(0, transform.indexOf("."));
            }
            return (T) Integer.valueOf(transform);
        }
        if (Double.class == target) {
            return (T) Double.valueOf(value.toString());
        }
        if (BigDecimal.class == target) {
            return (T) new BigDecimal(value.toString());
        }
        return hasNext() ? getNext().transform(target, value) : null;
    }

}
