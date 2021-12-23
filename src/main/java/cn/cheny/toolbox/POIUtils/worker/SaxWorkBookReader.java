package cn.cheny.toolbox.POIUtils.worker;

import cn.cheny.toolbox.POIUtils.entity.SaxReadInfo;
import cn.cheny.toolbox.POIUtils.sax.SaxReader;
import cn.cheny.toolbox.POIUtils.utils.CellDealFunction;
import lombok.SneakyThrows;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.util.*;

/**
 * 基于{@link SaxReader}
 * SAX模式读取excel数据，并进行封装
 *
 * @author cheney
 * @date 2020-10-21
 */
public class SaxWorkBookReader {

    private final SaxReader saxReader;

    private SaxReadInfo saxReadInfo;

    private RaxReadResult saxReadResult;

    public SaxWorkBookReader(SaxReadInfo saxReadInfo) {
        this.saxReader = new SaxReader();
        this.saxReadInfo = saxReadInfo;
    }

    public void readAndConsume(InputStream inputStream, DataConsumer dataConsumer) {
        readAndConsume((Object) inputStream, dataConsumer);
    }

    public void readAndConsume(File file, DataConsumer dataConsumer) {
        readAndConsume((Object) file, dataConsumer);
    }

    @SneakyThrows
    private void readAndConsume(Object excel, DataConsumer dataConsumer) {
        // 初始化读取结果
        saxReadResult = new RaxReadResult(saxReadInfo);
        // 设置reader
        SaxReader.RowDataReader rowDataReader = mapDataReader(dataConsumer);
        saxReader.setReader(rowDataReader);
        // 执行读取
        if (excel instanceof InputStream) {
            ByteArrayOutputStream sourceStream = copyInputStream((InputStream) excel);
            saxReader.processSheet(new ByteArrayInputStream(sourceStream.toByteArray()),
                    saxReadInfo.getSheetNum());
            excel = sourceStream;
        } else {
            saxReader.processSheet((File) excel, saxReadInfo.getSheetNum());
        }
        // 设置结果
        SaxReader.SheetCount sheetCount = saxReader.getFirstSheetCount();
        saxReadResult.setTitleCount(sheetCount.getMaxCol());
        saxReadResult.setSource(excel);
    }

    /**
     * 回写Map类型数据
     *
     * @param outputStream 回写流
     * @return 是否进行了回写
     */
    public boolean writeBackIfNeed(OutputStream outputStream) throws IOException {
        Workbook workbook = createBookBySource(saxReadResult.getSource());
        Sheet sheet = workbook.getSheetAt(saxReadInfo.getSheetNum());
        int titleCount = saxReadResult.getTitleCount();
        int titleRowNum = saxReadResult.getTitleRowNum();
        // 负责回写的字段
        List<String> writeBackKeys = saxReadInfo.getWriteBackKeys();
        Map<Integer, Map<String, String>> dataIndexMap = saxReadResult.getWriteBackData();
        if (CollectionUtils.isEmpty(writeBackKeys)
                || dataIndexMap.size() == 0) {
            return false;
        }
        HSSFColor.HSSFColorPredefined color = saxReadInfo.getWriteBackColumnColor();
        // 写入标题,数据
        WorkBookReader.writeSheet(workbook, sheet, dataIndexMap, writeBackKeys, color,
                // titleCount是原有列最大值，回写时从+1列开始
                titleRowNum, titleCount + 1);
        workbook.write(outputStream);
        return true;
    }

    /**
     * 创建一个读取并组装Map的函数
     *
     * @param dataConsumer 用户消费任务
     * @return sax读取函数
     */
    private SaxReader.RowDataReader mapDataReader(DataConsumer dataConsumer) {
        List<String> titles = new ArrayList<>();
        int titleRow = saxReadInfo.getTitleRow();
        CellDealFunction cellDealFunction = saxReadInfo.getCellDealFunction();
        return (rowNum, data) -> {
            int dataSize = data.size();
            if (rowNum == titleRow) {
                for (int i = 0; i < dataSize; i++) {
                    titles.add(cellDealFunction.dealTitle("col_" + i, data.get(i)));
                }
            } else if (rowNum > titleRow) {
                int titleSize = titles.size();
                // 只处理标题之后的数据
                HashMap<String, Object> rowMap = new HashMap<>();
                for (int i = 0; i < dataSize && i < titleSize; i++) {
                    Object val = cellDealFunction.dealVal(data.get(i));
                    rowMap.put(titles.get(i), val);
                }
                // 执行用户消费任务
                dataConsumer.consume(rowMap, rowNum, saxReadResult);
            }
        };
    }

    @SneakyThrows
    private Workbook createBookBySource(Object source) {
        Workbook workbook;
        InputStream excel = !(source instanceof ByteArrayOutputStream) ?
                new FileInputStream((File) source) :
                new ByteArrayInputStream(((ByteArrayOutputStream) source).toByteArray());
        try {
            workbook = new XSSFWorkbook(excel);
        } catch (Exception e) {
            workbook = new HSSFWorkbook(excel);
        }
        return workbook;
    }

    /**
     * 复制input流，提供复用
     *
     * @param inputStream 输入流
     * @return 输出流
     * @throws IOException
     */
    private ByteArrayOutputStream copyInputStream(InputStream inputStream) throws IOException {
        BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);
        ByteArrayOutputStream arrayOutputStream = new ByteArrayOutputStream();
        int cacheLen = 1024;
        byte[] bytes = new byte[1024];
        int l;
        while ((l = bufferedInputStream.read(bytes, 0, cacheLen)) != -1) {
            arrayOutputStream.write(bytes, 0, l);
        }
        return arrayOutputStream;
    }

    public interface DataConsumer {
        void consume(Map<String, Object> data, int row, RaxReadResult raxReadResult);
    }

    public void setSaxReadInfo(SaxReadInfo saxReadInfo) {
        this.saxReadInfo = saxReadInfo;
    }

}
