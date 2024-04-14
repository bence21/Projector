package projector.application;

import java.util.ArrayList;
import java.util.List;

public enum ScreenProjectionAction {
    DISPLAY("Display"),
    CLEAR("Clear"),
    NO_ACTION("No action"),
    ;

    private static List<ScreenProjectionAction> screenProjectionActions = null;
    private final String text;

    ScreenProjectionAction(String text) {
        this.text = text;
    }

    @Override
    public String toString() {
        return Settings.getInstance().getResourceBundle().getString(text);
    }

    public static List<ScreenProjectionAction> getScreenProjectionActions() {
        if (screenProjectionActions == null) {
            screenProjectionActions = new ArrayList<>();
            screenProjectionActions.add(ScreenProjectionAction.DISPLAY);
            screenProjectionActions.add(ScreenProjectionAction.CLEAR);
            screenProjectionActions.add(ScreenProjectionAction.NO_ACTION);
        }
        return screenProjectionActions;
    }
}
