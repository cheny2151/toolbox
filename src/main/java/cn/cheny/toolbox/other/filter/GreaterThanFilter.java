package cn.cheny.toolbox.other.filter;

public class GreaterThanFilter extends Filter {

    private static final String greaterThanSymbol = ">";

    public GreaterThanFilter(String property, Object value) {
        super(greaterThanSymbol, property, value);
    }

}
