package cn.cheny.toolbox.expression.test;

import java.math.BigDecimal;

/**
 * @author cheney
 * @date 2019-12-10
 */
public class TestFunction {

    public static String noArg() {
        return "yyyy-MM-dd";
    }

    public static String getString(Object test1, Object... test) {
        System.out.println(test1);
        return "yyyy-MM-dd";
    }

    public static BigDecimal abs(Object value) {
        return new BigDecimal(value.toString()).abs();
    }

    public static BigDecimal to_number(Object object) {
        if (object == null) {
            throw new NullPointerException();
        }
        return new BigDecimal(object.toString());
    }
}
