package com.bence.songbook;

import static com.bence.songbook.ui.activity.YoutubeActivity.logWithNullCheck;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.bence.songbook.models.FavouriteSong;
import com.bence.songbook.models.Language;
import com.bence.songbook.models.QueueSong;
import com.bence.songbook.models.Song;
import com.bence.songbook.models.SongCollection;
import com.bence.songbook.models.SongList;
import com.bence.songbook.network.ProjectionTextChangeListener;
import com.bence.songbook.ui.activity.MainActivity;

import java.util.ArrayList;
import java.util.List;

public class Memory {

    private static Memory instance;
    private List<Song> songs;
    private List<SongCollection> songCollections;
    private List<ProjectionTextChangeListener> projectionTextChangeListeners;
    private boolean shareOnNetwork;
    private List<Song> values;
    private MainActivity mainActivity;
    private Song songForLinking;
    private Song passingSong;
    private List<String> sharedTexts;
    private List<FavouriteSong> favouriteSongs;
    private List<QueueSong> queue;
    private int queueIndex = -1;
    private final List<Listener> listeners = new ArrayList<>();
    private SongList passingSongList;
    private SongList editingSongList;
    private String lastSearchedInText;
    private List<Language> passingLanguages;

    private Memory() {

    }

    public synchronized static Memory getInstance() {
        if (instance == null) {
            instance = new Memory();
        }
        return instance;
    }

    public synchronized List<Song> getSongs() {
        return songs;
    }

    public synchronized void setSongs(List<Song> songs) {
        this.songs = songs;
    }

    public List<Song> getSongsOrEmptyList() {
        if (songs == null) {
            return new ArrayList<>();
        }
        return songs;
    }

    public List<SongCollection> getSongCollections() {
        return songCollections;
    }

    public void setSongCollections(List<SongCollection> songCollections) {
        this.songCollections = songCollections;
    }

    public void setProjectionTextChangeListeners(List<ProjectionTextChangeListener> projectionTextChangeListeners) {
        this.projectionTextChangeListeners = projectionTextChangeListeners;
    }

    public boolean isShareOnNetwork() {
        return shareOnNetwork;
    }

    public void setShareOnNetwork(boolean shareOnNetwork) {
        this.shareOnNetwork = shareOnNetwork;
    }

    public List<Song> getValues() {
        return values;
    }

    public void setValues(List<Song> values) {
        this.values = values;
    }

    public MainActivity getMainActivity() {
        return mainActivity;
    }

    public void setMainActivity(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }

    public Song getSongForLinking() {
        return songForLinking;
    }

    public void setSongForLinking(Song songForLinking) {
        this.songForLinking = songForLinking;
    }

    public Song getPassingSong() {
        return passingSong;
    }

    public void setPassingSong(Song passingSong) {
        this.passingSong = passingSong;
    }

    public List<String> getSharedTexts() {
        if (sharedTexts == null) {
            sharedTexts = new ArrayList<>();
        }
        return sharedTexts;
    }

    public void setSharedTexts(List<String> sharedTexts) {
        this.sharedTexts = sharedTexts;
    }

    public List<FavouriteSong> getFavouriteSongs() {
        return favouriteSongs;
    }

    public void setFavouriteSongs(List<FavouriteSong> favouriteSongs) {
        this.favouriteSongs = favouriteSongs;
    }

    public void addSongToQueue(QueueSong queueSong) {
        if (queue == null) {
            queue = new ArrayList<>();
        }
        if (queue.size() == 0) {
            queueIndex = 0;
        }
        queueSong.setQueueNumber(queue.size());
        queue.add(queueSong);
        for (Listener listener : listeners) {
            listener.onAdd(queueSong);
        }
    }

    public List<QueueSong> getQueue() {
        return queue;
    }

    public void setQueue(List<QueueSong> queue) {
        this.queue = queue;
    }

    public void addOnQueueChangeListener(Listener listener) {
        listeners.add(listener);
    }

    public void removeQueueSong(QueueSong temp) {
        queue.remove(temp);
        for (Listener listener : listeners) {
            listener.onRemove(temp);
        }
    }

    public int getQueueIndex() {
        return queueIndex;
    }

    public void setQueueIndex(int queueIndex, Context context) {
        this.queueIndex = queueIndex;
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        sharedPreferences.edit().putInt("queueIndex", queueIndex).apply();
    }

    public SongList getPassingSongList() {
        return passingSongList;
    }

    public void setPassingSongList(SongList passingSongList) {
        this.passingSongList = passingSongList;
    }

    public SongList getEditingSongList() {
        return editingSongList;
    }

    public void setEditingSongList(SongList editingSongList) {
        this.editingSongList = editingSongList;
    }

    public String getLastSearchedInText() {
        return lastSearchedInText;
    }

    public void setLastSearchedInText(String lastSearchedInText) {
        this.lastSearchedInText = lastSearchedInText;
    }

    public List<Language> getPassingLanguages() {
        return passingLanguages;
    }

    public void setPassingLanguages(List<Language> passingLanguages) {
        this.passingLanguages = passingLanguages;
    }

    public void onText(String text) {
        try {
            if (projectionTextChangeListeners != null) {
                for (ProjectionTextChangeListener projectionTextChangeListener : projectionTextChangeListeners) {
                    projectionTextChangeListener.onSetText(text);
                }
            }
        } catch (Exception e) {
            logWithNullCheck(e, Memory.class.getSimpleName());
        }
    }

    public static void onTextForListeners(String text) {
        Memory memory = Memory.getInstance();
        if (memory != null) {
            memory.onText(text);
        }
    }

    public interface Listener {
        void onAdd(QueueSong queueSong);

        void onRemove(QueueSong queueSong);
    }
}
