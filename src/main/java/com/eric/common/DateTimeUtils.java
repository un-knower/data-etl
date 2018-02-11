package com.eric.common;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.IllegalFieldValueException;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 日期时间工具类
 */
public class DateTimeUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(DateTimeUtils.class);

    private static final String RULE = "^[12]\\d{3}-((0[1-9])|(1[0-2]))-((0[1-9])|((1|2)\\d)|(3[0-1])) [0-5]\\d:[0-5]\\d:[0-5]\\d$";

    public static final String FORMAT_SHORT = "yyyy-MM-dd";
    public static final String FORMAT_MEDIUM = "yyyy-MM-dd HH:mm:ss";
    public static final String FORMAT_LONG = "yyyy-MM-dd HH:mm:ss.SSS";
    public static final String FORMAT_MEDIUM_TZ = "yyyy-MM-dd'T'HH:mm:ss'Z'";
    public static final String FORMAT_LONG_TZ = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
    public static final String FORMAT_TIME_SHORT = "HH:mm:ss";
    public static final String FORMAT_TIME_LONG = "HH:mm:ss.SSS";
    public static final String FORMAT_NET = FORMAT_LONG;
    public static final String FORMAT_DEFAULT = FORMAT_MEDIUM_TZ;

    public static final long MILLIS_PER_SECOND = 1000;
    public static final long MILLIS_PER_MINUTE = 60 * MILLIS_PER_SECOND;
    public static final long MILLIS_PER_HOUR = 60 * MILLIS_PER_MINUTE;
    public static final long MILLIS_PER_DAY = 24 * MILLIS_PER_HOUR;

    private static final long VALUE_INVALID = -1;

    private static final Map<String, PatternEnum> FORMAT_MAP = new LinkedHashMap<>();

    static {
        for (PatternEnum patternEnum : PatternEnum.values()) {
            FORMAT_MAP.put(patternEnum.getName(), patternEnum);
        }
    }

    private enum PatternEnum {
        SHORT(FORMAT_SHORT, 10),
        MEDIUM(FORMAT_MEDIUM, 19),
        LONG(FORMAT_LONG, 23),
        MEDIUM_TZ(FORMAT_MEDIUM_TZ, 20),
        LONG_TZ(FORMAT_LONG_TZ, 24),
        NET(FORMAT_NET, 23),
        DEFAULT(FORMAT_DEFAULT, 20),;

        private final String name;
        private final DateTimeFormatter formatter;
        private final int length;

        PatternEnum(String name, int length) {
            this.name = name;
            this.length = length;
            this.formatter = DateTimeFormat.forPattern(name);
        }

        public String getName() {
            return name;
        }

        public DateTimeFormatter getFormatter() {
            return formatter;
        }

        public int getLength() {
            return length;
        }
    }

    /**
     * 根据pattern字符串获取对应Formatter实例
     *
     * @param pattern 表达式
     * @return DateTimeFormatter对象
     */
    private static DateTimeFormatter getFormatter(String pattern) {
        DateTimeFormatter formatter = null;
        PatternEnum formatterEnum = FORMAT_MAP.get(pattern);
        if (formatterEnum != null) {
            formatter = formatterEnum.getFormatter();
        } else {
            formatter = DateTimeFormat.forPattern(pattern);
        }
        return formatter;
    }

    /**
     * 根据pattern格式将日期字符串解析为Joda日期时间对象
     *
     * @param dateStr 时间字符串
     * @param pattern 表达式
     * @return DateTime对象
     * ============================================================
     * Modified reason: 兼容SimpleDateFormat解析的格式，包括24小时"24:00:00.000"(joda不支持24小时)
     * ============================================================
     */
    public static DateTime parse2JodaTime(String dateStr, String pattern) {
        try {
            return DateTime.parse(dateStr, getFormatter(pattern));
        } catch (IllegalFieldValueException e) {
            try {
                SimpleDateFormat dateFormat = new SimpleDateFormat(pattern);
                Date date = dateFormat.parse(dateStr);
                return new DateTime(date.getTime());
            } catch (ParseException e1) {
                LOGGER.warn("parse failed with pattern[{}].", pattern, e1);
                return null;
            }
        } catch (Throwable e) {
            LOGGER.warn("parse failed with pattern[{}].", pattern, e);
            return null;
        }
    }

    /**
     * 使用预定义Formatter解析日期时间字符串
     *
     * @param dateStr 时间字符串
     * @return 解析后的时间
     */
    protected static Date parseByDefaultFormatter(String dateStr) {
        Date dateTime = null;
        int len = 0;
        if (StringUtils.isBlank(dateStr)
                || (len = dateStr.length()) < PatternEnum.SHORT.getLength()
                || len > PatternEnum.LONG_TZ.getLength()) {
            LOGGER.warn("invalid date argument[{}].", dateStr);
            return null;
        }
        for (PatternEnum patternEnum : FORMAT_MAP.values()) {
            if (len != patternEnum.getLength()) {
                continue;
            }
            DateTime jodaTime = parse2JodaTime(dateStr, patternEnum.getName());
            if (jodaTime != null) {
                dateTime = jodaTime.toDate();
            }
        }
        if (dateTime == null) {
            LOGGER.warn("parse failed for dateStr[{}], no invalid formatter.[{}],=>{}",
                    dateStr,
                    len,
                    PatternEnum.DEFAULT.getLength()
            );
        }
        return dateTime;
    }

    /**
     * 解析日期时间字符串为日期对象[支持多种日期模式]
     *
     * @param dateStr 时间字符串
     * @param patterns 表达式
     * @return 解析后的时间
     */
    public static Date parse2Date(String dateStr, String... patterns) {

        //验证参数合法性
        if (StringUtils.isBlank(dateStr)) {
            LOGGER.warn("invalid input arguments, date=>[{}].", dateStr);
            return null;
        }
        // 判断是否指定格式，若未指定格式，并且日期字符串符合缺省格式规范，调用缺省格式进行解析
        if (ArrayUtils.isEmpty(patterns)) {
            int len = dateStr.length();
            if ((len < FORMAT_SHORT.length() || len > FORMAT_LONG_TZ.length())) {
                LOGGER.warn("invalid input arguments, date=>[{}], formats=>[{}]. details=>[specific format empty and didn't fit default format].", dateStr, Arrays.toString(patterns));
                return null;
            } else {
                return parseByDefaultFormatter(dateStr);
            }
        }
//        LOGGER.info("parse using specifyied format, formats=>[{}].", Arrays.toString(patterns));
        // 遍历格式列表，按顺序执行解析，若成功直接返回，否则尝试下一种格式
        for (String format : patterns) {
            DateTime jodaTime = parse2JodaTime(dateStr, format);
            if (jodaTime != null) {
                return jodaTime.toDate();
            }
        }
        return null;
    }

    /**
     * 解析日期时间字符串为日期时间对应毫秒值[支持多种日期模式]
     *
     * @param dateStr 时间字符串
     * @param patterns 表达式
     * @return 解析后的时间
     */
    public static long parse2Long(String dateStr, String... patterns) {
        Date dateTime = parse2Date(dateStr, patterns);
        return dateTime == null ? VALUE_INVALID : dateTime.getTime();
    }

    /**
     * 格式化日期时间对象[以缺省日期格式]
     *
     * @param date 时间
     * @return 默认格式化字符串
     */
    public static String format(Date date) {
        if (date == null) {
            return null;
        }
        return format(date, FORMAT_DEFAULT);
    }

    /**
     * 格式化日期时间对象[指定日期格式]
     *
     * @param date 时间
     * @param pattern 格式化表达式
     * @return 格式化字符串
     */
    public static String format(Date date, String pattern) {
        if (date == null || StringUtils.isEmpty(pattern)) {
            return null;
        }
        return format(date.getTime(), pattern);
    }

    /**
     * 格式化日期时间毫秒值[以缺省日期格式]
     *
     * @param datetimeInMillis 毫秒时间
     * @return 格式化字符串
     */
    public static String format(long datetimeInMillis) {
        if (datetimeInMillis < 1) {
            return null;
        }
        return format(datetimeInMillis, FORMAT_DEFAULT);
    }

    /**
     * 格式化日期时间毫秒值[以指定日期格式]
     *
     * @param datetimeInMillis 毫秒时间
     * @param pattern 格式化表达式
     * @return 格式化字符串
     */
    public static String format(long datetimeInMillis, String pattern) {
        if (datetimeInMillis < 1 || StringUtils.isEmpty(pattern)) {
            return null;
        }
        return new DateTime(datetimeInMillis).toString(pattern);
    }

    /**
     * 格式化日期时间字符串为另一种格式
     *
     * @param dateStr 时间字符串
     * @param patternBefore 之前的时间表达式
     * @param patternAfter 格式化之后的表达式
     * @return 新的格式化字符串
     */
    public static String format(String dateStr, String patternBefore, String patternAfter) {
        if (StringUtils.isBlank(dateStr) || StringUtils.isBlank(patternBefore) || StringUtils.isBlank(patternAfter)) {
            return null;
        }
        Date date = parse2Date(dateStr, patternBefore);
        if (date == null) {
            return null;
        }
        return format(date, patternAfter);
    }

    public static DateTime plusYears(long datetimeInMillis, int years) {
        return new DateTime(datetimeInMillis).plusYears(years);
    }

    public static DateTime plusMonths(long datetimeInMillis, int months) {
        return new DateTime(datetimeInMillis).plusMonths(months);
    }

    public static DateTime plusWeeks(long datetimeInMillis, int weeks) {
        return new DateTime(datetimeInMillis).plusWeeks(weeks);
    }

    public static DateTime plusDays(long datetimeInMillis, int days) {
        return new DateTime(datetimeInMillis).plusDays(days);
    }

    public static DateTime plusHours(long datetimeInMillis, int hours) {
        return new DateTime(datetimeInMillis).plusHours(hours);
    }

    public static DateTime plusMinutes(long datetimeInMillis, int minutes) {
        return new DateTime(datetimeInMillis).plusMinutes(minutes);
    }

    public static DateTime plusSeconds(long datetimeInMillis, int seconds) {
        return new DateTime(datetimeInMillis).plusSeconds(seconds);
    }

    public static DateTime plusMillis(long datetimeInMillis, int millis) {
        return new DateTime(datetimeInMillis).plusMillis(millis);
    }

    public static DateTime minusYears(long datetimeInMillis, int years) {
        return new DateTime(datetimeInMillis).minusYears(years);
    }

    public static DateTime minusMonths(long datetimeInMillis, int months) {
        return new DateTime(datetimeInMillis).minusMonths(months);
    }

    public static DateTime minusWeeks(long datetimeInMillis, int weeks) {
        return new DateTime(datetimeInMillis).minusWeeks(weeks);
    }

    public static DateTime minusDays(long datetimeInMillis, int days) {
        return new DateTime(datetimeInMillis).minusDays(days);
    }

    public static DateTime minusHours(long datetimeInMillis, int hours) {
        return new DateTime(datetimeInMillis).minusHours(hours);
    }

    public static DateTime minusMinutes(long datetimeInMillis, int minutes) {
        return new DateTime(datetimeInMillis).minusMinutes(minutes);
    }

    public static DateTime minusSeconds(long datetimeInMillis, int seconds) {
        return new DateTime(datetimeInMillis).minusSeconds(seconds);
    }

    public static DateTime minusMillis(long datetimeInMillis, int millis) {
        return new DateTime(datetimeInMillis).minusMillis(millis);
    }

    public static String plusYears(String dateStr, int years, String pattern) {
        DateTime jodaTime = parse2JodaTime(dateStr, pattern);
        if (jodaTime == null) {
            return null;
        }
        return jodaTime.plusYears(years).toString(pattern);
    }

    public static String plusMonths(String dateStr, int months, String pattern) {
        DateTime jodaTime = parse2JodaTime(dateStr, pattern);
        if (jodaTime == null) {
            return null;
        }
        return jodaTime.plusMonths(months).toString(pattern);
    }

    public static String plusWeeks(String dateStr, int weeks, String pattern) {
        DateTime jodaTime = parse2JodaTime(dateStr, pattern);
        if (jodaTime == null) {
            return null;
        }
        return jodaTime.plusWeeks(weeks).toString(pattern);
    }

    public static String plusDays(String dateStr, int days, String pattern) {
        DateTime jodaTime = parse2JodaTime(dateStr, pattern);
        if (jodaTime == null) {
            return null;
        }
        return jodaTime.plusDays(days).toString(pattern);
    }

    public static String plusHours(String dateStr, int hours, String pattern) {
        DateTime jodaTime = parse2JodaTime(dateStr, pattern);
        if (jodaTime == null) {
            return null;
        }
        return jodaTime.plusHours(hours).toString(pattern);
    }

    public static String plusMinutes(String dateStr, int minutes, String pattern) {
        DateTime jodaTime = parse2JodaTime(dateStr, pattern);
        if (jodaTime == null) {
            return null;
        }
        return jodaTime.plusMinutes(minutes).toString(pattern);
    }

    public static String plusSeconds(String dateStr, int seconds, String pattern) {
        DateTime jodaTime = parse2JodaTime(dateStr, pattern);
        if (jodaTime == null) {
            return null;
        }
        return jodaTime.plusSeconds(seconds).toString(pattern);
    }

    public static String plusMillis(String dateStr, int millis, String pattern) {
        DateTime jodaTime = parse2JodaTime(dateStr, pattern);
        if (jodaTime == null) {
            return null;
        }
        return jodaTime.plusMillis(millis).toString(pattern);
    }

    public static String minusYears(String dateStr, int years, String pattern) {
        DateTime jodaTime = parse2JodaTime(dateStr, pattern);
        if (jodaTime == null) {
            return null;
        }
        return jodaTime.minusYears(years).toString(pattern);
    }

    public static String minusMonths(String dateStr, int months, String pattern) {
        DateTime jodaTime = parse2JodaTime(dateStr, pattern);
        if (jodaTime == null) {
            return null;
        }
        return jodaTime.minusMonths(months).toString(pattern);
    }

    public static String minusWeeks(String dateStr, int weeks, String pattern) {
        DateTime jodaTime = parse2JodaTime(dateStr, pattern);
        if (jodaTime == null) {
            return null;
        }
        return jodaTime.minusWeeks(weeks).toString(pattern);
    }

    public static String minusDays(String dateStr, int days, String pattern) {
        DateTime jodaTime = parse2JodaTime(dateStr, pattern);
        if (jodaTime == null) {
            return null;
        }
        return jodaTime.minusDays(days).toString(pattern);
    }

    public static String minusHours(String dateStr, int hours, String pattern) {
        DateTime jodaTime = parse2JodaTime(dateStr, pattern);
        if (jodaTime == null) {
            return null;
        }
        return jodaTime.minusHours(hours).toString(pattern);
    }

    public static String minusMinutes(String dateStr, int minutes, String pattern) {
        DateTime jodaTime = parse2JodaTime(dateStr, pattern);
        if (jodaTime == null) {
            return null;
        }
        return jodaTime.minusMinutes(minutes).toString(pattern);
    }

    public static String minusSeconds(String dateStr, int seconds, String pattern) {
        DateTime jodaTime = parse2JodaTime(dateStr, pattern);
        if (jodaTime == null) {
            return null;
        }
        return jodaTime.minusSeconds(seconds).toString(pattern);
    }

    public static String minusMillis(String dateStr, int millis, String pattern) {
        DateTime jodaTime = parse2JodaTime(dateStr, pattern);
        if (jodaTime == null) {
            return null;
        }
        return jodaTime.minusMillis(millis).toString(pattern);
    }

    public static String plusYears(long datetimeInMillis, int years, String pattern) {
        return plusYears(datetimeInMillis, years).toString(pattern);
    }

    public static String plusMonths(long datetimeInMillis, int months, String pattern) {
        return plusMonths(datetimeInMillis, months).toString(pattern);
    }

    public static String plusWeeks(long datetimeInMillis, int weeks, String pattern) {
        return plusWeeks(datetimeInMillis, weeks).toString(pattern);
    }

    public static String plusDays(long datetimeInMillis, int days, String pattern) {
        return plusDays(datetimeInMillis, days).toString(pattern);
    }

    public static String plusHours(long datetimeInMillis, int hours, String pattern) {
        return plusHours(datetimeInMillis, hours).toString(pattern);
    }

    public static String plusMinutes(long datetimeInMillis, int minutes, String pattern) {
        return plusMinutes(datetimeInMillis, minutes).toString(pattern);
    }

    public static String plusSeconds(long datetimeInMillis, int seconds, String pattern) {
        return plusSeconds(datetimeInMillis, seconds).toString(pattern);
    }

    public static String plusMillis(long datetimeInMillis, int millis, String pattern) {
        return plusMillis(datetimeInMillis, millis).toString(pattern);
    }

    public static String minusYears(long datetimeInMillis, int years, String pattern) {
        return minusYears(datetimeInMillis, years).toString(pattern);
    }

    public static String minusMonths(long datetimeInMillis, int months, String pattern) {
        return minusMonths(datetimeInMillis, months).toString(pattern);
    }

    public static String minusWeeks(long datetimeInMillis, int weeks, String pattern) {
        return minusWeeks(datetimeInMillis, weeks).toString(pattern);
    }

    public static String minusDays(long datetimeInMillis, int days, String pattern) {
        return minusDays(datetimeInMillis, days).toString(pattern);
    }

    public static String minusHours(long datetimeInMillis, int hours, String pattern) {
        return minusHours(datetimeInMillis, hours).toString(pattern);
    }

    public static String minusMinutes(long datetimeInMillis, int minutes, String pattern) {
        return minusMinutes(datetimeInMillis, minutes).toString(pattern);
    }

    public static String minusSeconds(long datetimeInMillis, int seconds, String pattern) {
        return minusSeconds(datetimeInMillis, seconds).toString(pattern);
    }

    public static String minusMillis(long datetimeInMillis, int millis, String pattern) {
        return minusMillis(datetimeInMillis, millis).toString(pattern);
    }

    public static String getNextDay(long datetimeInMillis, String pattern) {
        return plusDays(datetimeInMillis, 1, pattern);
    }

    public static String getNextDay(String dateStr, String pattern) {
        return plusDays(dateStr, 1, pattern);
    }

    public static int compare(String startDateStr, String stopDateStr, String pattern) {
        DateTime startJodaTime = parse2JodaTime(startDateStr, pattern);
        DateTime stopJodaTime = parse2JodaTime(stopDateStr, pattern);
        if (startJodaTime == null && stopJodaTime == null) {
            return 0;
        }
        if (startJodaTime == null) {
            return -1;
        }
        if (stopJodaTime == null) {
            return 1;
        }
        return startJodaTime.compareTo(stopJodaTime);
    }

    /**
     * 搜索时获取结束时间
     *
     * @param time 时间字符串
     * @param dateFormat 格式化对象
     * @return 字符串
     * @throws ParseException
     * author zhangxuhui 2013-7-15 下午02:09:21
     */
    public static String getEndDate(String time, SimpleDateFormat dateFormat) throws ParseException {
        Date nowDate = new Date();
        Date timeDate = stringToDate(time, dateFormat);
        String endDate = "";
        if (timeDate.after(nowDate)) {
            endDate = dateFormat.format(nowDate);
        } else {
            endDate = dateFormat.format(timeDate);
        }
        return endDate;
    }

    /**
     * 字符串转换为日期格式
     *
     * @param dateStr 时间字符串
     * @param dateFormat 格式化对象
     * @return 日期
     */
    public static Date stringToDate(String dateStr, SimpleDateFormat dateFormat) throws ParseException {
        Date date = dateFormat.parse(dateStr);
        return date;
    }

    public static boolean ifShouldDelete(String start, String initDateStr, String keepDateStr, SimpleDateFormat sdf) throws ParseException {
        Date startDay = stringToDate(start, sdf);
        Date initDate = stringToDate(initDateStr, new SimpleDateFormat("yyyy-MM-dd"));
        Date keepDate = stringToDate(keepDateStr, sdf);
        if (startDay.before(initDate) || startDay.before(keepDate)) {
            return true;
        }
        return false;
    }

    /**
     * 时间天数增加或减少
     *
     * @param time
     * @param days
     * @param dateFormat
     * @return
     */
    public static String dateAddDays(Date time, int days, SimpleDateFormat dateFormat) {
        long timeLong = time.getTime() / 1000 + 60l * 60 * 24 * days;
        time.setTime(timeLong * 1000);
        String timeStr = dateFormat.format(time);
        return timeStr;
    }

    public static boolean ifShouldDelete(String start, String initDateStr, int keepDays, SimpleDateFormat sdf) throws ParseException {
        Date nowDay = new Date();
        String keepDateStr = DateTimeUtils.dateAddDays(nowDay, 0 - keepDays, sdf);
        return ifShouldDelete(start, initDateStr, keepDateStr, sdf);
    }

    /**
     * 验证日期字符串是否是yyyy-MM-dd HH:mm:ss格式
     *
     * @param dateStr
     * @return
     */
    public static boolean validateStringDate(String dateStr) {
        return dateStr != null && dateStr.matches(RULE);
    }

    /**
     * 转换时间字符串为毫秒值（0点到指定时间间隔毫秒值）
     *
     * @param timeStr
     * @param pattern
     * @return ============================================================
     * @Modified reason: 兼容SimpleDateFormat解析的格式，包括24小时"24:00:00.000"(joda不支持24小时)
     * ============================================================
     */
    public static long time2Millis(String timeStr, String pattern) {
        if (StringUtils.isEmpty(timeStr) || StringUtils.isEmpty(pattern)) {
            return -1;
        }
        try {
            DateTime jodaTime = DateTime.parse(timeStr, getFormatter(pattern).withZone(DateTimeZone.UTC));
            return jodaTime.getMillis();
        } catch (IllegalFieldValueException e) {
            try {
                SimpleDateFormat dateFormat = new SimpleDateFormat(pattern);
                Date date = dateFormat.parse(timeStr);
                return date.getTime();
            } catch (ParseException e1) {
                LOGGER.warn("parse failed with pattern[{}].", pattern, e1);
                return -1;
            }
        } catch (Throwable e) {
            LOGGER.warn("parse failed with pattern[{}].", pattern, e);
            return -1;
        }
    }

    /**
     * 获取对应时间的当月开始时间
     *
     * @param timeInMillis 毫秒时间
     * @return 时间毫秒数
     */
    public static long getMonthFirstDay(long timeInMillis) {
        return new DateTime(timeInMillis).withDayOfMonth(1).withTime(0, 0, 0, 0).getMillis();
    }

    /**
     * 获取对应时间的当月结束时间
     *
     * @param timeInMillis 毫秒时间
     * @return 时间毫秒数
     */
    public static long getMonthLastDay(long timeInMillis) {
        return new DateTime(timeInMillis).withDayOfMonth(1).withTime(0, 0, 0, 0).plusMonths(1).minusMillis(1).getMillis();
    }

    public static long getNextMonthLastDay(long timeInMillis) {
        return new DateTime(timeInMillis).withDayOfMonth(1).withTime(0, 0, 0, 0).plusMonths(2).minusMillis(1).getMillis();
    }

    /**
     * 获取对应时间的当年开始时间
     *
     * @param timeInMillis 毫秒时间
     * @return 时间毫秒数
     */
    public static long getYearFirstDay(long timeInMillis) {
        return new DateTime(timeInMillis).withDayOfYear(1).withTime(0, 0, 0, 0).getMillis();
    }

    /**
     * 获取对应时间的当年结束时间
     *
     * @param timeInMillis 毫秒时间
     * @return 时间毫秒数
     */
    public static long getYearLastDay(long timeInMillis) {
        return new DateTime(timeInMillis).withDayOfYear(1).withTime(0, 0, 0, 0).plusYears(1).minusMillis(1).getMillis();
    }

    /**
     * 获取对应时间的当天开始时间
     *
     * @param timeInMillis 毫秒时间
     * @return 时间毫秒数
     */
    public static long getDateBeginTime(long timeInMillis) {
        return new DateTime(timeInMillis).withTime(0, 0, 0, 0).getMillis();
    }

    /**
     * 获取对应时间的当天结束时间
     *
     * @param timeInMillis 毫秒时间
     * @return 时间毫秒数
     */
    public static long getDateEndTime(long timeInMillis) {
        return new DateTime(timeInMillis).withTime(0, 0, 0, 0).plusDays(1).minusMillis(1).getMillis();
    }

    /**
     * 获取当周周末的第二天
     *
     * @param dateStr 时间字符串
     * @param format 格式化表达式
     * @return 字符串时间
     */
    public static String getLatestSunday(String dateStr, String format) {
        return parse2JodaTime(dateStr, format).withDayOfWeek(7).toString(format);
    }

    /**
     * 获取当月底的第二天
     *
     * @param dateStr 时间字符串
     * @param format 格式化表达式
     * @return 字符串时间
     */
    public static String getLatestMonthEnd(String dateStr, String format) {
        return parse2JodaTime(dateStr, format).plusMonths(1).withDayOfMonth(1).toString(format);
    }

    public static void main(String[] args) {
////        long start = System.currentTimeMillis();
////        System.out.println(parse2Long("2016-07-22"));
////        System.out.println(parse2Long("2016-07-22 13:30:12"));
////        System.out.println(parse2Long("2016-07-22T13:30:12Z"));
////        System.out.println(parse2Long("2016-07-22 13:30:12.123"));
////        System.out.println(parse2Long("2016-07-22T13:30:12.123Z"));
////        System.out.println(System.currentTimeMillis() - start);
////        System.out.println(parse2Long("2016-07-22", "yyyy-MM-dd"));
////        System.out.println(parse2Long("2016-07-22 13:30:12", "yyyy-MM-dd HH:mm:ss"));
////        System.out.println(parse2Long("2016-07-22T13:30:12Z", "yyyy-MM-dd'T'HH:mm:ss'Z'"));
////        System.out.println(parse2Long("2016-07-22 13:30:12.123", "yyyy-MM-dd HH:mm:ss.SSS"));
////        System.out.println(parse2Long("2016-07-22T13:30:12.123Z", "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"));
////        System.out.println(parse2Long("1601", "yyMM"));
//
//        System.out.println(format(1456761600000L));
//        System.out.println(format(1470150600000L));
//        System.out.println(format(1470146400000L));
//        System.out.println(format(1467331200000L));
//        System.out.println(format(1467421020000L));
//        System.out.println(parse2Long("2016-07-01T00:00:00Z"));
//        System.out.println(parse2Long("2016-08-04T16:00:00Z"));
//        System.out.println(format(1470109116294L));
//
//        System.out.println("======================");
//        System.out.println(DateTime.parse("20160715"));
//        System.out.println(DateTime.parse("16"));
//        System.out.println(DateTime.parse("2016-07-15"));
//
//        System.out.println("==============================");
//        System.out.println(parse2Long("1970-01-01 00:00:00", "yyyy-MM-dd HH:mm:ss"));
//        System.out.println(parse2Long("10:30:00", "HH:mm:ss"));
//        System.out.println(parse2Date("10:30:00", "HH:mm:ss"));
//
//
//        long base = parse2Long("1970-01-01T00:00:00.000Z", FORMAT_LONG_TZ);
//        System.out.println(format(parse2Long("2016-08-18", FORMAT_SHORT) + parse2Long("10:30:00", "HH:mm:ss")));
//        System.out.println(format(parse2Long("2016-08-18", FORMAT_SHORT) + parse2Long("10:30:00", "HH:mm:ss") - base));
////        System.out.println(format(parse2Long("2016-08-18", FORMAT_SHORT) + 9000000));
////        System.out.println(format(parse2Long("2016-08-18", FORMAT_SHORT) + 37800000));
////        System.out.println(new DateTime(0).toString(FORMAT_LONG_TZ));
//
//        System.out.println(time2Millis("10:30:00", "HH:mm:ss"));
        Date date = DateTimeUtils.parse2Date("2016-08-18 24:00:00.000");
        System.out.println(date != null);
        System.out.println(format(date));
    }
}
