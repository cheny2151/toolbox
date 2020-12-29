package cn.cheny.toolbox.POIUtils.worker;

import cn.cheny.toolbox.POIUtils.annotation.ExcelData;
import cn.cheny.toolbox.POIUtils.annotation.ExcelHead;
import cn.cheny.toolbox.POIUtils.annotation.ExcelWriteBack;
import cn.cheny.toolbox.POIUtils.entity.ExcelReadInfo;
import cn.cheny.toolbox.POIUtils.entity.ReadProperty;
import cn.cheny.toolbox.POIUtils.entity.ReadResult;
import cn.cheny.toolbox.POIUtils.exception.WorkBookReadException;
import cn.cheny.toolbox.POIUtils.utils.CellDealFunction;
import cn.cheny.toolbox.POIUtils.utils.CellDealUtils;
import cn.cheny.toolbox.reflect.ReflectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

/**
 * excel表读取者
 */
public class WorkBookReader {

    // 解析为Map时预存的行号
    public final static String ROW_NUM_FIELD_AT_MAP = "_rowNum";

    public final static CellDealFunction DEFAULT_CELL_DEAL_FUNCTION = new CellDealUtils();

    /**
     * 文件excel数据流入口
     *
     * @param fileName    原始文件名
     * @param inputStream 输入流
     * @param targetClass 目标类型
     * @param <T>         目标类型泛型
     * @return 读取结果
     * @throws WorkBookReadException
     */
    public <T> ReadResult<T> read(String fileName, InputStream inputStream, Class<T> targetClass) {
        try {
            Workbook workbook;
            if (fileName.contains("xlsx")) {
                workbook = new XSSFWorkbook(inputStream);
            } else {
                workbook = new HSSFWorkbook(inputStream);
            }
            return read(workbook, targetClass);
        } catch (Exception e) {
            throw new WorkBookReadException("excel解析失败" + e.getMessage(), e);
        }
    }

    public ReadResult<Map<String, Object>> readAsMap(String fileName, InputStream inputStream, ExcelReadInfo excelInfo) {
        try {
            Workbook workbook;
            if (fileName.contains("xlsx")) {
                workbook = new XSSFWorkbook(inputStream);
            } else {
                workbook = new HSSFWorkbook(inputStream);
            }
            Sheet sheet = getSheet(workbook, excelInfo.getSheetName());
            //数据出现行数,从0开始算
            int titleRowNum = excelInfo.getTitleRow();
            int startRowNumber = titleRowNum + 1;
            CellDealFunction cellDealFunction = excelInfo.getCellDealFunction() == null ?
                    DEFAULT_CELL_DEAL_FUNCTION : excelInfo.getCellDealFunction();
            // 分析excel列表title
            Row titleRow = sheet.getRow(titleRowNum);
            Map<Integer, String> titleMap = new HashMap<>();
            titleRow.forEach(cell -> {
                String title = cellDealFunction.dealTitle("col_" + cell.getAddress().getColumn(), cell.getStringCellValue());
                titleMap.put(cell.getColumnIndex(), title);
            });
            List<Map<String, Object>> results = new ArrayList<>();
            Map<Integer, Map<String, Object>> resultWithRow = new HashMap<>();
            int lastRowNum = excelInfo.getEndRow() == null ? sheet.getLastRowNum() : excelInfo.getEndRow();
            ExcelReadInfo.CellStopFunction cellStopFunction = excelInfo.getCellStopFunction();
            for (int i = startRowNumber; i <= lastRowNum; i++) {
                Row row = sheet.getRow(i);
                if (cellStopFunction != null && cellStopFunction.stop(row)) {
                    break;
                }
                Map<String, Object> rowData = new HashMap<>();
                row.forEach(cell -> {
                    rowData.put(titleMap.get(cell.getColumnIndex()), getCellValue(cell, cellDealFunction));
                });
                rowData.put(ROW_NUM_FIELD_AT_MAP, i);
                results.add(rowData);
                resultWithRow.put(i, rowData);
            }
            return new ReadResult<>(results, resultWithRow, workbook, sheet, titleRow.getLastCellNum() - 1, titleRowNum, excelInfo);
        } catch (Exception e) {
            throw new WorkBookReadException("excel解析失败" + e.getMessage(), e);
        }
    }


