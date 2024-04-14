package projector.utils.scene.text;

import javafx.scene.text.Text;

import java.util.ArrayList;
import java.util.List;

public class Word {

    private final List<Text> letters = new ArrayList<>();

    public List<Text> getClonedLetters() {
        ArrayList<Text> list = new ArrayList<>(letters.size());
        for (Text letter : letters) {
            Text text = new Text(letter.getText());
            text.setFont(letter.getFont());
            list.add(text);
        }
        return list;
    }

    public List<Text> getLetters() {
        return letters;
    }

    public void addLetter(Text letter) {
        letters.add(letter);
    }

    public double getWidth() {
        double sum = 0;
        for (Text letter : letters) {
            // double width = letter.getBoundsInLocal().getWidth(); this contains also the font stroke size
            double width = letter.getLayoutBounds().getWidth(); // we need to width of the occupied space of text
            sum += width;
        }
        return sum;
    }

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder();
        for (Text letter : letters) {
            s.append(letter.getText());
        }
        return s.toString();
    }

    public int getNewLineCount() {
        int count = 0;
        for (Text text : letters) {
            for (char c : text.getText().toCharArray()) {
                if (c == '\n') {
                    ++count;
                }
            }
        }
        return count;
    }
}
