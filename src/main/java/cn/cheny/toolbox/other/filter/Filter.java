package cn.cheny.toolbox.other.filter;

import cn.cheny.toolbox.property.PropertyNameUtils;
import org.apache.commons.lang3.StringUtils;

import java.text.SimpleDateFormat;
import java.time.chrono.ChronoLocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Filter {

    // 转为下划线
    private static boolean USE_UNDERLINE = true;

    // 过滤符号
    private String symbol;

    // 过滤值
    private Object value;

    // 过滤属性
    private String property;

    // 连接符号
    private String connectionSymbol;

    // next
    private Filter next;

    public Filter() {
    }

    protected Filter(String symbol, String property, Object value, String connectionSymbol) {
        this.symbol = symbol;
        this.property = property;
        this.value = value;
        this.connectionSymbol = connectionSymbol;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public String getProperty() {
        if (USE_UNDERLINE && StringUtils.isNotEmpty(property)) {
            property = PropertyNameUtils.underline(property);
        }
        return property;
    }

    public void setProperty(String property) {
        this.property = property;
    }

    public String getConnectionSymbol() {
        return connectionSymbol == null ? Filters.Operator.AND.getScript() : connectionSymbol;
    }

    public void setConnectionSymbol(String connectionSymbol) {
        this.connectionSymbol = connectionSymbol;
    }

    public Filter getNext() {
        return next;
    }

    public void setNext(Filter next) {
        this.next = next;
    }

    public Filter andFilter(String symbol, String property, Object value) {
        Filter next = new Filter(symbol, property, value, Filters.Operator.AND.getScript());
        this.setNextToLastNode(next);
        return this;
    }

    public Filter orFilter(String symbol, String property, Object value) {
        Filter next = new Filter(symbol, property, value, Filters.Operator.OR.getScript());
        this.setNextToLastNode(next);
        return this;
    }

    private void setNextToLastNode(Filter next) {
        Filter node = this;
        while (node.next != null) {
            node = node.next;
        }
        node.setNext(next);
    }

    public Filter andEq(String property, Object value) {
        return this.andFilter(Filters.Operator.EQ.getScript(), property, value);
    }

    public Filter orEq(String property, Object value) {
        return this.orFilter(Filters.Operator.EQ.getScript(), property, value);
    }

    public Filter andNotEq(String property, Object value) {
        return this.andFilter(Filters.Operator.NE.getScript(), property, value);
    }

    public Filter orNotEq(String property, Object value) {
        return this.orFilter(Filters.Operator.NE.getScript(), property, value);
    }

    public Filter andGt(String property, Object value) {
        return this.andFilter(Filters.Operator.GT.getScript(), property, value);
    }

    public Filter orGt(String property, Object value) {
        return this.orFilter(Filters.Operator.GT.getScript(), property, value);
    }

    public Filter andGte(String property, Object value) {
        return this.andFilter(Filters.Operator.GTE.getScript(), property, value);
    }

    public Filter orGte(String property, Object value) {
        return this.orFilter(Filters.Operator.GTE.getScript(), property, value);
    }

    public Filter andLt(String property, Object value) {
        return this.andFilter(Filters.Operator.LT.getScript(), property, value);
    }

    public Filter orLt(String property, Object value) {
        return this.orFilter(Filters.Operator.LT.getScript(), property, value);
    }

    public Filter andLte(String property, Object value) {
        return this.andFilter(Filters.Operator.LTE.getScript(), property, value);
    }

    public Filter orLte(String property, Object value) {
        return this.orFilter(Filters.Operator.LTE.getScript(), property, value);
    }

    public <T> Filter andIn(String property, Collection<T> value) {
        Filter next = new InFilter(property, value, Filters.Operator.AND.getScript());
        this.setNextToLastNode(next);
        return this;
    }

    public <T> Filter orIn(String property, Collection<T> value) {
        Filter next = new InFilter(property, value, Filters.Operator.OR.getScript());
        this.setNextToLastNode(next);
        return this;
    }

    public <T> Filter andIn(String property, T[] value) {
        Filter next = new InFilter(property, value, Filters.Operator.AND.getScript());
        this.setNextToLastNode(next);
        return this;
    }

    public <T> Filter orIn(String property, T[] value) {
        Filter next = new InFilter(property, value, Filters.Operator.OR.getScript());
        this.setNextToLastNode(next);
        return this;
    }

    public Filter andLike(String property, Object value) {
        Filter next = new LikeFilter(property, value, Filters.Operator.AND.getScript());
        this.setNextToLastNode(next);
        return this;
    }

    public Filter orLike(String property, Object value) {
        Filter next = new LikeFilter(property, value, Filters.Operator.AND.getScript());
        this.setNextToLastNode(next);
        return this;
    }

    public Filter andNotLike(String property, Object value) {
        Filter next = new NotLikeFilter(property, value, Filters.Operator.AND.getScript());
        this.setNextToLastNode(next);
        return this;
    }

    public Filter orNotLike(String property, Object value) {
        Filter next = new NotLikeFilter(property, value, Filters.Operator.AND.getScript());
        this.setNextToLastNode(next);
        return this;
    }

    public Filter andIsNull(String property) {
        Filter next = new IsNullFilter(property, Filters.Operator.AND.getScript());
        this.setNextToLastNode(next);
        return this;
    }

    public Filter orIsNull(String property) {
        Filter next = new IsNullFilter(property, Filters.Operator.OR.getScript());
        this.setNextToLastNode(next);
        return this;
    }

    public Filter andIsNotNull(String property) {
        Filter next = new NotNullFilter(property, Filters.Operator.AND.getScript());
        this.setNextToLastNode(next);
        return this;
    }

    public Filter orIsNotNull(String property) {
        Filter next = new NotNullFilter(property, Filters.Operator.OR.getScript());
        this.setNextToLastNode(next);
        return this;
    }

    public String toString() {
        return this.property + " " + this.symbol + " " + formatSqlVal();
    }

    public String toSql() {
        StringBuilder sqlBuilder = new StringBuilder();
        Filter cur = this;
        while (cur != null) {
            sqlBuilder.append(" ").append(cur.getConnectionSymbol()).append(" ").append(cur);
            cur = cur.next;
        }
        return sqlBuilder.substring(2);
    }

    public List<Filter> linkToList() {
        List<Filter> filters = new ArrayList<>();
        Filter cur = this;
        while (cur != null) {
            filters.add(cur);
            cur = cur.next;
        }
        return filters;
    }

    protected String formatSqlVal() {
        return formatSqlVal(value);
    }

    protected String formatSqlVal(Object value) {
        if (value == null) {
            return "null";
        }
        String stringVal;
        if (value instanceof Number) {
            return value.toString();
        } else if (value.getClass().isArray()) {
            return Stream.of((Object[]) value).map(this::formatSqlVal).collect(Collectors.joining(","));
        } else if (value instanceof Collection) {
            return ((Collection<?>) value).stream().map(this::formatSqlVal).collect(Collectors.joining(","));
        } else if (value instanceof Date) {
            stringVal = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(((Date) value));
        } else if (value instanceof ChronoLocalDateTime) {
            stringVal = ((ChronoLocalDateTime<?>) value).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        } else {
            stringVal = value.toString();
        }
        return "'" + stringVal + "'";
    }

    protected String formatVal() {
        if (value == null) {
            return "null";
        }
        if (value.getClass().isArray()) {
            return Stream.of((Object[]) value).map(this::formatSqlVal).collect(Collectors.joining(","));
        } else if (value instanceof Collection) {
            return Stream.of((Collection<?>) value).map(this::formatSqlVal).collect(Collectors.joining(","));
        } else if (value instanceof Date) {
            return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(((Date) value));
        } else if (value instanceof ChronoLocalDateTime) {
            return ((ChronoLocalDateTime<?>) value).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        }
        return value.toString();
    }

    public static Filter filter(String symbol, String property, Object value) {
        return new Filter(symbol, property, value, "");
    }

    public static Filter eq(String property, Object value) {
        return Filter.filter(Filters.Operator.EQ.getScript(), property, value);
    }

    public static Filter notEq(String property, Object value) {
        return Filter.filter(Filters.Operator.NE.getScript(), property, value);
    }

    public static Filter gt(String property, Object value) {
        return Filter.filter(Filters.Operator.GT.getScript(), property, value);
    }

    public static Filter gte(String property, Object value) {
        return Filter.filter(Filters.Operator.GTE.getScript(), property, value);
    }

    public static Filter lt(String property, Object value) {
        return Filter.filter(Filters.Operator.LT.getScript(), property, value);
    }

    public static Filter lte(String property, Object value) {
        return Filter.filter(Filters.Operator.LTE.getScript(), property, value);
    }

    public static <T> Filter in(String property, Collection<T> value) {
        return new InFilter(property, value, "");
    }

    public static <T> Filter in(String property, T[] value) {
        return new InFilter(property, value, "");
    }

    public static Filter like(String property, Object value) {
        return new LikeFilter(property, value, "");
    }

    public static Filter notLike(String property, Object value) {
        return new NotLikeFilter(property, value, "");
    }

    public static Filter isNull(String property) {
        return new IsNullFilter(property, "");
    }

    public static Filter isNotNull(String property) {
        return new NotNullFilter(property, "");
    }

    public static void setUseUnderline(boolean underline) {
        Filter.USE_UNDERLINE = underline;
    }

}
