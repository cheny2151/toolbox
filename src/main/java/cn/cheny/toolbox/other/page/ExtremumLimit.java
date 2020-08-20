package cn.cheny.toolbox.other.page;

/**
 * 工具类-根据字段大小极值分页
 *
 * @author cheney
 * @date 2019/4/17
 */
public class ExtremumLimit {

    /**
     * 极小值
     */
    public final static byte MINIMUM_TYPE = 0;

    /**
     * 极大值
     */
    public final static byte MAXIMUM_TYPE = 1;

    /**
     * 极值
     */
    private Object extremum;

    /**
     * 极值类型
     * 0为极小值，1为极大值
     */
    private byte type;

    /**
     * limit大小
     */
    private int size;

    public ExtremumLimit(Object extremum, int size, ExtremumType extremumType) {
        this.extremum = extremum;
        this.size = size;
        this.type = extremumType.getType();
    }

    public Object getExtremum() {
        return extremum;
    }

    public void setExtremum(Object extremum) {
        this.extremum = extremum;
    }

    public byte getType() {
        return type;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public static ExtremumLimit create(Object extremum, int size, ExtremumType extremumType) {
        return new ExtremumLimit(extremum, size, extremumType);
    }

    public enum ExtremumType {
        /**
         * 极小值
         */
        MINIMUM(MINIMUM_TYPE),
        /**
         * 极大值
         */
        MAXIMUM(MAXIMUM_TYPE);

        private byte type;

        ExtremumType(byte type) {
            this.type = type;
        }

        public byte getType() {
            return type;
        }
    }
}
