package projector.utils;

import java.util.ArrayList;
import java.util.List;

public class CloneUtil {

    public static <T> List<T> cloneList(List<T> other) {
        if (other == null) {
            return null;
        }
        List<T> clonedList = new ArrayList<>(other.size());
        clonedList.addAll(other);
        return clonedList;
    }
}
