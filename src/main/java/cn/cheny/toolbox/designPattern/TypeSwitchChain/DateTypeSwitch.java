package cn.cheny.toolbox.designPattern.TypeSwitchChain;

import org.apache.commons.lang3.time.DateUtils;

import java.text.ParseException;
import java.util.Date;

/**
 * 解析Date
 * String转Date
 */
public class DateTypeSwitch extends BaseTypeSwitch {

    private final static String[] FORMAT = {"yyyy-MM-dd", "yyyy/MM/dd"};

    public DateTypeSwitch() {
    }

    public DateTypeSwitch(BaseTypeSwitch next) {
        super(next);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T transform(Class<T> target, Object value) {
        if (Date.class.isAssignableFrom(target)) {
            if (value instanceof Date) {
                return (T) value;
            }
            try {
                return (T) DateUtils.parseDate(value.toString(), FORMAT);
            } catch (ParseException e) {
                e.printStackTrace();
                return null;
            }
        }
        return hasNext() ? getNext().transform(target, value) : null;
    }
}
