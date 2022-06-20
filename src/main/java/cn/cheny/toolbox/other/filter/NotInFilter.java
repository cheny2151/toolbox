package cn.cheny.toolbox.other.filter;

public class NotInFilter extends Filter {

    protected NotInFilter(String property, Object value, String connectionSymbol) {
        super(Filters.Operator.NOT_IN.getScript(), property, value, connectionSymbol);
    }

    @Override
    public String toString() {
        return this.getProperty() + " " + this.getSymbol() + "(" + formatSqlVal() + ")";
    }
}
