package cn.cheny.toolbox.other.filter;

public class GreaterThanOrEqualFilter extends Filter {

    private static final String greaterThanOrEqualSymbol = ">=";

    public GreaterThanOrEqualFilter(String property, Object value) {
        super(greaterThanOrEqualSymbol, property, value);
    }

}
