package cn.cheny.toolbox.other.filter;

public class LessThanFilter extends Filter {

    private static final String lessThanSymbol = "<";

    public LessThanFilter(String property, Object value) {
        super(lessThanSymbol, property, value);
    }

}
