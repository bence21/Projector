package projector.remote;

public interface SongReadRemoteListener {
    void onSongVerseListViewItemClick(int index);

    void onSongListViewItemClick(int index);

    void onSearch(String text);

    void onSongPrev();

    void onSongNext();
}
