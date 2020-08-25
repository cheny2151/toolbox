package cn.cheny.toolbox.other;

import java.util.function.Consumer;

/**
 * @author cheney
 * @date 2019-07-03
 */
public class ResultAndFlag<T> {

    private T result;

    private boolean flag;

    private String msg;

    public ResultAndFlag(T result, boolean flag, String msg) {
        this.result = result;
        this.flag = flag;
        this.msg = msg;
    }

    public ResultAndFlag(T result, boolean flag) {
        this.result = result;
        this.flag = flag;
    }

    public static <T> ResultAndFlag<T> success(T result) {
        return new ResultAndFlag<>(result, true, null);
    }

    public static <T> ResultAndFlag<T> fail() {
        return new ResultAndFlag<>(null, false, null);
    }

    public static <T> ResultAndFlag<T> fail(T data) {
        return new ResultAndFlag<>(data, false, null);
    }

    public static <T> ResultAndFlag<T> fail(String msg, T data) {
        return new ResultAndFlag<>(data, false, msg);
    }

    public T getResult() {
        return result;
    }

    public boolean isSuccess() {
        return flag;
    }

    public String getMsg() {
        return msg;
    }

    public void ifSuccess(Consumer<T> consumer) {
        if (flag) {
            consumer.accept(result);
        }
    }
}
