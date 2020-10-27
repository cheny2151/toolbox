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

    /**
     * 标题处理
     *
     * @param reference 标题指向的单元格地址
     * @param cellVal   单元格值
     * @return 标题值
     */
    String dealTitle(String reference, Object cellVal);

    class DefaultCellDealFunction implements CellDealFunction {

        @Override
        public Object dealVal(Object cellVal) {
            return cellVal;
        }

        @Override
        public String dealTitle(String reference, Object cellVal) {
            return cellVal == null ? reference : cellVal.toString();
        }
    }

}
