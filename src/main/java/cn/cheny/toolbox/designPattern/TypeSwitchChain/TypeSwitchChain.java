package cn.cheny.toolbox.designPattern.TypeSwitchChain;

/**
 * 类型转换链
 */
public class TypeSwitchChain {

    private BaseTypeSwitch start;

    private BaseTypeSwitch end;

    private TypeSwitchChain() {
    }

    /**
     * 单例模式
     */
    private final static class TypeSwitchChainHolder {
        private final static TypeSwitchChain TYPE_SWITCH_CHAIN;

        static {
            TYPE_SWITCH_CHAIN = new TypeSwitchChain();
            DateTypeSwitch end = new DateTypeSwitch();
            TYPE_SWITCH_CHAIN.end = end;
            TYPE_SWITCH_CHAIN.start = new StringSwitch(new NumberSwitch(new BooleanSwitch(end)));
        }

    }

    /**
     * 获取单例
     *
     * @return
     */
    public static TypeSwitchChain getTypeSwitchChain() {
        return TypeSwitchChainHolder.TYPE_SWITCH_CHAIN;
    }

    /**
     * 在尾部追加一个转换器
     *
     * @param next
     */
    public void addSwitch(BaseTypeSwitch next) {
        this.end.setNext(next);
        this.end = next;
    }

    public <T> T startTransform(Class<T> target, Object value) {
        return value != null ? getStartSwitch().transform(target, value) : null;
    }

    private BaseTypeSwitch getStartSwitch() {
        return this.start;
    }

}
