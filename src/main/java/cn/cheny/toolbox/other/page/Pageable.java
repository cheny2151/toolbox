package cn.cheny.toolbox.other.page;

import cn.cheny.toolbox.other.filter.Filter;
import cn.cheny.toolbox.other.filter.Filters;
import cn.cheny.toolbox.other.order.Orders;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Map;

/**
 * 分页信息(带过滤,排序)
 * Created by cheny on 2017/9/24.
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class Pageable extends PageInfo {

    private static final long serialVersionUID = 5705303253597757865L;

    private Filters filters;

    private Orders orders;

    public Pageable() {
    }

    public Pageable(int pageNumber, int pageSize) {
        super(pageNumber,pageSize);
    }

    public static Pageable createByPageInfo(PageInfo pageInfo) {
        return new Pageable(pageInfo.getPageNumber(), pageInfo.getPageSize());
    }

    /**
     * 为Filters添加Filter
     *
     * @param filter 过滤条件
     * @return Filters
     */
    public Filters andFilter(Filter filter) {
        Filters filters = this.filters;
        if (filters == null) {
            filters = this.filters = new Filters();
        }
        filters.andFilter(filter);
        return filters;
    }

    public Filters orFilter(Filter filter) {
        Filters filters = this.filters;
        if (filters == null) {
            filters = this.filters = new Filters();
        }
        filters.orFilter(filter);
        return filters;
    }

    /**
     * 添加其他过滤条件到Filters对象
     *
     * @param params 其他过滤条件
     * @return Filters
     */
    public Filters addOtherParams(Map<String, Object> params) {
        Filters filters = this.filters;
        if (filters == null) {
            filters = this.filters = new Filters();
        }
        filters.addOtherParams(params);
        return filters;
    }

}
