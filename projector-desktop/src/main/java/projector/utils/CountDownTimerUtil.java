package projector.utils;

import projector.controller.util.AutomaticAction;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Date;

public class CountDownTimerUtil {

    public static String getTimeTextFromDate(Long milliseconds, AutomaticAction selectedAction) {
        if (milliseconds == null) {
            return "";
        }
        String s = "";
        if (milliseconds < 0) {
            if (selectedAction != AutomaticAction.COUNTDOWN_TIMER_ENDLESS) {
                milliseconds = 0L;
            } else {
                s += "+";
            }
        }
        long seconds = milliseconds / 1000;
        long minutes = seconds / 60;
        seconds = seconds % 60;
        long hour = minutes / 60;
        minutes = minutes % 60;
        if (Math.abs(hour) > 0) {
            s += Math.abs(hour) + ":";
        }
        if (!s.isEmpty()) {
            s = add0(minutes, s);
        }
        s += Math.abs(minutes) + ":";
        if (Math.abs(seconds) < 10) {
            s += 0;
        }
        s += Math.abs(seconds);
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
        s += Math.abs(hour) + ":";
        int minute = localTime.getMinute();
        s = add0(minute, s);
        s += Math.abs(minute);
        return s;
    }

    private static String add0(long x, String s) {
        if (Math.abs(x) < 10) {
            s += "0";
        }
        return s;
    }
}
