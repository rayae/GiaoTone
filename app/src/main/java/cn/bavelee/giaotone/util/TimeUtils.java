package cn.bavelee.giaotone.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class TimeUtils {

    public static int parseHour(String s) {
        try {
            String[] ss = s.split(":");
            return Integer.parseInt(ss[0]);
        } catch (Exception ignore) {
            return 0;
        }
    }

    public static int parserMinute(String s) {
        try {
            String[] ss = s.split(":");
            return Integer.parseInt(ss[1]);
        } catch (Exception ignore) {
            return 0;
        }
    }

    public static boolean isCurrentTimeAvailable(String nowTime, String startTime, String endTime) {
        if (startTime.equals(endTime))
            return true;
        Calendar date = Calendar.getInstance();
        Calendar begin = Calendar.getInstance();
        Calendar end = Calendar.getInstance();
        date.set(Calendar.HOUR_OF_DAY, parseHour(nowTime));
        date.set(Calendar.MINUTE, parserMinute(nowTime));
        begin.set(Calendar.HOUR_OF_DAY, parseHour(startTime));
        begin.set(Calendar.MINUTE, parserMinute(startTime));
        end.set(Calendar.HOUR_OF_DAY, parseHour(endTime));
        end.set(Calendar.MINUTE, parserMinute(endTime));
        if (!begin.before(end)) {
            //开始时间大于结束时间加1天
            end.add(Calendar.DAY_OF_MONTH, 1);
        }
        date.add(Calendar.SECOND, 1);
        end.add(Calendar.SECOND, 2);
        return (begin.before(date) && end.after(date));
    }

    //比较当前时间是否在时间区间内
    public static boolean isCurrentTimeAvailable(String startTime, String endTime) {
        return isCurrentTimeAvailable(new SimpleDateFormat("HH:mm", Locale.CHINESE).format(new Date()), startTime, endTime);
    }
}
