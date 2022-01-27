package cn.cheny.toolbox.window.output;

import cn.cheny.toolbox.exception.ToolboxRuntimeException;
import cn.cheny.toolbox.reflect.TypeUtils;
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
    public Object split(Object output, WindowElement element, int index) {
        Class<?> outputsClass = output.getClass();
        boolean multi = element.isMulti();
        boolean isList = List.class.isAssignableFrom(outputsClass);
        boolean isArray = outputsClass.isArray();
        if (!multi) {
            return get(isList, isArray, output, index);
        } else {
            int size = element.size();
            Object[] arrayResult = new Object[size];
            for (int i = 0; i < size; i++) {
                arrayResult[i] = get(isList, isArray, output, index + i);
            }
            return isArray ? arrayResult : TypeUtils.arrayToCollection(arrayResult, outputsClass, Object.class);
        }
    }

    private Object get(boolean isList, boolean isArray, Object output, int index) {
        if (isList) {
            return ((List<?>) output).get(index);
        } else if (isArray) {
            return ArrayUtils.get((Object[]) output, index);
        }
        throw new ToolboxRuntimeException("Can not use DefaultOutputSplit to split outputs,outputs class is not List/Array");
    }
}
