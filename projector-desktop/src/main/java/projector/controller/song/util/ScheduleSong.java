package projector.controller.song.util;

import projector.model.Song;

public class ScheduleSong extends SongTextFlow {

    private int listViewIndex;

    public ScheduleSong(Song song) {
        super(song);
    }

    public int getListViewIndex() {
        return listViewIndex;
    }

    public void setListViewIndex(int listViewIndex) {
        this.listViewIndex = listViewIndex;
    }

    @Override
    public String toString() {
        return super.toString();
    }
}
