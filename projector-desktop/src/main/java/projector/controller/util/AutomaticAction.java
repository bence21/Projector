package projector.controller.util;

public enum AutomaticAction {
    NOTHING, EMPTY, SONG_TITLE, COUNTDOWN_TIMER_ENDLESS;

    public static AutomaticAction getFromOrdinal(Integer ordinal) {
        if (ordinal == null) {
            return EMPTY;
        }
        try {
            return AutomaticAction.values()[ordinal];
        } catch (Exception e) {
            return EMPTY;
        }
    }
}
