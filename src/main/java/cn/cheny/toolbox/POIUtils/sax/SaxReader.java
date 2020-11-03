package cn.cheny.toolbox.POIUtils.sax;

import cn.cheny.toolbox.other.hex.AlphabetUtils;
import lombok.Data;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.ss.usermodel.BuiltinFormats;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.util.XMLHelper;
import org.apache.poi.xssf.eventusermodel.XSSFReader;
import org.apache.poi.xssf.model.SharedStringsTable;
import org.apache.poi.xssf.model.StylesTable;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFRichTextString;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * SAX实现excel读
 * 注意行号列号都从0开始算起
 * 读取到的数据只会格式化为字符串或者数字
 *
 * @author cheney
 * @date 2020-10-19
 */
public class SaxReader {

    /**
     * 读函数
     */
    private RowDataReader reader;

    /**
     * 当读函数不存在时，所有数据的缓存
     */
    private List<List<Object>> allData;

    /**
     * sheet页统计信息
     */
    private List<SheetCount> sheetCounts;

    /**
     * SAX内容处理器
     */
    private SheetHandler sheetHandler;

    public SaxReader() {
    }

    public SaxReader(RowDataReader rowDataReader) {
        this.reader = rowDataReader;
    }

    /**
     * 处理excel输入流，读取数据
     *
     * @param inputStream excel输入流
     * @param sheetNum    sheet页码（从0开始）
     * @throws Exception 异常
     */
    public void processSheet(InputStream inputStream, Integer sheetNum) throws Exception {
        OPCPackage pkg = OPCPackage.open(inputStream);
        processSheet(pkg, sheetNum);
    }

    /**
     * 处理excel文件，读取数据
     *
     * @param file     excel文件
     * @param sheetNum sheet页码（从0开始）
     * @throws Exception 异常
     */
    public void processSheet(File file, Integer sheetNum) throws Exception {
        OPCPackage pkg = OPCPackage.open(file);
        processSheet(pkg, sheetNum);
    }

    /**
     * 处理excel输入流，读取所有sheet数据
     *
     * @param inputStream excel输入流
     * @throws Exception 异常
     */
    public void processAllSheets(InputStream inputStream) throws Exception {
        OPCPackage pkg = OPCPackage.open(inputStream);
        processSheet(pkg, null);
    }

    /**
     * 处理excel文件，读取所有sheet数据
     *
     * @param file excel文件
     * @throws Exception 异常
     */
    public void processAllSheets(File file) throws Exception {
        OPCPackage pkg = OPCPackage.open(file);
        processSheet(pkg, null);
    }

    private void processSheet(OPCPackage pkg, Integer sheetNum) throws Exception {
        init();
        XSSFReader r = new XSSFReader(pkg);
        SharedStringsTable sst = r.getSharedStringsTable();
        StylesTable stylesTable = r.getStylesTable();
        XMLReader parser = fetchSheetParser(sst, stylesTable);
        Iterator<InputStream> sheets = r.getSheetsData();
        for (int i = 0; sheets.hasNext(); i++) {
            InputStream sheet = sheets.next();
            if (sheetNum != null) {
                if (i < sheetNum) {
                    sheet.close();
                    continue;
                } else if (i > sheetNum) {
                    sheet.close();
                    break;
                }
            }
            InputSource sheetSource = new InputSource(sheet);
            prepareNext();
            parser.parse(sheetSource);
            sheet.close();
        }
    }

    private XMLReader fetchSheetParser(SharedStringsTable sst, StylesTable stylesTable) throws SAXException, ParserConfigurationException {
        XMLReader parser = XMLHelper.newXMLReader();
        SheetHandler handler = new SheetHandler(sst, stylesTable);
        parser.setContentHandler(handler);
        this.sheetHandler = handler;
        return parser;
    }

    public RowDataReader getReader() {
        return reader;
    }

    public void setReader(RowDataReader reader) {
        this.reader = reader;
    }

    public List<List<Object>> getAllData() {
        return allData;
    }

    public void setAllData(List<List<Object>> allData) {
        this.allData = allData;
    }

    public List<SheetCount> getSheetCounts() {
        return sheetCounts;
    }

    public SheetCount getFirstSheetCount() {
        return sheetCounts != null && sheetCounts.size() > 0 ?
                sheetCounts.get(0) : null;
    }

    private void init() {
        this.sheetCounts = new ArrayList<>();
        this.allData = null;
    }

    private void prepareNext() {
        SheetCount count = new SheetCount();
        this.sheetCounts.add(count);
        this.sheetHandler.prepareNext(count);
    }

    /**
     * sheet页统计信息
     */
    @Data
    public static class SheetCount {
        /**
         * 最大列号
         */
        private int maxCol;

        /**
         * 最大列编码
         */
        private String maxColReference;

        /**
         * 最大行号
         */
        private int maxRow;
    }

    /**
     * See org.xml.sax.helpers.DefaultHandler javadocs
     */
    private class SheetHandler extends DefaultHandler {
        // excel单元格reference的解析正则表达式：组1为列字母，组2为行数
        private final Pattern rowNumPattern = Pattern.compile("([A-Z]+)([0-9]+)");
        private SharedStringsTable sst;
        private StylesTable stylesTable;
        private DataFormatter dataFormatter = new DataFormatter();
        private String dataFormatString;
        private Short formatIndex;
        private String lastContents;
        private CellType nextType;
        private int currentRow;
        private int currentCol;
        private boolean cacheAll;
        private List<Object> data;
        private List<List<Object>> allData;
        private SheetCount sheetCount;
        // 控制空单元格
        private int c;

