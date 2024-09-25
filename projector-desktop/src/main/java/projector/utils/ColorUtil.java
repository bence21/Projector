package projector.utils;

import javafx.scene.paint.Color;
import projector.application.Settings;

public class ColorUtil {

    private static final Settings settings = Settings.getInstance();

    public static Color getCollectionNameColor() {
        if (settings.isDarkTheme()) {
            return Color.rgb(224, 247, 250);
        } else {
            return Color.rgb(34, 42, 116);
        }
    }

    public static Color getSongTitleColor() {
        return getGeneralTextColor();
    }

    public static Color getGeneralTextColorByTheme(boolean darkTheme) {
        if (darkTheme) {
            return Color.rgb(255, 255, 255);
        } else {
            return Color.rgb(0, 0, 0);
        }
    }

    public static Color getGeneralTextColor() {
        return getGeneralTextColorByTheme(settings.isDarkTheme());
    }

    public static Color getSubduedTextColor() {
        if (settings.isDarkTheme()) {
            return getGeneralTextColor().darker();
        } else {
            return Color.rgb(21, 21, 21, 0.7);
        }
    }

    public static Color getReferenceTextColor() {
        if (settings.isDarkTheme()) {
            return Color.rgb(200, 224, 255);
        } else {
            return Color.rgb(24, 24, 24);
        }
    }

    public static Color getVisitedTextColor() {
        if (settings.isDarkTheme()) {
            return Color.rgb(187, 148, 255);
        } else {
            return Color.rgb(56, 0, 129);
        }
    }

    public static Color getMainBorderColor() {
        if (settings.isDarkTheme()) {
            return Color.rgb(70, 60, 60, 0.604);
        } else {
            return Color.rgb(182, 182, 182, 0.604);
        }
    }

    public static Color getSongVerseBorderColor() {
        if (settings.isDarkTheme()) {
            return Color.rgb(0, 0, 0, 0.604);
        } else {
            return Color.rgb(77, 77, 77, 0.5);
        }
    }

    @SuppressWarnings("unused")
    public static Color getSubduedColor(Color color) {
        if (settings.isDarkTheme()) {
            return color.darker();
        } else {
            return color.brighter();
        }
    }

    public static Color getColorWithOpacity(Color color, double opacity) {
        return new Color(
                color.getRed(),
                color.getGreen(),
                color.getBlue(),
                opacity
        );
    }
}
