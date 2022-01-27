package cn.cheny.toolbox.window.output;

import cn.cheny.toolbox.exception.ToolboxRuntimeException;
import cn.cheny.toolbox.reflect.TypeReference;
import cn.cheny.toolbox.reflect.TypeUtils;
import cn.cheny.toolbox.window.WindowElement;
import org.apache.commons.lang3.ArrayUtils;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.List;

/**
 * 批处理结果分割器
 *
 * @author by chenyi
 * @date 2022/1/24
 */
public interface BatchResultSplitter {

    /**
     * 分割批处理结果
     *
     * @param output  批处理结果
     * @param element 当前批次元素
     * @param index   当前批次在结果中的index
     * @return 批次结果
     */
    Object split(Object output, WindowElement element, int index);

    /**
     * 判断是否为数组
     *
     * @param outputClass 类型
     * @return 判断结果
     */
    default boolean isArray(Class<?> outputClass) {
        return outputClass.isArray();
    }

    /**
     * 判断是否为List集合或者其子类
     *
     * @param outputClass 类型
     * @return 判断结果
     */
    default boolean isList(Class<?> outputClass) {
        return List.class.isAssignableFrom(outputClass);
    }

    /**
     * 获取数组/List集合目标index的元素
     *
     * @param isList  是否为我数组
     * @param isArray 是否为集合
     * @param output  目标对象
     * @param index   目标位置
     * @return 元素
     */
    default Object get(boolean isList, boolean isArray, Object output, int index) {
        if (isList) {
            return ((List<?>) output).get(index);
        } else if (isArray) {
            return ArrayUtils.get((Object[]) output, index);
        }
        throw new ToolboxRuntimeException("Can not use DefaultOutputSplit to split outputs,outputs class is not List/Array");
    }

    /**
     * 获取List集合目标index开始size个元素
     *
     * @param output        目标对象
     * @param index         目标位置
     * @param size          数量
     * @param typeReference 集合类型
     * @param <R>           集合类型
     * @return 集合
     */
    default <R extends Collection<?>> R multiGetList(R output, int index, int size, TypeReference<R> typeReference) {
        Object[] arrayResult = new Object[size];
        for (int i = 0; i < size; i++) {
            arrayResult[i] = get(true, false, output, index + i);
        }
        return TypeUtils.caseToObject(arrayResult, typeReference);
    }

    /**
     * 获取数组目标index开始size个元素
     *
     * @param output      目标对象
     * @param index       目标位置
     * @param size        数量
     * @param elementType 元素类型
     * @param <R>         集合类型
     * @return 集合
     */
    @SuppressWarnings("unchecked")
    default <R> R[] multiGetArray(R[] output, int index, int size, Class<R> elementType) {
        R[] arrayResult = (R[]) Array.newInstance(elementType, size);
        for (int i = 0; i < size; i++) {
            arrayResult[i] = (R) get(false, true, output, index + i);
        }
        return arrayResult;
    }

}
