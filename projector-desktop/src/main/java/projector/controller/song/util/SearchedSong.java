package projector.controller.song.util;

import projector.model.Song;

public class SearchedSong extends SongTextFlow {

    private String foundAtVerse;

    public SearchedSong(Song song) {
        super(song);
    }

    public String getFoundAtVerse() {
        return foundAtVerse;
    }

    public void setFoundAtVerse(String foundAtVerse) {
        this.foundAtVerse = foundAtVerse;
    }

}
