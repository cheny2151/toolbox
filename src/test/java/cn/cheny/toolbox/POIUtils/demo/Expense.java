package cn.cheny.toolbox.POIUtils.demo;

import cn.cheny.toolbox.POIUtils.annotation.ExcelData;
import cn.cheny.toolbox.POIUtils.annotation.ExcelWriteBack;
import lombok.Data;
import org.apache.poi.hssf.util.HSSFColor;

import java.math.BigDecimal;

/**
 * @author cheney
 * @date 2019-12-19
 */
@Data
public class Expense {

    @ExcelData(type = ExcelData.SwitchType.COLUMN_TITLE, columnTitle = "id")
    private String id;

    @ExcelData(type = ExcelData.SwitchType.COLUMN_TITLE, columnTitle = "account_balance")
    private BigDecimal accountBalance;

    @ExcelWriteBack(title = "信息", titleColor = HSSFColor.HSSFColorPredefined.RED,color = HSSFColor.HSSFColorPredefined.RED)
    private String msg;

}
