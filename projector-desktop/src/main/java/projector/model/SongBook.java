package projector.model;

import com.j256.ormlite.field.DatabaseField;

import java.io.Serial;
import java.util.List;

public class SongBook extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;
    @DatabaseField
    private String title;
    private List<Song> songs;

    public SongBook() {
    }

    public SongBook(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<Song> getSongs() {
        return songs;
    }

    public void setSongs(List<Song> songs) {
        this.songs = songs;
    }
}
