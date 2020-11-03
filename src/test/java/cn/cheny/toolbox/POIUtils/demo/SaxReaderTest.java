package cn.cheny.toolbox.POIUtils.demo;

import cn.cheny.toolbox.POIUtils.PoiUtils;
import cn.cheny.toolbox.POIUtils.entity.ExcelReadInfo;
import cn.cheny.toolbox.POIUtils.entity.ReadResult;
import cn.cheny.toolbox.POIUtils.entity.SaxReadInfo;
import cn.cheny.toolbox.POIUtils.sax.SaxReader;
import cn.cheny.toolbox.POIUtils.worker.SaxWorkBookReader;
import org.apache.poi.hssf.util.HSSFColor;
import org.junit.Test;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author cheney
 * @date 2020-10-27
 */
public class SaxReaderTest {

    @Test
    public void saxReader() throws Exception {
        long l = System.currentTimeMillis();
        SaxReader saxReader = new SaxReader((row, data) -> {
            data.forEach(e -> System.out.println(e.getClass() + ":" + e));
        });
        saxReader.processSheet(new File("D:\\test.xlsx"), 0);
        System.out.println(System.currentTimeMillis() - l);
        System.out.println(saxReader.getFirstSheetCount());
    }

    @Test
    public void saxBookReader() {
        long l = System.currentTimeMillis();
        SaxReadInfo saxReadInfo = SaxReadInfo.withWriteBack(Collections.singletonList("标题test"), HSSFColor.HSSFColorPredefined.RED);
        SaxWorkBookReader saxWorkBookReader = new SaxWorkBookReader(saxReadInfo);
        File file = new File("D:\\test.xlsx");
        saxWorkBookReader.readAndConsume(file, (data, rowNum, raxReadResult) -> {
            System.out.println("行:" + rowNum + "->" + data);
            HashMap<String, String> data1 = new HashMap<>();
            data1.put("标题test", "test");
            raxReadResult.addWriteBackData(rowNum, data1);
        });
//        saxWorkBookReader.writeBackIfNeed(new FileOutputStream(file));
        System.out.println(System.currentTimeMillis() - l);
    }

    @Test
    public void poiUtils(){
        ReadResult<Map<String, Object>> mapReadResult = PoiUtils.readAsMap(new File("D:\\test.xlsx"), ExcelReadInfo.readInfo());
        System.out.println(mapReadResult.getData());
    }


}
