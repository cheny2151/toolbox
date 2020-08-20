package cn.cheny.toolbox.other.page;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.io.Serializable;

/**
 * 基本分页信息
 */
@Data
public class PageInfo implements Serializable {

    private static final long serialVersionUID = 7439257553404129509L;

    /**
     * 默认分页大小
     */
    public final static int DEFAULT_PAGE_SIZE = 20;

    /**
     * 默认页码
     */
    public final static int DEFAULT_PAGE_NUMBER = 1;

    private int pageNumber = DEFAULT_PAGE_NUMBER;

    private int pageSize = DEFAULT_PAGE_SIZE;

    private int totalPage;

    private long total;

    public PageInfo() {
    }

    public PageInfo(int pageNumber, int pageSize) {
        this.pageNumber = pageNumber;
        this.pageSize = pageSize;
    }

    /**
     * 分页起始位置
     */
    @JsonIgnore
    public int getStartSize() {
        return (pageNumber - 1) * pageSize;
    }

}
