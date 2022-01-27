package cn.cheny.toolbox.window.output;

import cn.cheny.toolbox.reflect.TypeUtils;
import cn.cheny.toolbox.window.WindowElement;

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
        Class<?> outputClass = output.getClass();
        boolean multi = element.isMulti();
        boolean isList = isList(outputClass);
        boolean isArray = isArray(outputClass);
        if (!multi) {
            return get(isList, isArray, output, index);
        } else {
            int size = element.size();
            Object[] arrayResult = new Object[size];
            for (int i = 0; i < size; i++) {
                arrayResult[i] = get(isList, isArray, output, index + i);
            }
            return isArray ? arrayResult : TypeUtils.arrayToCollection(arrayResult, outputClass, Object.class);
        }
    }

}
