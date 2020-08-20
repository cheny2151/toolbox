package cn.cheny.toolbox.POIUtils.utils;

import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * @author cheney
 * @date 2019-12-27
 */
public class CellDealUtils implements CellDealFunction {

    private static final Pattern SPECIAL_CHARACTERS = Pattern.compile("[+\\-*/%?&|><=!()（）]");

    public static Object dealCellValue(Object cellValue) {
        if (cellValue instanceof String) {
            cellValue = ((String) cellValue).trim();
        }
        return cellValue;
    }

    public static String dealCellTitle(String cellTitle) {
        String[] split = SPECIAL_CHARACTERS.split(cellTitle);
        return Stream.of(split).reduce("", (a, b) -> a + b);
    }

    @Override
    public Object dealVal(Object cellVal) {
        return dealCellValue(cellVal);
    }

    @Override
    public String dealTitle(Object cellVal) {
        return dealCellTitle(cellVal == null ? "" : cellVal.toString());
    }
}
