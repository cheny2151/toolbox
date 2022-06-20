package cn.cheny.toolbox.other.filter;


import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 属性过滤实体
 */
public class Filters {

    public enum Operator {

        EQ("="),

        NE("!="),

        GT(">"),

        LT("<"),

        GTE(">="),

        LTE("<="),

        LIKE("like"),

        NOT_LIKE("not like"),

        IN("in"),

        NOT_IN("not in"),

        IS_NULL("is null"),

        IS_NOT_NULL("is not null");

        private final String script;

        Operator(String script) {
            this.script = script;
        }

        public String getScript() {
            return script;
        }
    }

    public enum Connection {

        AND("and"),

        OR("or");

        private final String symbol;

        Connection(String symbol) {
            this.symbol = symbol;
        }

        public String getSymbol() {
            return symbol;
        }
    }

    private Integer skip;

    private Integer limit;

    private List<FilterSegment> filters;

    /**
     * 存储以map为载体的其他过滤参数
     */
    private Map<String, Object> otherParams;

    public Filters() {
        this.filters = new ArrayList<>();
    }

    public Filters(Filter filter) {
        this();
        andFilter(filter);
    }

    public Filters andFilter(Filter filter) {
        this.filters.add(new FilterSegment(filter, Connection.AND.getSymbol()));
        return this;
    }

    public Filters orFilter(Filter filter) {
        this.filters.add(new FilterSegment(filter, Connection.OR.getSymbol()));
        return this;
    }

    public Filters andFilter(Filter filter, String tableName) {
        concatTableName(filter, tableName);
        this.filters.add(new FilterSegment(filter, Connection.AND.getSymbol()));
        return this;
    }

    public Filters orFilter(Filter filter, String tableName) {
        concatTableName(filter, tableName);
        this.filters.add(new FilterSegment(filter, Connection.OR.getSymbol()));
        return this;
    }

    public Filters addOtherParams(String key, Object value) {
        if (this.otherParams == null) {
            this.otherParams = new HashMap<>();
        }
        this.otherParams.put(key, value);
        return this;
    }

    public Filters addOtherParams(Map<String, Object> params) {
        if (this.otherParams == null) {
            this.otherParams = new HashMap<>();
        }
        this.otherParams.putAll(params);
        return this;
    }

    public void clearOtherParams() {
        this.otherParams = new HashMap<>();
    }

    public void setOtherParams(Map<String, Object> otherParams) {
        this.otherParams = otherParams;
    }

    public Map<String, Object> getOtherParams() {
        return otherParams;
    }

    public boolean isHasOtherParams() {
        Map<String, Object> otherParams = this.otherParams;
        return otherParams != null && otherParams.size() > 0;
    }

    public void setFilters(List<FilterSegment> filters) {
        this.filters = filters;
    }

    public List<FilterSegment> getFilters() {
        return filters;
    }

    public Integer getSkip() {
        return skip;
    }

    public void setSkip(Integer skip) {
        this.skip = skip;
    }

    public Filters skip(Integer skip) {
        this.setSkip(skip);
        return this;
    }

    public Integer getLimit() {
        return limit;
    }

    public void setLimit(Integer limit) {
        this.limit = limit;
    }

    public Filters limit(Integer limit) {
        this.setLimit(limit);
        return this;
    }

    public static Filters build() {
        return new Filters();
    }

    public static Filters build(Filter filter) {
        return new Filters().andFilter(filter);
    }

    public static Filters build(Filter filter, String tableName) {
        return new Filters().andFilter(filter, tableName);
    }

    public String toSql() {
        StringBuilder sqlBuilder = new StringBuilder();
        boolean first = true;
        for (FilterSegment filterSegment : this.filters) {
            Filter filter = filterSegment.filter;
            if (first) {
                first = false;
                sqlBuilder.append(" ").append(filter.toSql());
            } else {
                sqlBuilder.append(" ").append(filterSegment.connection)
                        .append(" (").append(filter.toSql()).append(")");
            }
        }
        return sqlBuilder.substring(1);
    }

    /**
     * 拼装表名
     *
     * @param filter    过滤实体
     * @param tableName 表名
     */
    private void concatTableName(Filter filter, String tableName) {
        if (StringUtils.isNotBlank(tableName)) {
            while (filter != null) {
                filter.setProperty(tableName + "." + filter.getProperty());
                filter = filter.getNext();
            }
        }
    }

    private static class FilterSegment {
        private final Filter filter;
        private final String connection;

        public FilterSegment(Filter filter, String connection) {
            this.filter = filter;
            this.connection = connection;
        }

        public Filter getFilter() {
            return filter;
        }

        public String getConnection() {
            return connection;
        }
    }

}
