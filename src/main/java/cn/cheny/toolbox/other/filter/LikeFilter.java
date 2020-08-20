package cn.cheny.toolbox.other.filter;

public class LikeFilter extends Filter {

    private static final String likeSymbol = "like";

    public LikeFilter(String property, Object value) {
        super(likeSymbol, property, "%" + value + "%");
    }

}
