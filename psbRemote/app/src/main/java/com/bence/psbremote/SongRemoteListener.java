package com.bence.psbremote;

import com.bence.psbremote.model.Song;

import java.util.List;

public interface SongRemoteListener {
    void onSongVerseListViewChanged(List<String> list);

    void onSongListViewChanged(List<Song> list);
}
