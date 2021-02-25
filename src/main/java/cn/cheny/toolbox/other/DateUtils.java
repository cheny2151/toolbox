package cn.cheny.toolbox.other;

import com.alibaba.fastjson.JSON;

import java.text.ParseException;
import java.util.Date;

/**
 * @Date 2021/2/25
 * @Created by chenyi
 */
public class DateUtils {

    private final static String[] FORMATSTR = new String[]{"yyyy-MM-dd HH:mm:ss", "yyyy", "yyyy-MM", "yyyyMM", "yyyy/MM", "yyyy-MM-dd", "yyyyMMdd"
            , "yyyy/MM/dd", "yyyyMMddHHmmss", "yyyy/MM/dd HH:mm:ss"};

    public static Date parseDate(String date) {
        try {
            return org.apache.commons.lang3.time.DateUtils.parseDate(date, FORMATSTR);
        } catch (ParseException e) {
            throw new IllegalArgumentException("ParseException for " + date + ",pattern is " + JSON.toJSONString(FORMATSTR), e);
        }
    }

    public static Date toDate(Object obj) {
        if (obj == null) {
            return null;
        } else if (obj instanceof Date) {
            return (Date) obj;
        } else if (obj instanceof Long) {
            return new Date((Long) obj);
        } else if (obj instanceof String) {
            return parseDate((String) obj);
        } else {
            throw new IllegalArgumentException("fail to parse date,origin data is " + obj);
        }
    }

}
