package cn.cheny.toolbox.other.filter;


import java.time.LocalDateTime;
import java.util.*;

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

        IS_NULL("is null"),

        IS_NOT_NULL("is not null"),

        AND("and"),

        OR("or");

        private final String script;

        Operator(String script) {
            this.script = script;
        }

        public String getScript() {
            return script;
        }
    }

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
        this.filters.add(new FilterSegment(filter, Operator.AND.getScript()));
        return this;
    }

    public Filters orFilter(Filter filter) {
        this.filters.add(new FilterSegment(filter, Operator.OR.getScript()));
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
        this.otherParams.clear();
    }

    public Map<String, Object> getOtherParams() {
        return otherParams;
    }

    public boolean isHasOtherParams() {
        Map<String, Object> otherParams = this.otherParams;
        return otherParams != null && otherParams.size() > 0;
    }

    public static Filters build() {
        return new Filters();
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
                sqlBuilder.append(" ").append(filterSegment.connection).append(" （")
                        .append(" ").append(filter.toSql()).append(")");
            }
        }
        return sqlBuilder.substring(1);
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

    public static void main(String[] args) {
        Filter filter = Filter.eq("test", new Date()).andEq("test2", 2)
                .andEq("test3", LocalDateTime.now()).andIsNull("test4").orGt("test5", "5");
        Filter filter2 = Filter.in("test6", new String[]{"1", "2", "3", "4", "5", "6", "7"})
                .andLike("test7", "100");
        String sql = Filters.build().andFilter(filter).orFilter(filter2).toSql();
        System.out.println(sql);
    }

}
