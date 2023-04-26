package com.zary.sniffer.util;

import org.apache.commons.lang3.time.DateUtils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class DateUtil extends DateUtils {

    /**
     * 默认日期格式
     */
    public static final String FMT_DEFAULT = "yyyy-MM-dd HH:mm:ss";

    /**
     * 获取空日期(1970-01-01日期格式统一默认值)
     *
     * @return
     */
    public static Calendar getCalendarEmpty() {
        Calendar ca = Calendar.getInstance();
        ca.clear();
        ca.set(1970, 0, 1);
        return ca;
    }

    /**
     * 获取空日期(1970-01-01日期格式统一默认值)
     *
     * @return
     */
    public static Date getDateEmpty() {
        return getCalendarEmpty().getTime();
    }

    /**
     * 获取当前时间
     *
     * @return
     */
    public static Calendar getCalendarNow() {
        return Calendar.getInstance();
    }

    /**
     * 获取当前时间
     *
     * @return
     */
    public static Date getDateNow() {
        return Calendar.getInstance().getTime();
    }

    /**
     * 获取当前13位时间戳
     *
     * @return
     */
    public static long getNowTimestamp() {
        return System.currentTimeMillis();
    }

    /**
     * 获取UTC当前时间
     *
     * @return
     */
    public static Calendar getCalendarUtcNow() {
        Calendar ca = Calendar.getInstance();
        // 2、取得时间偏移量：
        int zoneOffset = ca.get(Calendar.ZONE_OFFSET);
        // 3、取得夏令时差：
        int dstOffset = ca.get(Calendar.DST_OFFSET);
        // 4、取得UTC时间：
        ca.add(Calendar.MILLISECOND, -(zoneOffset + dstOffset));
        return ca;
    }

    /**
     * 获取UTC当前时间
     *
     * @return
     */
    public static Date getDateUtcNow() {
        return getCalendarUtcNow().getTime();
    }

    /**
     * 获取UTC当前13位时间戳
     *
     * @return
     */
    public static long getUtcNowTimestamp() {
        return getCalendarUtcNow().getTimeInMillis();
    }

    /**
     * 根据日期字符串获取日期yyyy-MM-dd HH:mm:ss
     *
     * @return
     */
    public static Calendar getCalendar(String dateStr) {
        Calendar ca = getCalendarEmpty();
        ca.setTime(getDate(dateStr, FMT_DEFAULT));
        return ca;
    }

    /**
     * 根据日期long获取日期
     *
     * @return
     */
    public static Calendar getCalendar(long l) {
        Calendar ca = getCalendarEmpty();
        try {
            ca.setTimeInMillis(l);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ca;
    }

    /**
     * 根据日期字符串获取日期yyyy-MM-dd HH:mm:ss
     *
     * @return
     */
    public static Date getDate(String dateStr) {
        return getDate(dateStr, FMT_DEFAULT);
    }

    /**
     * 根据日期字符串获取日期
     *
     * @return
     */
    public static Calendar getCalendar(String dateStr, String dateFormat) {
        Calendar ca = getCalendarEmpty();
        ca.setTime(getDate(dateStr, dateFormat));
        return ca;
    }

    /**
     * 根据日期字符串获取日期
     *
     * @return
     */
    public static Date getDate(String dateStr, String dateFormat) {
        Date dt = getDateEmpty();
        try {
            SimpleDateFormat fmt = new SimpleDateFormat(dateFormat);
            dt = fmt.parse(dateStr);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return dt;
    }

    /**
     * 根据日期long获取日期
     *
     * @return
     */
    public static Date getDate(long l) {
        return getCalendar(l).getTime();
    }

    /**
     * 根据日期long获取日期字符串yyyy-MM-dd HH:mm:ss
     *
     * @return
     */
    public static String toDateString(long l) {
        return toDateString(l, FMT_DEFAULT);
    }

    /**
     * 根据日期long获取日期字符串
     *
     * @return
     */
    public static String toDateString(long l, String dateFormat) {
        return toDateString(getCalendar(l), dateFormat);
    }

    /**
     * 根据日期获取日期字符串yyyy-MM-dd HH:mm:ss
     *
     * @return
     */
    public static String toDateString(Calendar ca) {
        return toDateString(ca, FMT_DEFAULT);
    }

    /**
     * 根据日期获取日期字符串
     *
     * @return
     */
    public static String toDateString(Calendar ca, String dateFormat) {
        return toDateString(ca.getTime(), dateFormat);
    }

    /**
     * 根据日期获取日期字符串yyyy-MM-dd HH:mm:ss
     *
     * @return
     */
    public static String toDateString(Date dt) {
        return toDateString(dt, FMT_DEFAULT);
    }

    /**
     * 根据日期获取日期字符串
     *
     * @return
     */
    public static String toDateString(Date dt, String dateFormat) {
        String dateStr = "";
        try {
            SimpleDateFormat fmt = new SimpleDateFormat(dateFormat);
            dateStr = fmt.format(dt);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return dateStr;
    }


    /**
     * 获取当天剩余的时间，单位：秒
     *
     * @return
     */
    public static int getTodayRemainSecondNum() {
        //当前时间毫秒数
        long current = System.currentTimeMillis();
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, 1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        long tomorrowZero = calendar.getTimeInMillis();
        long remainSecond = (tomorrowZero - current) / 1000;
        return (int) remainSecond;
    }
}
