package cn.cheny.toolbox.window.output;

import cn.cheny.toolbox.exception.ToolboxRuntimeException;
import cn.cheny.toolbox.window.WindowElement;
import org.apache.commons.lang3.ArrayUtils;

import java.util.List;

/**
 * 默认批处理结果分割器
 * 批处理返回数组/有序集合为默认行为，该分割器对此类行为进行处理
 *
 * @author by chenyi
 * @date 2022/1/24
 */
public class DefaultBatchResultSplitter implements BatchResultSplitter {

    @Override
    public Object split(Object outputs, WindowElement element, int index) {
        Class<?> outputsClass = outputs.getClass();
        if (List.class.isAssignableFrom(outputsClass)) {
            return ((List<?>) outputs).get(index);
        } else if (outputsClass.isArray()) {
            return ArrayUtils.get((Object[]) outputs, index);
        }
        throw new ToolboxRuntimeException("Can not use DefaultOutputSplit to split outputs,outputs class is not List/Array");
    }
}
