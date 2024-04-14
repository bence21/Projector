package projector.utils.scene.text;

import java.util.ArrayList;
import java.util.List;

public class Phrase {
    private List<Word> words = new ArrayList<>();

    public List<Word> getWords() {
        return words;
    }

    public void addWord(Word word) {
        words.add(word);
    }

    public double getWidth() {
        double sum = 0;
        for (Word word : words) {
            sum += word.getWidth();
        }
        return sum;
    }

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder();
        for (Word word : words) {
            s.append(word.toString());
        }
        return s.toString();
    }
}
