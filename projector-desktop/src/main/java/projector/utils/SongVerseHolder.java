package projector.utils;

import projector.model.SongVerse;
import projector.utils.scene.text.SongVersePartTextFlow;

import java.util.List;

public class SongVerseHolder {

    private SongVerse songVerse;
    private List<SongVersePartTextFlow> songVersePartTextFlows;

    public SongVerse getSongVerse() {
        return songVerse;
    }

    public void setSongVerse(SongVerse songVerse) {
        this.songVerse = songVerse;
    }

    public List<SongVersePartTextFlow> getSongVersePartTextFlows() {
        return songVersePartTextFlows;
    }

    public void setSongVersePartTextFlows(List<SongVersePartTextFlow> songVersePartTextFlows) {
        this.songVersePartTextFlows = songVersePartTextFlows;
    }
}