        private SheetHandler(SharedStringsTable sst, StylesTable stylesTable) {
            this.sst = sst;
            this.currentRow = 0;
            this.currentCol = -1;
            this.stylesTable = stylesTable;
            this.cacheAll = reader == null;
            this.c = 0;
            data = new ArrayList<>();
            if (this.cacheAll) {
                SaxReader.this.allData = this.allData = new ArrayList<>();
            }
        }

        public void startElement(String uri, String localName, String name,
                                 Attributes attributes) {
            // c => cell
            if (name.equals("c")) {
                c++;
                // Print the cell reference
                String reference = attributes.getValue("r");
                Matcher matcher = rowNumPattern.matcher(reference);
                matcher.find();
                String colStr = matcher.group(1);
                String rowStr = matcher.group(2);
                // column num(从0开始算)
                int col = AlphabetUtils.toDec(colStr) - 1;
                // 补充空列
                for (int nullCol = currentCol + 1; nullCol < col; nullCol++) {
                    data.add(null);
                }
                currentCol = col;
                // 记录最大列
                if (col > sheetCount.maxCol) {
                    sheetCount.maxCol = col;
                    sheetCount.maxColReference = reference;
                }
                // row num(从0开始算)
                int row = Integer.parseInt(rowStr) - 1;
                if (currentRow != row) {
                    currentRow = row;
                }
                setNextDataType(attributes);
            }
            // Clear contents cache
            lastContents = "";
        }

        public void endElement(String uri, String localName, String name) {
            // v => contents of a cell
            if (name.equals("v")) {
                if (c > 1) {
                    // c大于1，说明存在空单元格
                    for (int i = 0; i < c - 1; i++) {
                        data.add(null);
                    }
                }
                Object nextValue = getNextValue();
                data.add(nextValue);
                c = 0;
            } else if (name.equals("row")) {
                sheetCount.maxRow = currentRow;
                if (cacheAll) {
                    allData.add(data);
                } else {
                    SaxReader.this.reader.read(currentRow, data);
                }
                currentRow++;
                currentCol = -1;
                c = 0;
                data = new ArrayList<>();
            }
        }

        public void characters(char[] ch, int start, int length) {
            lastContents += new String(ch, start, length);
        }

        private void setNextDataType(Attributes attributes) {
            Short formatIndex = null;
            String dataFormatString = null;
            CellType nextType = null;
            // Figure out if the value is an index in the SST
            String cellType = attributes.getValue("t");
            if (cellType == null) {
                nextType = CellType.NUMBER;
            } else if (cellType.equals("e")) {
                nextType = CellType.ERROR;
            } else if (cellType.equals("b")) {
                nextType = CellType.BOOLE;
            } else if ("str".equals(cellType)) {
                nextType = CellType.FORMULA;
            } else if ("inlineStr".equals(cellType)) {
                nextType = CellType.INLINESTR;
            } else {
                // 其余都当作String处理
                nextType = CellType.STRING;
            }
            String cellStyleStr = attributes.getValue("s");
            if (cellStyleStr != null) {
                int styleIndex = Integer.parseInt(cellStyleStr);
                XSSFCellStyle style = stylesTable.getStyleAt(styleIndex);
                formatIndex = style.getDataFormat();
                dataFormatString = style.getDataFormatString();
                if (dataFormatString == null) {
                    dataFormatString = BuiltinFormats.getBuiltinFormat(formatIndex);
                }
                if (dataFormatString != null &&
                        dataFormatString.contains("yy")) {
                    nextType = CellType.DATE;
                    dataFormatString = "yyyy-MM-dd HH:mm:ss";
                }
            }
            this.formatIndex = formatIndex;
            this.dataFormatString = dataFormatString;
            this.nextType = nextType;
        }

        public Object getNextValue() {
            switch (nextType) {
                case STRING: {
                    try {
                        int idx = Integer.parseInt(lastContents);
                        return sst.getItemAt(idx).getString();
                    } catch (NumberFormatException e) {
                        return lastContents;
                    }
                }
                case NUMBER: {
                    return parseNumber(lastContents);
                }
                case DATE: {
                    return dataFormatter.formatRawCellContents(Double.parseDouble(lastContents), formatIndex, dataFormatString);
                }
                case BOOLE: {
                    char first = lastContents.charAt(0);
                    return first != '0' ? "true" : "false";
                }
                case ERROR: {
                    return "\"ERROR:" + lastContents + "\"";
                }
                case INLINESTR: {
                    return new XSSFRichTextString(lastContents).toString();
                }
                default: {
                    return lastContents;
                }
            }

        }

        /**
         * 处理科学计数法
         */
        private Object parseNumber(String val) {
            val = val.contains(".") ?
                    new BigDecimal(val).toPlainString() :
                    val;
            if (val.contains(".")) {
                return Double.parseDouble(val);
            }
            Object number;
            try {
                number = Integer.parseInt(val);
            } catch (NumberFormatException e) {
                try {
                    number = Long.parseLong(val);
                } catch (NumberFormatException e2) {
                    number = val;
                }
            }
            return number;
        }

        /**
         * 为处理下一个sheet做准备
         *
         * @param count sheet统计实体
         */
        public void prepareNext(SheetCount count) {
            this.sheetCount = count;
            this.currentRow = 0;
            this.currentCol = -1;
            this.c = 0;
            this.data = new ArrayList<>();
        }
    }

    /**
     * cell类型枚举
     */
    private enum CellType {
        NUMBER,
        STRING,
        DATE,
        BOOLE,
        ERROR,
        FORMULA,
        INLINESTR
    }

    @FunctionalInterface
    public interface RowDataReader {
        void read(int rowNum, List<Object> data);
    }

}
