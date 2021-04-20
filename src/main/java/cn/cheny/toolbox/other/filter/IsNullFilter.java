package cn.cheny.toolbox.other.filter;

public class IsNullFilter extends Filter {

    protected IsNullFilter(String property, String connectionSymbol) {
        super(Filters.Operator.IS_NULL.getScript(), property, null, connectionSymbol);
    }

    @Override
    public String toString() {
        return this.getProperty() + " " + this.getSymbol();
    }
}
