package projector.application;

public enum ProjectionType {
    BIBLE("Bible"),
    SONG("Song"),
    REFERENCE("Reference"),
    CLIP_BOARD("Clipboard"),
    COUNTDOWN_TIMER("Countdown timer"),
    IMAGE("Image"),
    CLEAR("Clear"),
    COUNTDOWN_TIMER_PROCESS("Countdown timer process"),
    ;

    private final String text;

    ProjectionType(String text) {
        this.text = text;
    }

    @Override
    public String toString() {
        return Settings.getInstance().getResourceBundle().getString(text);
    }

    public boolean sameCategory(ProjectionType projectionType) {
        if (this.equals(projectionType)) {
            return true;
        }
        if (this.isCountdownTimer()) {
            return projectionType.isCountdownTimer();
        }
        return false;
    }

    public boolean isCountdownTimer() {
        return this == ProjectionType.COUNTDOWN_TIMER || this == ProjectionType.COUNTDOWN_TIMER_PROCESS;
    }
}
