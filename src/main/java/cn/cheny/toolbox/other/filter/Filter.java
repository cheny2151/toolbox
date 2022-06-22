package cn.cheny.toolbox.other.filter;

import cn.cheny.toolbox.property.PropertyNameUtils;
import cn.cheny.toolbox.reflect.FieldGetter;
import cn.cheny.toolbox.reflect.ReflectUtils;
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

    // 表/字段连接符
    protected static String TABLE_CONNECTION = ".";

    // 是否下划线默认值
    private static boolean DEFAULT_USE_UNDERLINE = true;

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

    // 是否下划线
    private Boolean useUnderLine;

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
        String property = this.property;
        if (getUseUnderLine() && StringUtils.isNotEmpty(property)) {
            if (property.contains(TABLE_CONNECTION)) {
                String[] split = property.split(TABLE_CONNECTION);
                if (split.length == 2) {
                    property = split[0] + PropertyNameUtils.underline(split[1]);
                }
            } else {
                property = PropertyNameUtils.underline(property);
            }
        }
        return property;
    }

    public void setProperty(String property) {
        this.property = property;
    }

    public String getConnectionSymbol() {
        return connectionSymbol == null ? Filters.Connection.AND.getSymbol() : connectionSymbol;
    }

    public void setConnectionSymbol(String connectionSymbol) {
        this.connectionSymbol = connectionSymbol;
    }

    public boolean getUseUnderLine() {
        if (this.useUnderLine != null) {
            return this.useUnderLine;
        }
        return DEFAULT_USE_UNDERLINE;
    }

    public void setUseUnderline(boolean underline) {
        this.useUnderLine = underline;
    }

    public Filter useUnderline(boolean underline) {
        this.setUseUnderline(underline);
        return this;
    }

    public Filter getNext() {
        return next;
    }

    public void setNext(Filter next) {
        this.next = next;
    }

    public Filter andFilter(Filters.Operator operator, String property, Object value) {
        Filter next = buildFilter(operator, property, value, Filters.Connection.AND);
        this.setNextToLastNode(next);
        return this;
    }

    public <F> Filter andFilter(Filters.Operator operator, FieldGetter<F> fieldGetter, Object value) {
        return andFilter(operator, ReflectUtils.fieldName(fieldGetter), value);
    }

    public Filter orFilter(Filters.Operator operator, String property, Object value) {
        Filter next = buildFilter(operator, property, value, Filters.Connection.OR);
        this.setNextToLastNode(next);
        return this;
    }

    public <F> Filter orFilter(Filters.Operator operator, FieldGetter<F> fieldGetter, Object value) {
        return orFilter(operator, ReflectUtils.fieldName(fieldGetter), value);
    }

    public Filter andEq(String property, Object value) {
        return this.andFilter(Filters.Operator.EQ, property, value);
    }

    public <F> Filter andEq(FieldGetter<F> fieldGetter, Object value) {
        return this.andFilter(Filters.Operator.EQ, fieldGetter, value);
    }

    public Filter orEq(String property, Object value) {
        return this.orFilter(Filters.Operator.EQ, property, value);
    }

    public <F> Filter orEq(FieldGetter<F> fieldGetter, Object value) {
        return this.orFilter(Filters.Operator.EQ, fieldGetter, value);
    }

    public Filter andNotEq(String property, Object value) {
        return this.andFilter(Filters.Operator.NE, property, value);
    }

    public <F> Filter andNotEq(FieldGetter<F> fieldGetter, Object value) {
        return this.andFilter(Filters.Operator.NE, fieldGetter, value);
    }

    public Filter orNotEq(String property, Object value) {
        return this.orFilter(Filters.Operator.NE, property, value);
    }

    public <F> Filter orNotEq(FieldGetter<F> fieldGetter, Object value) {
        return this.orFilter(Filters.Operator.NE, fieldGetter, value);
    }

    public Filter andGt(String property, Object value) {
        return this.andFilter(Filters.Operator.GT, property, value);
    }

    public <F> Filter andGt(FieldGetter<F> fieldGetter, Object value) {
        return this.andFilter(Filters.Operator.GT, fieldGetter, value);
    }

    public Filter orGt(String property, Object value) {
        return this.orFilter(Filters.Operator.GT, property, value);
    }

    public <F> Filter orGt(FieldGetter<F> fieldGetter, Object value) {
        return this.orFilter(Filters.Operator.GT, fieldGetter, value);
    }

    public Filter andGte(String property, Object value) {
        return this.andFilter(Filters.Operator.GTE, property, value);
    }

    public <F> Filter andGte(FieldGetter<F> fieldGetter, Object value) {
        return this.andFilter(Filters.Operator.GTE, fieldGetter, value);
    }

    public Filter orGte(String property, Object value) {
        return this.orFilter(Filters.Operator.GTE, property, value);
    }

    public <F> Filter orGte(FieldGetter<F> fieldGetter, Object value) {
        return this.orFilter(Filters.Operator.GTE, fieldGetter, value);
    }

    public Filter andLt(String property, Object value) {
        return this.andFilter(Filters.Operator.LT, property, value);
    }

    public <F> Filter andLt(FieldGetter<F> fieldGetter, Object value) {
        return this.andFilter(Filters.Operator.LT, fieldGetter, value);
    }

    public Filter orLt(String property, Object value) {
        return this.orFilter(Filters.Operator.LT, property, value);
    }

    public <F> Filter orLt(FieldGetter<F> fieldGetter, Object value) {
        return this.orFilter(Filters.Operator.LT, fieldGetter, value);
    }

    public Filter andLte(String property, Object value) {
        return this.andFilter(Filters.Operator.LTE, property, value);
    }

    public <F> Filter andLte(FieldGetter<F> fieldGetter, Object value) {
        return this.andFilter(Filters.Operator.LTE, fieldGetter, value);
    }

    public Filter orLte(String property, Object value) {
        return this.orFilter(Filters.Operator.LTE, property, value);
    }

    public <F> Filter orLte(FieldGetter<F> fieldGetter, Object value) {
        return this.orFilter(Filters.Operator.LTE, fieldGetter, value);
    }

    public <T> Filter andIn(String property, Collection<T> value) {
        return this.andFilter(Filters.Operator.IN, property, value);
    }

    public <F, T> Filter andIn(FieldGetter<F> fieldGetter, Collection<T> value) {
        return this.andFilter(Filters.Operator.IN, fieldGetter, value);
    }

    public <T> Filter orIn(String property, Collection<T> value) {
        return this.orFilter(Filters.Operator.IN, property, value);
    }

    public <F, T> Filter orIn(FieldGetter<F> fieldGetter, Collection<T> value) {
        return this.orFilter(Filters.Operator.IN, fieldGetter, value);
    }

    public <T> Filter andIn(String property, T[] value) {
        return this.andFilter(Filters.Operator.IN, property, value);
    }

    public <F, T> Filter andIn(FieldGetter<F> fieldGetter, T[] value) {
        return this.andFilter(Filters.Operator.IN, fieldGetter, value);
    }

    public <T> Filter orIn(String property, T[] value) {
        return this.orFilter(Filters.Operator.IN, property, value);
    }

    public <F, T> Filter orIn(FieldGetter<F> fieldGetter, T[] value) {
        return this.orFilter(Filters.Operator.IN, fieldGetter, value);
    }

    public Filter andLike(String property, Object value) {
        return this.andFilter(Filters.Operator.LIKE, property, value);
    }

    public <F> Filter andLike(FieldGetter<F> fieldGetter, Object value) {
        return this.andFilter(Filters.Operator.LIKE, fieldGetter, value);
    }

    public Filter orLike(String property, Object value) {
        return this.orFilter(Filters.Operator.LIKE, property, value);
    }

    public <F> Filter orLike(FieldGetter<F> fieldGetter, Object value) {
        return this.orFilter(Filters.Operator.LIKE, fieldGetter, value);
    }

    public Filter andNotLike(String property, Object value) {
        return this.andFilter(Filters.Operator.NOT_LIKE, property, value);
    }

    public <F> Filter andNotLike(FieldGetter<F> fieldGetter, Object value) {
        return this.andFilter(Filters.Operator.NOT_LIKE, fieldGetter, value);
    }

    public Filter orNotLike(String property, Object value) {
        return this.orFilter(Filters.Operator.NOT_LIKE, property, value);
    }

    public <F> Filter orNotLike(FieldGetter<F> fieldGetter, Object value) {
        return this.orFilter(Filters.Operator.NOT_LIKE, fieldGetter, value);
    }

    public Filter andIsNull(String property) {
        return this.andFilter(Filters.Operator.IS_NULL, property, null);
    }

    public <F> Filter andIsNull(FieldGetter<F> fieldGetter) {
        return this.andFilter(Filters.Operator.IS_NULL, fieldGetter, null);
    }

    public Filter orIsNull(String property) {
        return this.orFilter(Filters.Operator.IS_NULL, property, null);
    }

    public <F> Filter orIsNull(FieldGetter<F> fieldGetter) {
        return this.orFilter(Filters.Operator.IS_NULL, fieldGetter, null);
    }

    public Filter andIsNotNull(String property) {
        return this.andFilter(Filters.Operator.IS_NOT_NULL, property, null);
    }

    public <F> Filter andIsNotNull(FieldGetter<F> fieldGetter) {
        return this.andFilter(Filters.Operator.IS_NOT_NULL, fieldGetter, null);
    }

    public Filter orIsNotNull(String property) {
        return this.orFilter(Filters.Operator.IS_NOT_NULL, property, null);
    }

    public <F> Filter orIsNotNull(FieldGetter<F> fieldGetter) {
        return this.orFilter(Filters.Operator.IS_NOT_NULL, fieldGetter, null);
    }

    public static Filter filter(Filters.Operator operator, String property, Object value) {
        return buildFilter(operator, property, value, null);
    }

    public static <F> Filter filter(Filters.Operator operator, FieldGetter<F> fieldGetter, Object value) {
        return buildFilter(operator, ReflectUtils.fieldName(fieldGetter), value, null);
    }

    public static Filter eq(String property, Object value) {
        return Filter.filter(Filters.Operator.EQ, property, value);
    }

    public static <F> Filter eq(FieldGetter<F> fieldGetter, Object value) {
        return Filter.filter(Filters.Operator.EQ, fieldGetter, value);
    }

    public static Filter notEq(String property, Object value) {
        return Filter.filter(Filters.Operator.NE, property, value);
    }

    public static <F> Filter notEq(FieldGetter<F> fieldGetter, Object value) {
        return Filter.filter(Filters.Operator.NE, fieldGetter, value);
    }

    public static Filter gt(String property, Object value) {
        return Filter.filter(Filters.Operator.GT, property, value);
    }

    public static <F> Filter gt(FieldGetter<F> fieldGetter, Object value) {
        return Filter.filter(Filters.Operator.GT, fieldGetter, value);
    }

    public static Filter gte(String property, Object value) {
        return Filter.filter(Filters.Operator.GTE, property, value);
    }

    public static <F> Filter gte(FieldGetter<F> fieldGetter, Object value) {
        return Filter.filter(Filters.Operator.GTE, fieldGetter, value);
    }

    public static Filter lt(String property, Object value) {
        return Filter.filter(Filters.Operator.LT, property, value);
    }

    public static <F> Filter lt(FieldGetter<F> fieldGetter, Object value) {
        return Filter.filter(Filters.Operator.LT, fieldGetter, value);
    }

    public static Filter lte(String property, Object value) {
        return Filter.filter(Filters.Operator.LTE, property, value);
    }


    public static <F> Filter lte(FieldGetter<F> fieldGetter, Object value) {
        return Filter.filter(Filters.Operator.LTE, fieldGetter, value);
    }

    public static <T> Filter in(String property, Collection<T> value) {
        return Filter.filter(Filters.Operator.IN, property, value);
    }

    public static <F, T> Filter in(FieldGetter<F> fieldGetter, Collection<T> value) {
        return Filter.filter(Filters.Operator.IN, fieldGetter, value);
    }

    public static <T> Filter in(String property, T[] value) {
        return Filter.filter(Filters.Operator.IN, property, value);
    }

    public static <F, T> Filter in(FieldGetter<F> fieldGetter, T[] value) {
        return Filter.filter(Filters.Operator.IN, fieldGetter, value);
    }

    public static Filter like(String property, Object value) {
        return Filter.filter(Filters.Operator.LIKE, property, value);
    }

    public static <F> Filter like(FieldGetter<F> fieldGetter, Object value) {
        return Filter.filter(Filters.Operator.LIKE, fieldGetter, value);
    }

    public static Filter notLike(String property, Object value) {
        return Filter.filter(Filters.Operator.NOT_LIKE, property, value);
    }

    public static <F> Filter notLike(FieldGetter<F> fieldGetter, Object value) {
        return Filter.filter(Filters.Operator.NOT_LIKE, fieldGetter, value);
    }

    public static Filter isNull(String property) {
        return Filter.filter(Filters.Operator.IS_NULL, property, null);
    }

    public static <F> Filter isNull(FieldGetter<F> fieldGetter) {
        return Filter.filter(Filters.Operator.IS_NULL, fieldGetter, null);
    }

    public static Filter isNotNull(String property) {
        return Filter.filter(Filters.Operator.IS_NOT_NULL, property, null);
    }

    public static <F> Filter isNotNull(FieldGetter<F> fieldGetter) {
        return Filter.filter(Filters.Operator.IS_NOT_NULL, fieldGetter, null);
    }

    public static void setDefaultUseUnderline(boolean underline) {
        Filter.DEFAULT_USE_UNDERLINE = underline;
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

    protected static Filter buildFilter(Filters.Operator operator, String property, Object value, Filters.Connection connection) {
        String symbol = connection == null ? "" : connection.getSymbol();
        Filter filter;
        switch (operator) {
            case IN: {
                filter = new InFilter(property, value, symbol);
                break;
            }
            case NOT_IN: {
                filter = new NotInFilter(property, value, symbol);
                break;
            }
            case LIKE: {
                filter = new LikeFilter(property, value, symbol);
                break;
            }
            case NOT_LIKE: {
                filter = new NotLikeFilter(property, value, symbol);
                break;
            }
            case IS_NULL: {
                filter = new IsNullFilter(property, symbol);
                break;
            }
            case IS_NOT_NULL: {
                filter = new NotNullFilter(property, symbol);
                break;
            }
            default: {
                filter = new Filter(operator.getScript(), property, value, symbol);
            }
        }
        return filter;
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

    private void setNextToLastNode(Filter next) {
        Filter node = this;
        while (node.next != null) {
            node = node.next;
        }
        node.setNext(next);
    }

}
