package cn.cheny.toolbox.other.order;

import cn.cheny.toolbox.property.PropertyNameUtils;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 排序集合类
 *
 * @author cheney
 * @date 2019-07-08
 */
@NoArgsConstructor
public class Orders extends ArrayList<Orders.Order> {

    /**
     * 默认主表标识
     */
    private final static String MAIN_TABLE = "t0.";

    /**
     * 排序类
     */
    @NoArgsConstructor
    public static class Order {

        private final static OrderType DEFAULT_TYPE = OrderType.desc;

        private final static boolean USE_UNDERLINE = true;

        /**
         * 属性名
         */
        private String property;

        /**
         * 排序类型
         */
        private OrderType type = DEFAULT_TYPE;

        public Order(String property, OrderType type) {
            this.property = property;
            if (type != null) {
                this.type = type;
            }
        }

        public Order(String property, String type) {
            this.property = property;
            if (EnumUtils.isValidEnum(OrderType.class, type)) {
                this.type = OrderType.valueOf(type);
            }
        }

        public static Order asc(String property) {
            return new Order(property, OrderType.asc);
        }

        public static Order desc(String property) {
            return new Order(property, OrderType.desc);
        }

        public String getProperty() {
            String property = this.property;
            if (USE_UNDERLINE && StringUtils.isNotEmpty(property)) {
                property = PropertyNameUtils.underline(property);
            }
            if (!property.contains(".")) {
                return MAIN_TABLE + property;
            }
            return property;
        }

        public void setProperty(String property) {
            this.property = property;
        }

        public OrderType getType() {
            return type;
        }

        public void setType(OrderType type) {
            this.type = type;
        }

        public boolean valid() {
            return StringUtils.isNotEmpty(this.property);
        }
    }

    /**
     * 排序类型枚举
     */
    public enum OrderType {
        asc,
        desc
    }

    public List<Order> getOrders() {
        return this.stream().filter(Order::valid).collect(Collectors.toList());
    }

    public boolean getValid() {
        return this.getOrders().size() > 0;
    }

    public void setOrders(List<Order> orders) {
        this.clear();
        this.addAll(orders);
    }

    private void addOrder(Order order) {
        this.add(order);
    }

    public Orders addAscOrder(String property) {
        if (!StringUtils.isEmpty(property)) {
            this.add(Order.asc(property));
        }
        return this;
    }

    public Orders addDescOrder(String property) {
        if (!StringUtils.isEmpty(property)) {
            this.add(Order.desc(property));
        }
        return this;
    }

    public static Orders singleton(Order order) {
        Orders orders = new Orders();
        orders.addOrder(order);
        return orders;
    }

    public static Orders singletonAsc(String property) {
        return singleton(Order.asc(property));
    }

    public static Orders singletonDesc(String property) {
        return singleton(Order.desc(property));
    }
}
