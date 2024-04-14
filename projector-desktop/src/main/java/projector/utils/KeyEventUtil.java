package projector.utils;


import javafx.scene.input.KeyEvent;

public class KeyEventUtil {

    public static String getTextFromEvent(KeyEvent event) {
        String text = event.getText();
        if (text == null || text.trim().isEmpty()) {
            String character = event.getCharacter();
            if (character != null && !character.trim().isEmpty()) {
                return character;
            }
            return "";
        }
        return text;
    }
}
