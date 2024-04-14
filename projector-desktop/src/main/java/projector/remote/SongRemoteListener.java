package projector.remote;

import javafx.collections.ObservableList;
import projector.controller.song.util.SearchedSong;
import projector.utils.scene.text.SongVersePartTextFlow;

import java.util.List;

public interface SongRemoteListener {
    void onSongVerseListViewChanged(List<SongVersePartTextFlow> list);

    void onSongListViewChanged(ObservableList<SearchedSong> items);
}
