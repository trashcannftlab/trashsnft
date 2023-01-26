package easyJava.utils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class DateUtils {

    /**
     * <pre>YYYY-MM-dd HH:mm:ss
     * @return
     */
    public static String nowDatetime() {
        return getDateTimeString(new Date());
    }

    /**
     *
     * @return
     */
    public static String getRandomNum() {
        Calendar ca = Calendar.getInstance();
        String la = "" + ca.getTimeInMillis();
        return la.substring(0, 10);
    }

    public static long parseTZDate(String createdDateTime) throws ParseException {
        String STANDARD_DATE_FORMAT_UTC = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
        SimpleDateFormat sdf = new SimpleDateFormat(STANDARD_DATE_FORMAT_UTC);
        sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
        Date dateTime = sdf.parse(createdDateTime);
        return dateTime.getTime();
    }

    /**
     */
    public static int getWeekDay(Date date) {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        return c.get(Calendar.DAY_OF_WEEK) - 1;
    }

    /**
     */
    public static int getDayHour() {
        Calendar c = Calendar.getInstance();
        c.setTime(new Date());
        return c.get(Calendar.HOUR_OF_DAY);
    }

    /**
     * @return String yyyy-MM-dd HH:mm:ss
     */
    public static String getTimeStr(int day) {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        df.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai"));
        String resultDay = df.format(getTimeDate(day));
        return resultDay;
    }

    /**
     * @return Date
     */
    public static Date getTimeDate(int day) {
        Date d = new Date();
        Calendar cal = Calendar.getInstance();
        cal.setTime(d);
        cal.add(Calendar.DATE, -day);
        return cal.getTime();
    }

    /**
     * @return
     */
    public static String getMinuteStr(int minute) {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        df.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai"));
        String resultDay = df.format(getMinuteDate(minute));
        return resultDay;
    }

    private static Date getMinuteDate(int minute) {
        Date d = new Date();
        Calendar cal = Calendar.getInstance();
        cal.setTime(d);
        cal.add(Calendar.MINUTE, -minute);
        return cal.getTime();
    }

    public static long getHowManyDay(String t) throws ParseException {
        DateFormat df = DateFormat.getDateInstance();
        Date date = df.parse(t);
        Calendar othercal = Calendar.getInstance();
        othercal.setTime(date);
        Calendar cal = Calendar.getInstance();
        long m = cal.getTimeInMillis() - othercal.getTimeInMillis();
        return ((m / 1000) / 3600) / 24;

    }

    public static long getHowManyMinute(String t) throws ParseException {
        if (t.contains(".")) {
            String[] b = t.split("\\.");
            t = b[0];
        }
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        df.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai"));
        Date date = df.parse(t);
        Calendar othercal = Calendar.getInstance();
        othercal.setTime(date);
        Calendar cal = Calendar.getInstance();
        long m = cal.getTimeInMillis() - othercal.getTimeInMillis();
        return (m / 1000) / 60;

    }

    /**
     *
     * @return
     */
    public static String getTodayStr() {
        Calendar ca = Calendar.getInstance();
        String month = ca.get(ca.MONTH) + 1 + "";
        if (month.length() == 1) {
            month = "0" + month;
        }
        String date = ca.get(ca.DATE) + "";
        if (date.length() == 1) {
            date = "0" + date;
        }
        return ca.get(ca.YEAR) + "-" + month + "-" + date;
    }

    /**
     *
     * @return
     */
    public static String getTodayPastStr(int day) {
        Calendar ca = Calendar.getInstance();
        ca.add(Calendar.DAY_OF_MONTH, day);
        String month = ca.get(ca.MONTH) + 1 + "";
        if (month.length() == 1) {
            month = "0" + month;
        }
        String date = ca.get(ca.DATE) + "";
        if (date.length() == 1) {
            date = "0" + date;
        }
        return ca.get(ca.YEAR) + "-" + month + "-" + date;
    }

    public static String getDatePastStr(Date newdate, int day) {
        Calendar ca = Calendar.getInstance();
        ca.setTime(newdate);
        ca.add(Calendar.DAY_OF_MONTH, day);
        String month = ca.get(ca.MONTH) + 1 + "";
        if (month.length() == 1) {
            month = "0" + month;
        }
        String date = ca.get(ca.DATE) + "";
        if (date.length() == 1) {
            date = "0" + date;
        }
        return ca.get(ca.YEAR) + "-" + month + "-" + date;
    }

    public static String getDateTimePastStr(Date datetime, int day) {
        Calendar ca = Calendar.getInstance();
        ca.setTime(datetime);
        ca.add(Calendar.DAY_OF_MONTH, day);
        String month = ca.get(ca.MONTH) + 1 + "";
        if (month.length() == 1) {
            month = "0" + month;
        }
        String date = ca.get(ca.DATE) + "";
        if (date.length() == 1) {
            date = "0" + date;
        }
        return ca.get(ca.YEAR) + "-" + month + "-" + date;
    }

    /**
     *
     * @param datetime
     * @param min
     * @return
     */
    public static Date getDateTimePastMin(Date datetime, int min) {
        Calendar ca = Calendar.getInstance();
        ca.setTime(datetime);
        ca.add(Calendar.MINUTE, min);
        return ca.getTime();
    }

    public static Date getTodayPast(int day) {
        Calendar ca = Calendar.getInstance();
        ca.add(Calendar.DAY_OF_MONTH, day);
        return ca.getTime();
    }

    /**
     *
     * @param date
     * @return
     */
    public static String GetDateString(Date date) {
        Calendar ca = Calendar.getInstance();
        ca.setTime(date);
        String month = ca.get(ca.MONTH) + 1 + "";
        if (month.length() == 1) {
            month = "0" + month;
        }
        String day = ca.get(ca.DATE) + "";
        if (day.length() == 1) {
            day = "0" + day;
        }
        return ca.get(ca.YEAR) + "-" + month + "-" + day;
    }

    /**
     * @param day2
     * @return
     */
    public static int getDayMinus(String day1, String day2) {
        long days = 0;
        try {
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            df.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai"));
            Date d1 = df.parse(day1);
            Date d2 = df.parse(day2);
            long diff = d1.getTime() - d2.getTime();
            days = diff / (1000 * 60 * 60 * 24);
        } catch (Exception e) {
        }
        return (int) Math.ceil(days);
    }

    /**
     *
     * @param date
     * @return
     */
    public static String getDateTimeString(Date date) {
        String dateStr = "";
        DateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        sdf2.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai"));
        try {
            dateStr = sdf2.format(date);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return dateStr;
    }

    public static String getDateStr() {
        String dateStr = "";
        DateFormat sdf2 = new SimpleDateFormat("yyyyMMdd");
        sdf2.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai"));
        try {
            dateStr = sdf2.format(new Date());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return dateStr;
    }

    /**
     *
     * @param date
     * @return
     */
    public static String getTimeString(Date date) {
        String dateStr = "";
        DateFormat sdf2 = new SimpleDateFormat("HH:mm:ss");
        sdf2.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai"));
        try {
            dateStr = sdf2.format(date);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return dateStr;
    }

    /**
     *
     * @param dateStr
     * @return
     * @throws ParseException
     */
    public static Date parseDateTimeStr(String dateStr) {
        Date d1 = null;
        try {
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            df.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai"));
            d1 = df.parse(dateStr);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return d1;
    }

    /**
     *
     * @param dateStr
     * @return
     * @throws ParseException
     */
    public static Date parseDateStr(String dateStr) {
        Date d1 = null;
        try {
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
            df.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai"));
            d1 = df.parse(dateStr);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return d1;
    }

    /**
     *
     *
     * @param now
     * @param returnDate
     * @return
     */
    public static int daysBetween(Date now, Date returnDate) {
        Calendar cReturnDate = Calendar.getInstance();
        Calendar cNow = Calendar.getInstance();
        cNow.setTime(returnDate);
        cReturnDate.setTime(now);
        setTimeToMidnight(cNow);
        setTimeToMidnight(cReturnDate);
        long todayMs = cNow.getTimeInMillis();
        long returnMs = cReturnDate.getTimeInMillis();
        long intervalMs = todayMs - returnMs;
        return millisecondsToDays(intervalMs) + 1;
    }

    /**
     *
     *
     * @return
     */
    public static int daysBetween(String nowStr, String returnDateStr) {
        Calendar cReturnDate = Calendar.getInstance();
        Calendar cNow = Calendar.getInstance();
        Date returnDate = parseDateStr(returnDateStr);
        Date now = parseDateStr(nowStr);
        cNow.setTime(returnDate);
        cReturnDate.setTime(now);
        setTimeToMidnight(cNow);
        setTimeToMidnight(cReturnDate);
        long todayMs = cNow.getTimeInMillis();
        long returnMs = cReturnDate.getTimeInMillis();
        long intervalMs = todayMs - returnMs;
        return millisecondsToDays(intervalMs) + 1;
    }

    public static void main(String[] args) {

        Date startdate = parseDateStr("2015-10-14");
        Date enddate = parseDateStr("2015-10-15");
        String a = daysBetween(startdate, enddate) + "";
    }

    private static int millisecondsToDays(long intervalMs) {
        return (int) (intervalMs / (1000 * 86400));
    }

    private static void setTimeToMidnight(Calendar calendar) {
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
    }

    /**
     *
     *
     * @return
     */
    public static boolean isWeekEnd() {
        Calendar cal = Calendar.getInstance();
        if (cal.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY || cal.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
            return true;
        }
        return false;
    }
}