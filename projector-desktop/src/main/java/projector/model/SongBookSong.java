package projector.model;

import com.j256.ormlite.field.DatabaseField;

public class SongBookSong {

    @DatabaseField(foreign = true, foreignAutoRefresh = true, index = true)
    private SongBook songBook;
    @DatabaseField(foreign = true, foreignAutoRefresh = true, index = true)
    private Song song;

    public SongBookSong() {
    }

    public SongBookSong(SongBook songBook, Song song) {
        this.songBook = songBook;
        this.song = song;
    }
}
