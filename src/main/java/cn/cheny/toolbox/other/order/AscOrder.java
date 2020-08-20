package cn.cheny.toolbox.other.order;

/**
 * 升序
 */
public class AscOrder extends Order {

    public AscOrder(String... property) {
        super(property, Type.asc);
    }

}