    /**
     * 从workbook读取数据
     *
     * @param workbook    poi Workbook
     * @param targetClass 目标类型
     */
    private <T> ReadResult<T> read(Workbook workbook, Class<T> targetClass) throws IllegalAccessException, InvocationTargetException {
        //数据出现行数,从0开始算
        ExcelHead excelHead = targetClass.getAnnotation(ExcelHead.class);
        int titleRowNum = 0;
        int lastRowNum;
        String sheetName = null;
        if (excelHead != null) {
            titleRowNum = excelHead.titleRow();
            sheetName = excelHead.sheetName();
            lastRowNum = excelHead.endRow();
        } else {
            lastRowNum = -1;
        }
        Sheet sheet = getSheet(workbook, sheetName);

        int startRowNumber = titleRowNum + 1;
        // 分析excel列表映射字段信息
        Row titleRow = sheet.getRow(titleRowNum);
        Map<Integer, ReadProperty> readPropertyMap = analysisAnnotation(targetClass, titleRow);
        List<T> results = new ArrayList<>();
        Map<Integer, T> resultWithRow = new HashMap<>();
        // lastRowNum==-1,则无指定结尾行
        lastRowNum = lastRowNum == -1 ? sheet.getLastRowNum() : lastRowNum;
        for (int i = startRowNumber; i <= lastRowNum; i++) {
            T t = createTarget(targetClass, readPropertyMap, sheet.getRow(i));
            results.add(t);
            resultWithRow.put(i, t);
        }
        return new ReadResult<>(results, resultWithRow, workbook, sheet, titleRow.getLastCellNum() - 1, titleRowNum, targetClass);
    }

    /**
     * 回写数据
     *
     * @param readResult 读取的数据结果实体
     * @param <T>        数据类型
     */
    @SuppressWarnings("unchecked")
    public <T> void writeBack(ReadResult<T> readResult) {
        if (readResult.isReadAsMap()) {
            writeBackIsMap((ReadResult<Map<String, Object>>) readResult);
        } else {
            writeBackNoMap(readResult);
        }
    }

    /**
     * 回写非Map类型数据
     *
     * @param readResult 读取的数据结果实体
     * @param <T>        数据类型
     */
    public <T> void writeBackNoMap(ReadResult<T> readResult) {
        Sheet sheet = readResult.getSheet();
        Workbook workbook = readResult.getWorkbook();
        int titleCount = readResult.getTitleCount();
        int titleRowNum = readResult.getTitleRowNum();
        Class<T> dataClass = readResult.getDataClass();
        // 获取负责回写的注解
        Map<String, Field> fieldMap = ReflectUtils.getAllFieldHasAnnotation(dataClass, ExcelWriteBack.class);
        if (fieldMap == null || fieldMap.size() == 0) {
            throw new IllegalArgumentException("write back excel,but not find annotation @ExcelWriteBack");
        }
        // 构造注解与读方法的映射
        Map<ExcelWriteBack, Method> backMethodMap =
                fieldMap.values().stream().collect(Collectors.toMap(
                        field -> field.getDeclaredAnnotation(ExcelWriteBack.class),
                        field -> ReflectUtils.getReadMethod(dataClass, field.getName())
                ));
        ArrayList<Method> readMethods = new ArrayList<>();
        // 写入标题
        List<ExcelWriteBack> excelWriteBacks = backMethodMap.keySet().stream().sorted(Comparator.comparingInt(ExcelWriteBack::sort)).collect(Collectors.toList());
        int currentColumnNum = titleCount + 1;
        for (ExcelWriteBack excelWriteBack : excelWriteBacks) {
            String title = excelWriteBack.title();
            HSSFColor.HSSFColorPredefined color = excelWriteBack.titleColor();
            Cell cell = sheet.getRow(titleRowNum).createCell(currentColumnNum++);
            cell.setCellValue(title);
            setFontColor(workbook, color, cell);
            readMethods.add(backMethodMap.get(excelWriteBack));
        }
        // 写数据
        int size = excelWriteBacks.size();
        Map<Integer, T> dataIndexMap = readResult.getDataIndexMap();
        dataIndexMap.forEach((index, value) -> {
            int _currentColumnNum = titleCount + 1;
            for (int i = 0; i < size; i++) {
                ExcelWriteBack excelWriteBack = excelWriteBacks.get(i);
                HSSFColor.HSSFColorPredefined color = excelWriteBack.color();
                Object cellValue = ReflectUtils.readValue(value, readMethods.get(i));
                if (cellValue != null) {
                    Cell cell = sheet.getRow(index).createCell(_currentColumnNum++);
                    cell.setCellValue(cellValue.toString());
                    setFontColor(workbook, color, cell);
                }
            }
        });
    }

