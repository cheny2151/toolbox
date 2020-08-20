package cn.cheny.toolbox.POIUtils.utils;

/**
 * poi 单元格值处理函数
 *
 * @author cheney
 * @date 2020-07-20
 */
public interface CellDealFunction {

    /**
     * 执行值处理
     *
     * @param cellVal 单元格值
     * @return 处理结果值
     */
    Object dealVal(Object cellVal);

    String dealTitle(Object cellVal);

}
