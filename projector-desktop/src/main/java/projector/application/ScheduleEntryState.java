package projector.application;

import com.google.gson.annotations.Expose;
import projector.model.Song;

public class ScheduleEntryState {

    public static final String TYPE_SECTION = "section";
    public static final String TYPE_SONG = "song";

    @Expose
    private String type;
    @Expose
    private String sectionLabel;
    @Expose
    private Long songId;
    @Expose
    private String songUuid;

    public static ScheduleEntryState section(String label) {
        ScheduleEntryState state = new ScheduleEntryState();
        state.type = TYPE_SECTION;
        state.sectionLabel = label;
        return state;
    }

    public static ScheduleEntryState song(Song song) {
        ScheduleEntryState state = new ScheduleEntryState();
        state.type = TYPE_SONG;
        if (song.getUuid() != null) {
            state.songUuid = song.getUuid();
        } else {
            state.songId = song.getId();
        }
        return state;
    }

    public String getType() {
        return type;
    }

    public String getSectionLabel() {
        return sectionLabel;
    }

    public Long getSongId() {
        return songId;
    }

    public String getSongUuid() {
        return songUuid;
    }
}