    /**
     * 回写Map类型数据
     *
     * @param readResult 读取的Map类型数据结果实体
     */
    public void writeBackIsMap(ReadResult<Map<String, Object>> readResult) {
        Sheet sheet = readResult.getSheet();
        Workbook workbook = readResult.getWorkbook();
        int titleCount = readResult.getTitleCount();
        int titleRowNum = readResult.getTitleRowNum();
        ExcelReadInfo excelReadInfo = readResult.getExcelReadInfo();
        // 负责回写的字段
        List<String> writeBackKeys = excelReadInfo.getWriteBackKeys();
        HSSFColor.HSSFColorPredefined color = excelReadInfo.getWriteBackColumnColor();
        // 写数据
        Map<Integer, Map<String, Object>> dataIndexMap = readResult.getDataIndexMap();
        writeSheet(workbook, sheet, dataIndexMap, writeBackKeys, color, titleRowNum, titleCount);
    }

    /**
     * 根据每一行创建一个实体
     *
     * @param targetClass     目标class
     * @param readPropertyMap 字段信息集合
     * @param row             行
     * @param <T>             目标泛型
     * @return
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     */
    private <T> T createTarget(Class<T> targetClass, Map<Integer, ReadProperty> readPropertyMap, Row row)
            throws IllegalAccessException, InvocationTargetException {
        T t = ReflectUtils.newObject(targetClass, null, null);
        for (Map.Entry<Integer, ReadProperty> entry : readPropertyMap.entrySet()) {
            ReadProperty readProperty = entry.getValue();
            Cell cell = row.getCell(entry.getKey());
            if (cell == null) {
                continue;
            }
            Object value = getCellValue(cell, DEFAULT_CELL_DEAL_FUNCTION);
            readProperty.writerUnknownTypeValue(t, value);
        }
        return t;
    }

    private Sheet getSheet(Workbook workbook, String sheetName) {
        if (sheetName == null || "".equals(sheetName)) {
            return workbook.getSheetAt(0);
        } else {
            Sheet sheet = workbook.getSheet(sheetName);
            return sheet != null ? sheet : getSheet(workbook, null);
        }
    }

    /**
     * 获取单元格数据(只处理Number和String)
     * Date属于Number
     *
     * @param cell             单元格实体
     * @param cellDealFunction 单元格数据处理函数
     * @return 单元格数据
     */
    private Object getCellValue(Cell cell, CellDealFunction cellDealFunction) {
        Object value;
        switch (cell.getCellType()) {
            case STRING: {
                value = cellDealFunction.dealVal(cell.getStringCellValue());
                break;
            }
            case NUMERIC: {
                if (DateUtil.isCellDateFormatted(cell)) {
                    value = cell.getDateCellValue();
                } else {
                    value = cell.getNumericCellValue();
                }
                break;
            }
            default: {
                value = null;
            }
        }
        return value;
    }


