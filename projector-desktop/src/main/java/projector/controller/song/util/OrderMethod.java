package projector.controller.song.util;

import projector.application.Settings;

public enum OrderMethod {
    RELEVANCE("Relevance"),
    ASCENDING_BY_TITLE("Ascending by title"),
    DESCENDING_BY_TITLE("Descending by title"),
    BY_MODIFIED_DATE("By modified date"),
    BY_PUBLISHED("By published"),
    BY_COLLECTION("By collection");
    private String text;

    OrderMethod(String text) {
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
