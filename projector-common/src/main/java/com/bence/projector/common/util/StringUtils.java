package com.bence.projector.common.util;

public class StringUtils {

    public static String trimLongString(String s, int maxLength) {
        if (s == null) {
            return null;
        }
        return s.substring(0, Math.min(s.length(), maxLength));
    }

    public static String trimLongString100(String s) {
        return trimLongString(s, 100);
    }

    public static String trimLongString255(String s) {
        return trimLongString(s, 255);
    }
}
