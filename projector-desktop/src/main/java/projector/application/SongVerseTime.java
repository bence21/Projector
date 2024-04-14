package projector.application;

import projector.model.Song;

import java.util.Date;
import java.util.Objects;

public class SongVerseTime {

    private final String songTitle;

    private final double[] verseTimes;
    private Song song;
    private Date date;
    private String songUuid;
    private Long songId;
    private Integer songTextLength;

    public SongVerseTime(String songTitle, int n) {
        this.songTitle = songTitle;
        verseTimes = new double[n];
    }

    public String getSongTitle() {
        return songTitle;
    }

    public double[] getVerseTimes() {
        return verseTimes;
    }

    public double getTotalTime() {
        double totalTime = 0;
        for (double verseTime : verseTimes) {
            totalTime += verseTime;
        }
        return totalTime;
    }

    public String getSongUuid() {
        if (song == null) {
            return songUuid;
        }
        return song.getUuid();
    }

    public int getSongTextLength() {
        if (song == null) {
            return Objects.requireNonNullElse(songTextLength, 0);
        }
        return song.getVersesText().length();
    }

    public void setSong(Song song) {
        this.song = song;
    }

    public Song getSong() {
        return song;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Date getDate() {
        return date;
    }

    public void setSongUuid(String songUuid) {
        this.songUuid = songUuid;
    }

    public void setSongId(long songId) {
        if (songId != 0) {
            this.songId = songId;
        }
    }

    public Long getSongId() {
        if (songId != null) {
            return songId;
        }
        if (song == null) {
            return null;
        }
        return song.getId();
    }

    public void setSongTextLength(int songTextLength) {
        this.songTextLength = songTextLength;
    }

    public boolean closeTextLength(int songTextLength) {
        if (this.songTextLength == null) {
            return true;
        }
        double lengthRatio = Math.min(this.songTextLength, songTextLength);
        lengthRatio /= Math.max(this.songTextLength, songTextLength);
        return lengthRatio > 0.95;
    }
}
