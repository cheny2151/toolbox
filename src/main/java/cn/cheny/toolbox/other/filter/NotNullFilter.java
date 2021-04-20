package cn.cheny.toolbox.other.filter;

public class NotNullFilter extends Filter {

    protected NotNullFilter(String property, String connectionSymbol) {
        super(Filters.Operator.IS_NOT_NULL.getScript(), property, null, connectionSymbol);
    }

    @Override
    public String toString() {
        return this.getProperty() + " " + this.getSymbol();
    }
}
