package cn.cheny.toolbox.other.filter;

public class NotEqualFilter extends Filter {

    private static final String noEqualSymbol = "!=";

    public NotEqualFilter(String property, Object value) {
        super(noEqualSymbol, property, value);
    }

}
