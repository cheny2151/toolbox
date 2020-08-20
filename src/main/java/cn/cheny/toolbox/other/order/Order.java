package cn.cheny.toolbox.other.order;

/**
 * 排序父类
 */
public abstract class Order {

    private String[] properties;

    private Type type;

    public enum Type{

        asc,

        desc

    }

    public Order(String[] properties, Type type) {
        if (properties == null || properties.length == 0) {
            throw new IllegalArgumentException("illegal arg property");
        }
        this.properties = properties;
        this.type = type;
    }

    public String[] getProperties() {
        return properties;
    }

    public void setProperties(String[] properties) {
        this.properties = properties;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }
}
