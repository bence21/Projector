package projector.utils;

public class NumberUtil {

    public static Integer getIntegerFromNumber(Number number) {
        if (number == null) {
            return null;
        }
        return number.intValue();
    }

    public static Integer parseSafeInteger(String s) {
        try {
            return Integer.parseInt(s);
        } catch (Exception ignored) {
            return null;
        }
    }

    public static int parseSafeInt(String s, int def) {
        Integer anInteger = parseSafeInteger(s);
        if (anInteger == null) {
            return def;
        }
        return anInteger;
    }
}
