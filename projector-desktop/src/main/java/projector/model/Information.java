package projector.model;

import com.j256.ormlite.field.DatabaseField;

import java.util.Date;

public class Information extends BaseEntity {

    @DatabaseField
    private Date lastDownloadedSongs;

    public Date getLastDownloadedSongs() {
        return lastDownloadedSongs == null ? null : (Date) lastDownloadedSongs.clone();
    }

    public void setLastDownloadedSongs(Date lastDownloadedSongs) {
        this.lastDownloadedSongs = lastDownloadedSongs == null ? null : (Date) lastDownloadedSongs.clone();
    }
}
