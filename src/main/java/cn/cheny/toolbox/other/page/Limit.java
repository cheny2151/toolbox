package cn.cheny.toolbox.other.page;

/**
 * 工具类-分页数据实体
 *
 * @author cheney
 * @date 2019/4/17
 */
public class Limit {

    private int num;

    private int size;

    public Limit() {
    }

    public Limit(int num, int size) {
        this.num = num;
        this.size = size;
    }

    public int getNum() {
        return num;
    }

    public void setNum(int num) {
        this.num = num;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public static Limit create(int num, int size) {
        return new Limit(num, size);
    }
}
