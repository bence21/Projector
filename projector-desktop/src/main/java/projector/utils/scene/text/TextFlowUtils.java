package projector.utils.scene.text;

import javafx.scene.paint.Color;

public class TextFlowUtils {

    public static String getColoredText(String text, Color color) {
        String s = "";
        if (color != null) {
            s += "<color=\"" + color + "\">";
        }
        s += text;
        if (color != null) {
            s += "</color>";
        }
        return s;
    }

    public static String getItalicText(String text) {
        return "[" + text + "]";
    }
}
