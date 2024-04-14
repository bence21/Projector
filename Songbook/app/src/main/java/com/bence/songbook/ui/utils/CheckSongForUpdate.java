package com.bence.songbook.ui.utils;

import android.annotation.SuppressLint;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.PopupWindow;

import com.bence.songbook.Memory;
import com.bence.songbook.R;
import com.bence.songbook.api.SongApiBean;
import com.bence.songbook.models.Language;
import com.bence.songbook.models.Song;

import java.util.ArrayList;
import java.util.List;

public class CheckSongForUpdate {

    public static final int UPDATE_SONGS_RESULT = 20;
    private static CheckSongForUpdate instance;
    private Song song;
    private List<CheckSongForUpdateListener> listeners;

    private CheckSongForUpdate() {
        listeners = new ArrayList<>();
    }

    public static CheckSongForUpdate getInstance() {
        if (instance == null) {
            instance = new CheckSongForUpdate();
        }
        return instance;
    }

    public void clearListeners() {
        listeners.clear();
        song = null;
    }

    public void addListener(CheckSongForUpdateListener listener, Song song) {
        if (song != this.song) {
            listeners.clear();
            this.song = song;
            checkForUpdate();
        }
        listeners.add(listener);
    }

    private void checkForUpdate() {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                SongApiBean songApiBean = new SongApiBean();
                Song newSong = songApiBean.getSong(song.getUuid());
                if (newSong != null && newSong.getModifiedDate().after(song.getModifiedDate())) {
                    notifyListeners();
                }
            }
        });
        thread.start();
    }

    private void notifyListeners() {
        for (CheckSongForUpdateListener listener : listeners) {
            listener.onSongHasBeenModified(createUpdatePopupWindow(listener.inflater));
        }
    }

    private void notifyListenersWithUpdate() {
        List<Language> passingLanguages = new ArrayList<>();
        passingLanguages.add(song.getLanguage());
        Memory.getInstance().setPassingLanguages(passingLanguages);
        for (CheckSongForUpdateListener listener : listeners) {
            listener.onUpdateButtonClick();
        }
    }

    private PopupWindow createUpdatePopupWindow(LayoutInflater inflater) {
        @SuppressLint("InflateParams") View customView = inflater.inflate(R.layout.content_update_songs, null);
        final PopupWindow updateSongsPopupWindow = new PopupWindow(
                customView,
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        if (Build.VERSION.SDK_INT >= 21) {
            updateSongsPopupWindow.setElevation(5.0f);
        }
        Button cancelButton = customView.findViewById(R.id.cancelButton);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateSongsPopupWindow.dismiss();
            }
        });
        Button updateButton = customView.findViewById(R.id.updateSongsButton);
        updateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                notifyListenersWithUpdate();
                updateSongsPopupWindow.dismiss();
            }
        });
        updateSongsPopupWindow.setOutsideTouchable(false);
        updateSongsPopupWindow.setBackgroundDrawable(null);
        return updateSongsPopupWindow;
    }
}
