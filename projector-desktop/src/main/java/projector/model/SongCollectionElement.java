package projector.model;

import com.google.gson.annotations.Expose;
import com.j256.ormlite.field.DatabaseField;

public class SongCollectionElement extends BaseEntity {

    @Expose
    @DatabaseField
    private String ordinalNumber;
    private String ordinalNumberReplaced;
    private String ordinalNumberLowerCase;
    private Integer ordinalNumberInt;
    @Expose
    @DatabaseField
    private String songUuid;
    private transient Song song;
    @DatabaseField(foreign = true, index = true, foreignAutoRefresh = true, maxForeignAutoRefreshLevel = 1)
    private SongCollection songCollection;

    public int getOrdinalNumberInt() {
        try {
            if (ordinalNumberInt == null) {
                ordinalNumberInt = Integer.parseInt(ordinalNumber.replaceAll("[^0-9]*", ""));
            }
            return ordinalNumberInt;
        } catch (NumberFormatException e) {
            return Integer.MAX_VALUE;
        }
    }

    public String getOrdinalNumber() {
        if (ordinalNumber.equals("0")) {
            return ordinalNumber;
        }
        if (ordinalNumberReplaced == null) {
            ordinalNumberReplaced = ordinalNumber.replaceAll("^0+", "");
        }
        return ordinalNumberReplaced;
    }

    public void setOrdinalNumber(String ordinalNumber) {
        if (ordinalNumber == null) {
            this.ordinalNumber = null;
        } else {
            this.ordinalNumber = ordinalNumber.replaceAll("^0+", "");
        }
        this.ordinalNumberReplaced = null;
        this.ordinalNumberLowerCase = null;
        this.ordinalNumberInt = null;
    }

    @Override
    public String toString() {
        return ordinalNumber;
    }

    public String getSongUuid() {
        return songUuid;
    }

    public void setSongUuid(String songUuid) {
        this.songUuid = songUuid;
    }

    public SongCollection getSongCollection() {
        return songCollection;
    }

    public void setSongCollection(SongCollection songCollection) {
        this.songCollection = songCollection;
    }

    public void setSong(Song song) {
        this.song = song;
    }

    public Song getSong() {
        return song;
    }

    public String getOrdinalNumberLowerCase() {
        if (ordinalNumberLowerCase == null) {
            ordinalNumberLowerCase = getOrdinalNumber().toLowerCase();
        }
        return ordinalNumberLowerCase;
    }
}
