package cn.cheny.toolbox.window;

import java.util.List;

/**
 * 窗口元素
 *
 * @author by chenyi
 * @date 2021/9/22
 */
public class WindowElementCollection extends WindowElement {

    private final int size;

    public WindowElementCollection(List<?> input) {
        super(input);
        this.size = input.size();
    }

    public void collectInput(List<Object> inputs) {
        inputs.addAll((List<?>) getInput());
    }

    public int size() {
        return size;
    }

}
