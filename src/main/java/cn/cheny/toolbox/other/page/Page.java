package cn.cheny.toolbox.other.page;

import java.util.ArrayList;
import java.util.List;

/**
 * 分页最外层(页面展示用)
 * Created by cheny on 2017/9/24.
 */
public class Page<T> extends PageInfo {

    private static final long serialVersionUID = 285590709870311132L;

    private List<T> content;

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

    public List<T> getContent() {
        return content;
    }

    public void setContent(List<T> content) {
        this.content = content;
    }

    /**
     * 生成一个空列表的分页
     */
    public static <T> Page<T> emptyPage(Pageable pageable) {
        return new Page<>(new ArrayList<>(0), 0L, pageable);
    }
}
