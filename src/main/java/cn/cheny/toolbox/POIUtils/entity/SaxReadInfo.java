package cn.cheny.toolbox.POIUtils.entity;

import cn.cheny.toolbox.POIUtils.utils.CellDealFunction;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.poi.hssf.util.HSSFColor;

import java.util.List;

/**
 * excel读取信息
 *
 * @author cheney
 * @date 2019-12-25
 */
@Data
@AllArgsConstructor
public class SaxReadInfo {

    private final static CellDealFunction DEFAULT_CELL_DEAL_FUNCTION = new CellDealFunction.DefaultCellDealFunction();

    // sheet页（注意是从0开始）
    private int sheetNum;
    // 标题所在行（注意是从0开始）
    private int titleRow;
    private List<String> writeBackKeys;
    private HSSFColor.HSSFColorPredefined writeBackColumnColor;
    private CellDealFunction cellDealFunction;

    public static SaxReadInfo readInfo() {
        return readInfo(0, 0, null);
    }

    public static SaxReadInfo readInfo(int sheetNum, int titleRow,
                                       CellDealFunction cellDealFunction) {
        if (cellDealFunction == null) {
            cellDealFunction = DEFAULT_CELL_DEAL_FUNCTION;
        }
        return new SaxReadInfo(sheetNum, titleRow, null, null, cellDealFunction);
    }

    public static SaxReadInfo withWriteBack(List<String> writeBackKeys,
                                            HSSFColor.HSSFColorPredefined writeBackColumnColor) {
        return withWriteBack(0, 0, writeBackKeys, writeBackColumnColor, null);
    }

    public static SaxReadInfo withWriteBack(int sheetNum, int titleRow,
                                            List<String> writeBackKeys,
                                            HSSFColor.HSSFColorPredefined writeBackColumnColor) {
        return withWriteBack(sheetNum, titleRow, writeBackKeys, writeBackColumnColor, null);
    }

    public static SaxReadInfo withWriteBack(int sheetNum, int titleRow,
                                            List<String> writeBackKeys,
                                            HSSFColor.HSSFColorPredefined writeBackColumnColor,
                                            CellDealFunction cellDealFunction) {
        if (CollectionUtils.isEmpty(writeBackKeys)) {
            throw new IllegalArgumentException("writeBackKeys can not be empty");
        }
        writeBackColumnColor = writeBackColumnColor == null ? HSSFColor.HSSFColorPredefined.BLACK : writeBackColumnColor;
        SaxReadInfo excelReadInfo = readInfo(sheetNum, titleRow, cellDealFunction);
        excelReadInfo.setWriteBackKeys(writeBackKeys);
        excelReadInfo.setWriteBackColumnColor(writeBackColumnColor);
        return excelReadInfo;
    }

}
