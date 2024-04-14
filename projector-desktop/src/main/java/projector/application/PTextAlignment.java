package projector.application;

import javafx.scene.text.TextAlignment;

public enum PTextAlignment {
    LEFT("Left"),

    CENTER("Center"),

    RIGHT("Right"),
    ;

    private final String text;

    PTextAlignment(String text) {
        this.text = text;
    }

    @Override
    public String toString() {
        return Settings.getInstance().getResourceBundle().getString(text);
    }

    public TextAlignment asTextAlignment() {
        return switch (this) {
            case LEFT -> TextAlignment.LEFT;
            case CENTER -> TextAlignment.CENTER;
            case RIGHT -> TextAlignment.RIGHT;
        };
    }
}
