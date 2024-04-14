package projector.utils;

import javafx.scene.text.TextFlow;
import projector.model.BibleVerse;

public class BibleVerseTextFlow extends TextFlow {

    private final BibleVerse bibleVerse;

    public BibleVerseTextFlow(TextFlow textFlow, BibleVerse bibleVerse) {
        super(textFlow);
        this.bibleVerse = bibleVerse;
    }

    public BibleVerse getBibleVerse() {
        return bibleVerse;
    }
}
