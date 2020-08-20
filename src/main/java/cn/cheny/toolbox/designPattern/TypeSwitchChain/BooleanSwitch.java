package cn.cheny.toolbox.designPattern.TypeSwitchChain;

public class BooleanSwitch extends BaseTypeSwitch {

    private final static String[] TRUE_VALUE_KEY = {"true", "是"};

    public BooleanSwitch() {
    }

    public BooleanSwitch(BaseTypeSwitch next) {
        super(next);
    }

    @Override
    public <T> T transform(Class<T> target, Object value) {
        if (Boolean.class.isAssignableFrom(target) || boolean.class.isAssignableFrom(target)) {
            return (T) isTrueValue(value.toString());
        }
        return hasNext() ? getNext().transform(target, value) : null;
    }

    private Boolean isTrueValue(String s) {
        for (String empty : TRUE_VALUE_KEY) {
            if (empty.equalsIgnoreCase(s)) {
                return true;
            }
        }
        return false;
    }

}
