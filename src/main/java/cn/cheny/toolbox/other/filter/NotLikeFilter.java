package cn.cheny.toolbox.other.filter;

public class NotLikeFilter extends Filter {

    protected NotLikeFilter(String property, Object value, String connectionSymbol) {
        super(Filters.Operator.NOT_LIKE.getScript(), property, value, connectionSymbol);
    }

    @Override
    public String toString() {
        return this.getProperty() + " " + this.getSymbol() + " '%" + formatVal() + "%'";
    }
}
