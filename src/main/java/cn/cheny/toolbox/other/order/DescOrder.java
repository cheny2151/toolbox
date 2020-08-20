package cn.cheny.toolbox.other.order;

/**
 * 降序
 */
public class DescOrder extends Order {

    public DescOrder(String... property) {
        super(property, Type.desc);
    }

}
