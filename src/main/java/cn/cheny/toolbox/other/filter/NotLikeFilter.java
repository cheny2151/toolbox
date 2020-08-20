package cn.cheny.toolbox.other.filter;

public class NotLikeFilter extends Filter {

    private static final String notLikeSymbol = "not like";

    public NotLikeFilter(String property, Object value) {
        super(notLikeSymbol, property, "%" + value + "%");
    }

}