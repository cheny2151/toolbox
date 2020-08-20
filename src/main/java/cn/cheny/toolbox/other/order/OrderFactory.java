package cn.cheny.toolbox.other.order;

/**
 * 排序工厂类
 */
public class OrderFactory {

    /**
     * 默认排序属性
     */
    public final static String DEFAULT_ORDER_PRO = "createDate";

    private OrderFactory() {
    }

    public static Order asc(String... property) {
        return new AscOrder(property);
    }

    public static Order desc(String... property) {
        return new DescOrder(property);
    }

    public static Order defaultOrder() {
        return new DescOrder(DEFAULT_ORDER_PRO);
    }

}
