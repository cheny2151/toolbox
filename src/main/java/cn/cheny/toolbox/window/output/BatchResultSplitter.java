package cn.cheny.toolbox.window.output;

import cn.cheny.toolbox.window.WindowElement;

/**
 * 批处理结果分割器
 *
 * @author by chenyi
 * @date 2022/1/24
 */
public interface BatchResultSplitter {

    Object split(Object outputs, WindowElement element, int index);

}
