package cn.cheny.toolbox.other.filter;

public class EqualFilter extends Filter {

    private static final String equalSymbol = "=";

    public EqualFilter(String property, Object value) {
        super(equalSymbol, property, value);
    }

}
