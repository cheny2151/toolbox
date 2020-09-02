package cn.cheny.toolbox.other.page;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * 分页最外层(页面展示用)
 * Created by cheny on 2017/9/24.
 */
public class Page<T> extends PageInfo {

    private static final long serialVersionUID = 285590709870311132L;

    private Collection<T> content;

    public Page() {
    }

    public Page(List<T> content, long total, PageInfo pageInfo) {
        super();
        super.setTotal(total);
        super.setPageNumber(pageInfo.getPageNumber());
        super.setPageSize(pageInfo.getPageSize());
        if (total == 0) {
            super.setTotalPage(0);
        } else {
            int pageSize = pageInfo.getPageSize();
            super.setTotalPage(
                    (int) ((total + pageSize - 1) / pageSize)
            );
        }
        this.content = content;
    }

    public Collection<T> getContent() {
        return content;
    }

    public void setContent(Collection<T> content) {
        this.content = content;
    }

    /**
     * 生成一个空列表的分页
     */
    public static <T> Page<T> emptyPage(Pageable pageable) {
        return new Page<>(new ArrayList<>(0), 0L, pageable);
    }

    public static <T> Page<T> build(long total, int pageSize, int pageNum, Collection<T> list) {
        Page<T> page = new Page<>();
        page.setTotal(total);
        page.setPageSize(pageSize);
        page.setPageNumber(pageNum);
        page.setContent(list);
        page.setTotalPage((int) ((total + pageSize - 1) / pageSize));
        return page;
    }

    public static <T> Page<T> build(long total, Pageable page, List<T> list) {
        return build(total, page.getPageSize(), page.getPageNumber(), list);
    }
}
