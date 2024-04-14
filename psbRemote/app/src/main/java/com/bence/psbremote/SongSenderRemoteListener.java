package com.bence.psbremote;

public interface SongSenderRemoteListener {
    void onSongVerseListViewItemClick(int position);

    void onSongListViewItemClick(int position);

    void onSearch(String text);

    void onSongPrev();

    void onSongNext();
}
