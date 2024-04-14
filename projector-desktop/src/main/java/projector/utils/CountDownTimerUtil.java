package projector.utils;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Date;

public class CountDownTimerUtil {

    public static String getTimeTextFromDate(Long milliseconds) {
        if (milliseconds == null) {
            return "";
        }
        if (milliseconds < 0) {
            milliseconds = 0L;
        }
        long seconds = milliseconds / 1000;
        long minutes = seconds / 60;
        seconds = seconds % 60;
        long hour = minutes / 60;
        minutes = minutes % 60;
        String s = "";
        if (hour > 0) {
            s = hour + ":";
        }
        if (!s.isEmpty()) {
            s = add0(minutes, s);
        }
        s += minutes + ":";
        if (seconds < 10) {
            s += 0;
        }
        s += seconds;
        return s;
    }

    public static Long getRemainedTime(Date finishDate) {
        Date now = new Date();
        if (finishDate == null) {
            return null;
        }
        return finishDate.getTime() - now.getTime();
    }

    public static String getDisplayTextFromDateTime(Date date) {
        if (date == null) {
            return "";
        }
        LocalDateTime localDateTime = LocalDateTime.ofInstant(date.toInstant(), java.time.ZoneId.systemDefault());
        LocalTime localTime = localDateTime.toLocalTime();
        String s = "";
        int hour = localTime.getHour();
        s = add0(hour, s);
        s += hour + ":";
        int minute = localTime.getMinute();
        s = add0(minute, s);
        s += minute;
        return s;
    }

    private static String add0(long x, String s) {
        if (x < 10) {
            s += "0";
        }
        return s;
    }
}
