package cn.cheny.toolbox.POIUtils;

import cn.cheny.toolbox.POIUtils.entity.ExcelReadInfo;
import cn.cheny.toolbox.POIUtils.entity.ReadResult;
import cn.cheny.toolbox.POIUtils.exception.WorkBookReadException;
import cn.cheny.toolbox.POIUtils.worker.HSSFWorkbookBuilder;
import cn.cheny.toolbox.POIUtils.worker.WorkBookReader;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Workbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * create by cheny
 * V1.5
 */
public class PoiUtils {

    /**
     * 单例模式
     */
    private static class HSSFWorkbookBuilderHolder {
        private final static HSSFWorkbookBuilder HSSF_WORKBOOK_BUILDERK_BUILDER = new HSSFWorkbookBuilder();
    }

    /**
     * 单例模式
     */
    private static class WorkbookReaderHolder {
        private final static WorkBookReader WORKBOOK_READER = new WorkBookReader();
    }

    public static WorkBookReader getWorkBookReader() {
        return WorkbookReaderHolder.WORKBOOK_READER;
    }

    private static HSSFWorkbookBuilder getHSSFWorkbookBuilder() {
        return HSSFWorkbookBuilderHolder.HSSF_WORKBOOK_BUILDERK_BUILDER;
    }

    /**
     * 创建一张含有数据的表
     *
     * @param data 数据
     * @return
     */
    public static HSSFWorkbook createSheet(List<?> data) {
        return getHSSFWorkbookBuilder().createSheet(data);
    }

    /**
     * 创建一张表头
     *
     * @param targetClass 目标类型
     * @return
     */
    public static HSSFWorkbook createEmptySheet(Class<?> targetClass) {
        return getHSSFWorkbookBuilder().createHead(targetClass);
    }

    /**
     * 读取数据
     *
     * @param file        文件
     * @param targetClass 目标类型
     * @param <T>         类型
     * @return
     */
    public static <T> ReadResult<T> readFormFile(File file, Class<T> targetClass) {
        try {
            return getWorkBookReader().read(file.getName(), new FileInputStream(file), targetClass);
        } catch (FileNotFoundException e) {
            throw new WorkBookReadException("excel解析失败" + e.getMessage(), e);
        }
    }

    /**
     * 读取数据
     *
     * @param fileName    文件名称(用作判断为xls/xlsx)
     * @param inputStream 输入流
     * @param targetClass 目标类型
     * @param <T>         类型
     * @return
     */
    public static <T> ReadResult<T> readForStream(String fileName, InputStream inputStream, Class<T> targetClass) {
        return getWorkBookReader().read(fileName, inputStream, targetClass);
    }

    /**
     * 读取数据
     *
     * @param file        文件
     * @param targetClass 目标类型
     * @param <T>         类型
     * @return
     */
    public static ReadResult<Map<String, Object>> readAsMap(File file, ExcelReadInfo excelReadInfo) {
        try {
            return getWorkBookReader().readAsMap(file.getName(), new FileInputStream(file), excelReadInfo);
        } catch (FileNotFoundException e) {
            throw new WorkBookReadException("excel解析失败" + e.getMessage(), e);
        }
    }

    /**
     * 读取数据
     *
     * @param fileName    文件名称(用作判断为xls/xlsx)
     * @param inputStream 输入流
     * @param targetClass 目标类型
     * @param <T>         类型
     * @return
     */
    public static ReadResult<Map<String, Object>> readAsMap(String fileName, InputStream inputStream, ExcelReadInfo excelReadInfo) {
        return getWorkBookReader().readAsMap(fileName, inputStream, excelReadInfo);
    }

    /**
     * 回写数据
     *
     * @param readResult 读取的结果实体
     * @param <T>        数据类型
     */
    public static <T> Workbook writeBack(ReadResult<T> readResult) {
        getWorkBookReader().writeBack(readResult);
        return readResult.getWorkbook();
    }

}
