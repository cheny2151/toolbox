package cn.cheny.toolbox.asyncTask.pool2;

import java.util.Arrays;

/**
 * @author by chenyi
 * @date 2022/1/17
 */
enum State {

    // 失活
    INACTIVATION(0),
    // 激活
    ACTIVATION(1),
    // 归还中
    RETURNING(2),
    // 关闭
    CLOSED(3);

    private final int val;

    State(int val) {
        this.val = val;
    }

    public int getVal() {
        return val;
    }

    public static State valueOf(int val) {
        return Arrays.stream(State.values()).filter(e -> e.getVal() == val)
                .findFirst()
                .orElseThrow(IllegalAccessError::new);
    }

}
