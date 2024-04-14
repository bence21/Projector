package projector.application;

public enum ProjectionType {
    BIBLE("Bible"),
    SONG("Song"),
    REFERENCE("Reference"),
    CLIP_BOARD("Clipboard"),
    COUNTDOWN_TIMER("Countdown timer"),
    IMAGE("Image"),
    ;

    private final String text;

    ProjectionType(String text) {
        this.text = text;
    }

    @Override
    public String toString() {
        return Settings.getInstance().getResourceBundle().getString(text);
    }
}
