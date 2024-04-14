package projector.controller.song.util;

import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import projector.model.Song;

public abstract class SongTextFlow {
    private final Song song;
    private TextFlow textFlow;
    private Text text;

    SongTextFlow(Song song) {
        this.song = song;
    }

    public Song getSong() {
        return song;
    }

    public TextFlow getTextFlow() {
        return textFlow;
    }

    public void setTextFlow(TextFlow textFlow) {
        this.textFlow = textFlow;
    }

    public Text getText() {
        return text;
    }

    public void setText(Text text) {
        this.text = text;
    }

    @Override
    public String toString() {
        return song.toString();
    }
}
