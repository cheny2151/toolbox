package cn.cheny.toolbox.other;

import com.alibaba.fastjson.JSON;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * @Date 2021/2/25
 * @author by chenyi
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

    public static Calendar getDayStartCalendar(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.MILLISECOND, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        return calendar;
    }

    public static Calendar getDayEndCalendar(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.MILLISECOND, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        return calendar;
    }

    public static Date getDayStartTime(Date date) {
        return getDayStartCalendar(date).getTime();
    }

    public static Date getDayEndTime(Date date) {
        return getDayEndCalendar(date).getTime();
    }

    /**
     * 获取日期期间
     *
     * @param startDateObj 开始时间
     * @param endDateObj   结束时间
     * @param format       格式化
     * @return 期间集合
     */
    public static List<String> dayBetween(String startDateObj, String endDateObj, String format) {
        if (startDateObj == null || endDateObj == null) {
            throw new IllegalArgumentException("Date can not be null");
        }
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat(format);
            Date startDate = dateFormat.parse(startDateObj);
            Date endDate = dateFormat.parse(endDateObj);
            if (startDate.after(endDate)) {
                throw new IllegalArgumentException("Start date can not after end date");
            }
            Calendar startCalendar = getDayStartCalendar(startDate);
            Calendar endCalendar = getDayStartCalendar(endDate);
            List<String> results = new ArrayList<>();
            while (startCalendar.compareTo(endCalendar) <= 0) {
                String date = dateFormat.format(startCalendar.getTime());
                results.add(date);
                startCalendar.add(Calendar.DAY_OF_YEAR, 1);
            }
            return results;
        } catch (ParseException e) {
            throw new IllegalArgumentException("日期格式异常");
        }
    }

}
