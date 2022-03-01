package cn.cheny.toolbox.window;

import java.util.Arrays;

/**
 * 窗口收集方法的静态参数实例
 *
 * @author by chenyi
 * @date 2022/3/1
 */
public class CollectedStaticParams {

    private final Object[] staticArgs;

    public CollectedStaticParams(Object[] args, BatchConfiguration batchConfiguration) {
        int batchArgIndex = batchConfiguration.getBatchArgIndex();
        this.staticArgs = collectStatic(args, batchArgIndex);
    }

    private Object[] collectStatic(Object[] args, int batchArgIndex) {
        int length = args.length;
        if (length == 1) {
            return new Object[0];
        }
        Object[] staticArgs = new Object[length];
        int idx = 0;
        for (int i = 0; i < length; i++) {
            if (i == batchArgIndex) {
                continue;
            }
            staticArgs[idx++] = args[i];
        }
        return staticArgs;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CollectedStaticParams that = (CollectedStaticParams) o;
        return Arrays.equals(staticArgs, that.staticArgs);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(staticArgs);
    }
}
