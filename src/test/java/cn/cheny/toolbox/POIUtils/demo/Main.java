package cn.cheny.toolbox.POIUtils.demo;

import cn.cheny.toolbox.POIUtils.PoiUtils;
import cn.cheny.toolbox.POIUtils.annotation.ExcelHead;
import cn.cheny.toolbox.POIUtils.entity.ExcelReadInfo;
import cn.cheny.toolbox.POIUtils.entity.ReadResult;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Workbook;
import org.junit.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;

public class Main {

    public static final String FILE = "/Users/chenyi/Downloads/test.xls";

    public static void main(String[] args) {

        ArrayList<MemberVoPOI> list = new ArrayList<>();
        ArrayList<String> text = new ArrayList<>();
        text.add("asdf356s4");
        text.add("asdgdasg");
        text.add("是啊都就分了四个");
        for (int i = 0; i < 100; i++) {
            MemberVoPOI memberVoPOI = new MemberVoPOI();
            memberVoPOI.setPhone("136845354" + i);
            memberVoPOI.setName("Name" + i);
            memberVoPOI.setWechat("微信" + i);
            memberVoPOI.setAddress("地址地址" + i);
            list.add(memberVoPOI);
        }
        HSSFWorkbook workbook = PoiUtils.createSheet(list);
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(FILE);
            workbook.write(fileOutputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void test() {
        File file = new File(FILE);
        ReadResult<MemberVoPOI> readResult = PoiUtils.readFormFile(file, MemberVoPOI.class);
        for (MemberVoPOI memberVoPOI : readResult.getData()) {
            System.out.println(memberVoPOI);
        }
    }

    @Test
    public void test2() {
        HSSFWorkbook workbook = PoiUtils.createEmptySheet(MemberVoPOI.class);
        try {
            FileOutputStream fileOutputStream = new FileOutputStream("C://360Downloads//test.xls");
            workbook.write(fileOutputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void test3() {
        ExcelHead annotation = MemberVoPOI.class.getAnnotation(ExcelHead.class);
        System.out.println(annotation.headTitle());
    }

    @Test
    public void test4() throws IOException {
        File file = new File("D://test.xlsx");
        ReadResult<Expense> readResult = PoiUtils.readFormFile(file, Expense.class);
        for (Expense expense : readResult.getData()) {
            System.out.println(expense);
            expense.setMsg("测试");
        }
        Workbook sheets = PoiUtils.writeBack(readResult);
        sheets.write(new FileOutputStream("D://test2.xlsx"));
    }

    @Test
    public void test5() throws IOException {
        File file = new File("D://test2.xlsx");
        ReadResult<Map<String, Object>> readResult = PoiUtils.readAsMap(file, ExcelReadInfo.withWriteBack(null, 0, null,
                row -> {
                    Cell cell = row.getCell(0);
                    return cell.getCellType().equals(CellType.STRING) && cell.getStringCellValue().contains("账务明细列表结束");
                }, Collections.singletonList("回写测试"), HSSFColor.HSSFColorPredefined.RED));
        for (Map<String, Object> data : readResult.getData()) {
            System.out.println(data);
            data.put("回写测试", "test");
        }
        Workbook sheets = PoiUtils.writeBack(readResult);
        File file1 = new File("D://test3.xlsx");
        sheets.write(new FileOutputStream(file1));
    }

}
