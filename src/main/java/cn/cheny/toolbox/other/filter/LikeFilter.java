package cn.cheny.toolbox.other.filter;

public class LikeFilter extends Filter {

    protected LikeFilter(String property, Object value, String connectionSymbol) {
        super(Filters.Operator.LIKE.getScript(), property, value, connectionSymbol);
    }

    @Override
    public String toString() {
        return this.getProperty() + " " + this.getSymbol() + " '%" + formatVal() + "%'";
    }
}
