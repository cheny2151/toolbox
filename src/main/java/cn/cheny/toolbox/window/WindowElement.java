package cn.cheny.toolbox.window;

import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * 窗口元素
 *
 * @author by chenyi
 * @date 2021/9/22
 */
public class WindowElement {

    private Object input;

    private Object output;

    private final CountDownLatch countDownLatch;

    private Throwable error;

    public WindowElement(Object input) {
        this.input = input;
        this.countDownLatch = new CountDownLatch(1);
    }

    public Throwable getError() {
        return error;
    }

    public void setError(Throwable error) {
        this.error = error;
        this.countDownLatch.countDown();
    }

    public Object getInput() {
        return input;
    }

    public void setInput(Object input) {
        this.input = input;
    }

    public Object getOutput() throws Throwable {
        this.countDownLatch.await();
        if (error != null) {
            throw error;
        }
        return output;
    }

    public void setOutput(Object output) {
        this.output = output;
        this.countDownLatch.countDown();
    }

    public void collectInput(List<Object> inputs) {
        inputs.add(getInput());
    }

    public int size() {
        return 1;
    }

    public boolean isMulti() {
        return false;
    }


}
