package cn.cheny.toolbox.POIUtils.entity;

import cn.cheny.toolbox.POIUtils.utils.CellDealFunction;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.Row;

import java.util.List;

/**
 * excel读取信息
 *
 * @author cheney
 * @date 2019-12-25
 */
@Data
@AllArgsConstructor
public class ExcelReadInfo {
    private String sheetName;
    private int titleRow;
    private Integer endRow;
    private List<String> writeBackKeys;
    private HSSFColor.HSSFColorPredefined writeBackColumnColor;
    private CellStopFunction cellStopFunction;
    private CellDealFunction cellDealFunction;

    public static ExcelReadInfo readInfo() {
        return readInfo(null, null, null, null, null);
    }

    public static ExcelReadInfo readInfo(String sheetName, Integer titleRow, Integer endRow,
                                         CellStopFunction cellStopFunction, CellDealFunction cellDealFunction) {
        titleRow = titleRow == null ? 0 : titleRow;
        return new ExcelReadInfo(sheetName, titleRow, endRow, null, null, cellStopFunction, cellDealFunction);
    }

    public static ExcelReadInfo withWriteBack(String sheetName, Integer titleRow,
                                              Integer endRow, CellStopFunction cellStopFunction,
                                              List<String> writeBackKeys,
                                              HSSFColor.HSSFColorPredefined writeBackColumnColor) {
        return withWriteBack(sheetName, titleRow, endRow, cellStopFunction, writeBackKeys, writeBackColumnColor, null);
    }

    public static ExcelReadInfo withWriteBack(String sheetName, Integer titleRow,
                                              Integer endRow, CellStopFunction cellStopFunction,
                                              List<String> writeBackKeys,
                                              HSSFColor.HSSFColorPredefined writeBackColumnColor,
                                              CellDealFunction cellDealFunction) {
        if (CollectionUtils.isEmpty(writeBackKeys)) {
            throw new IllegalArgumentException("writeBackKeys can not be empty");
        }
        writeBackColumnColor = writeBackColumnColor == null ? HSSFColor.HSSFColorPredefined.BLACK : writeBackColumnColor;
        ExcelReadInfo excelReadInfo = readInfo(sheetName, titleRow, endRow, cellStopFunction, cellDealFunction);
        excelReadInfo.setWriteBackKeys(writeBackKeys);
        excelReadInfo.setWriteBackColumnColor(writeBackColumnColor);
        return excelReadInfo;
    }

    public static ExcelReadInfo withWriteBack(List<String> writeBackKeys,
                                              HSSFColor.HSSFColorPredefined writeBackColumnColor) {
        return withWriteBack(null, null, null, null, writeBackKeys, writeBackColumnColor);
    }

    @FunctionalInterface
    public interface CellStopFunction {
        boolean stop(Row row);
    }
}
