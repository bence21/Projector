package projector.controller.util;

import projector.application.Settings;

public enum ImageOrderMethod {

    BY_LAST_ACCESSED("By last accessed"),
    ASCENDING_BY_TITLE("Ascending by title"),
    DESCENDING_BY_TITLE("Descending by title"),
    BY_MODIFIED_DATE("By modified date"),
    BY_CREATED_DATE("By created date");
    private String text;

    ImageOrderMethod(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    @Override
    public String toString() {
        return Settings.getInstance().getResourceBundle().getString(text);
    }
}
