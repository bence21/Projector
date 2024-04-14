package com.bence.songbook;

public interface ProgressMessage {

    void onProgress(int value);

    void onSongCollectionProgress(int value);

    void onSetMax(int value);
}
