package cn.cheny.toolbox.POIUtils.entity;

import lombok.Data;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import java.util.List;
import java.util.Map;

/**
 * excel读取结果
 *
 * @author cheney
 * @date 2019-12-19
 */
@Data
public class ReadResult<T> {

    // 读取结果数据
    private List<T> data;
    // 读取结果数据
    private Map<Integer, T> dataIndexMap;

    // poi Workbook
    private Workbook workbook;

    // poi sheet
    private Sheet sheet;

    // 读取的标题总列数
    private int titleCount;

    // 标题行号
    private int titleRowNum;

    // 数据类型
    private Class<T> dataClass;

    // excel读取信息
    private ExcelReadInfo excelReadInfo;

    private boolean readAsMap;

    public ReadResult(List<T> data, Map<Integer, T> dataIndexMap,
                      Workbook workbook, Sheet sheet, int titleCount,
                      int titleRowNum, Class<T> dataClass) {
        this.data = data;
        this.workbook = workbook;
        this.sheet = sheet;
        this.dataIndexMap = dataIndexMap;
        this.titleCount = titleCount;
        this.titleRowNum = titleRowNum;
        this.dataClass = dataClass;
        this.readAsMap = false;
    }

    @SuppressWarnings("unchecked")
    public ReadResult(List<Map<String, Object>> data, Map<Integer, Map<String, Object>> dataIndexMap,
                      Workbook workbook, Sheet sheet, int titleCount,
                      int titleRowNum, ExcelReadInfo excelReadInfo) {
        this.data = (List<T>) data;
        this.workbook = workbook;
        this.sheet = sheet;
        this.dataIndexMap = (Map<Integer, T>) dataIndexMap;
        this.titleCount = titleCount;
        this.titleRowNum = titleRowNum;
        this.excelReadInfo = excelReadInfo;
        this.readAsMap = true;
    }

}
