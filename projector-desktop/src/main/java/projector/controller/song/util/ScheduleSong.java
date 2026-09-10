package projector.controller.song.util;

import projector.model.Song;

public class ScheduleSong extends SongTextFlow {

    private int listViewIndex;
    /**
     * When non-null, this row is a service section label (no {@link Song}).
     */
    private final String sectionLabel;

    public ScheduleSong(Song song) {
        super(song);
        this.sectionLabel = null;
    }

    /**
     * Section heading in the schedule (e.g. prayer, communion); not projected as a song.
     */
    public ScheduleSong(String sectionLabel) {
        super(null);
        this.sectionLabel = sectionLabel;
    }

    public boolean isSection() {
        return sectionLabel != null;
    }

    public String getSectionLabel() {
        return sectionLabel;
    }

    public int getListViewIndex() {
        return listViewIndex;
    }

    public void setListViewIndex(int listViewIndex) {
        this.listViewIndex = listViewIndex;
    }

    @Override
    public String toString() {
        if (sectionLabel != null) {
            return sectionLabel;
        }
        return super.toString();
    }
}
