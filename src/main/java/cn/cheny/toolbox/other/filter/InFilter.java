package cn.cheny.toolbox.other.filter;

import java.util.Collection;

public class InFilter extends Filter {

    private static final String inSymbol = "in";

    public <T> InFilter(String property, Collection<T> value) {
        super(inSymbol, property, value);
    }

    public <T> InFilter(String property, T[] value) {
        super(inSymbol, property, value);
    }

}