    private Workbook judgeWorkBook(File file) throws IOException {
        if (file.getName().contains("xlsx")) {
            return new XSSFWorkbook(new FileInputStream(file));
        } else {
            return new HSSFWorkbook(new FileInputStream(file));
        }
    }

    /**
     * 解析注解，获取列对应field的map
     *
     * @param targetClass 目标类型
     * @param titleRow    标题行
     * @return 解析结果
     */
    public <T> Map<Integer, ReadProperty> analysisAnnotation(Class<T> targetClass, Row titleRow) throws WorkBookReadException {
        //遍历所有含有@ExcelData的字段
        Map<Integer, ReadProperty> readPropertyMap = new HashMap<>();
        Map<String, Integer> titleMap = new HashMap<>();
        titleRow.forEach(cell -> {
            String title = cell.getStringCellValue();
            if (StringUtils.isNotBlank(title) && titleMap.containsKey(title)) {
                throw new WorkBookReadException("duplicate title value:" + title);
            }
            titleMap.put(title, cell.getColumnIndex());
        });
        for (Field field : ReflectUtils.getAllFields(targetClass, Object.class)) {
            ExcelData annotation = field.getAnnotation(ExcelData.class);
            if (annotation != null) {
                ExcelData.SwitchType type = annotation.type();
                switch (type) {
                    case COLUMN_NUM: {
                        //存放字段信息
                        readPropertyMap.put(annotation.column(),
                                new ReadProperty(field.getName(),
                                        field.getType(), field,
                                        ReflectUtils.getWriterMethod(targetClass, field.getName(), field.getType())
                                ));
                        break;
                    }
                    case COLUMN_TITLE: {
                        Integer index = titleMap.get(annotation.columnTitle());
                        if (index != null) {
                            readPropertyMap.put(index,
                                    new ReadProperty(field.getName(),
                                            field.getType(), field,
                                            ReflectUtils.getWriterMethod(targetClass, field.getName(), field.getType())
                                    ));
                        }
                    }
                }
            }
        }
        if (readPropertyMap.size() < 1) {
            throw new WorkBookReadException("未发现该类的@ExcelData注解");
        }
        return readPropertyMap;
    }

    /**
     * 设置字体颜色
     */
    private static void setFontColor(Workbook workbook, HSSFColor.HSSFColorPredefined color, Cell cell) {
        CellStyle cellStyle = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setColor(color.getIndex());
        cellStyle.setFont(font);
        cell.setCellStyle(cellStyle);
    }

    /**
     * 回写数据到sheet
     *
     * @param workbook      写数据的excel
     * @param sheet         写数据的sheet
     * @param writeBackData 写入的数据,key为行号
     * @param writeBackKeys 写数据的keys
     * @param titleColor    标题颜色
     * @param titleRowNum   写入标题的行号
     * @param titleColNum   写入标题的列号
     */
    public static <T> void writeSheet(Workbook workbook, Sheet sheet,
                                      Map<Integer, Map<String, T>> writeBackData,
                                      List<String> writeBackKeys,
                                      HSSFColor.HSSFColorPredefined titleColor,
                                      int titleRowNum, int titleColNum) {
        // 写入标题
        int currentColumnNum = titleColNum;
        for (String title : writeBackKeys) {
            Cell cell = sheet.getRow(titleRowNum).createCell(currentColumnNum++);
            cell.setCellValue(title);
            setFontColor(workbook, titleColor, cell);
        }
        // 写数据
        writeBackData.forEach((index, value) -> {
            int _currentColumnNum = titleColNum;
            for (String key : writeBackKeys) {
                Object cellValue = value.get(key);
                if (cellValue != null) {
                    Cell cell = sheet.getRow(index).createCell(_currentColumnNum++);
                    cell.setCellValue(cellValue.toString());
                }
            }
        });
    }

}
