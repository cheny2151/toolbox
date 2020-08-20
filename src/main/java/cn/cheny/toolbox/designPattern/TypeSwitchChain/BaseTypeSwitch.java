package cn.cheny.toolbox.designPattern.TypeSwitchChain;

public abstract class BaseTypeSwitch {

    private BaseTypeSwitch next;

    public BaseTypeSwitch() {
    }

    public BaseTypeSwitch(BaseTypeSwitch next) {
        this.next = next;
    }

    /**
     * 转换类型
     *
     * @param target 目标类型
     * @param value  值（传入的必须不为null）
     * @param <T>
     * @return
     */
    public abstract <T> T transform(Class<T> target, Object value);

    /**
     * 下一个转换器
     *
     * @return
     */
    public BaseTypeSwitch getNext() {
        return next;
    }

    /**
     * 设置下一个转换器
     *
     * @param next
     */
    public void setNext(BaseTypeSwitch next) {
        this.next = next;
    }

    /**
     * 判断是否有后续转换器
     *
     * @return
     */
    public boolean hasNext() {
        return next != null;
    }

}
