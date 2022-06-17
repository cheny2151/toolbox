package cn.cheny.toolbox.other.filter;

public class InFilter extends Filter {

    protected InFilter(String property, Object value, String connectionSymbol) {
        super(Filters.Operator.IN.getScript(), property, value, connectionSymbol);
    }

    @Override
    public String toString() {
        return this.getProperty() + " " + this.getSymbol() + "(" + formatSqlVal() + ")";
    }
}
