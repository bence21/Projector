package projector.utils;

public class NumberUtil {

    public static Integer getIntegerFromNumber(Number number) {
        if (number == null) {
            return null;
        }
        return number.intValue();
    }
}
