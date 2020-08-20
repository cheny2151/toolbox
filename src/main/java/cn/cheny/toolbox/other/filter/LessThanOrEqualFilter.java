package cn.cheny.toolbox.other.filter;

public class LessThanOrEqualFilter extends Filter {

    private static final String lessThanOrEqualSymbol = "<=";

    public LessThanOrEqualFilter(String property, Object value) {
        super(lessThanOrEqualSymbol, property, value);
    }

}
