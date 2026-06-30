package com.bence.songbook.repository;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.LongSparseArray;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.bence.songbook.models.QueueSong;
import com.bence.songbook.models.Song;
import com.bence.songbook.ui.utils.SingleLiveEvent;
import com.bence.songbook.utils.Utility;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class QueueRepository {

    private static volatile QueueRepository instance;

    private final Context appContext;
    private final MutableLiveData<List<QueueSong>> queueLiveData = new MutableLiveData<>(new ArrayList<>());
    private final SingleLiveEvent<QueueEvent> events = new SingleLiveEvent<>();

    private final List<QueueSong> queue = new ArrayList<>();
    private int queueIndex;
    private boolean loadedFromDatabase;

    private QueueRepository(Context context) {
        appContext = context.getApplicationContext();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(appContext);
        queueIndex = prefs.getInt("queueIndex", -1);
    }

    public static QueueRepository getInstance(Context context) {
        if (instance == null) {
            synchronized (QueueRepository.class) {
                if (instance == null) {
                    instance = new QueueRepository(context);
                }
            }
        }
        return instance;
    }

    public LiveData<List<QueueSong>> getQueueLiveData() {
        return queueLiveData;
    }

    public LiveData<QueueEvent> getEvents() {
        return events;
    }

    public synchronized List<QueueSong> getQueueSnapshot() {
        return new ArrayList<>(queue);
    }

    public synchronized int getQueueIndex() {
        return queueIndex;
    }

    public synchronized boolean isEmpty() {
        return queue.isEmpty();
    }

    public synchronized void loadFromDatabase(QueueSongRepository queueSongRepository) {
        if (loadedFromDatabase && !queue.isEmpty()) {
            return;
        }
        List<QueueSong> loaded = queueSongRepository.findAll();
        Collections.sort(loaded, (o1, o2) -> Utility.compare(o1.getQueueNumber(), o2.getQueueNumber()));
        queue.clear();
        queue.addAll(loaded);
        loadedFromDatabase = true;
        publishQueue();
    }

    public synchronized void hydrateSongs(List<Song> allSongs, SongRepository songRepository) {
        LongSparseArray<Song> sparseArray = new LongSparseArray<>(allSongs.size());
        for (Song song : allSongs) {
            if (song.getId() != null) {
                sparseArray.put(song.getId(), song);
            }
        }
        boolean changed = false;
        for (QueueSong queueSong : queue) {
            if (queueSong.getSong() == null) {
                continue;
            }
            Long id = queueSong.getSong().getId();
            if (id == null) {
                continue;
            }
            Song song = sparseArray.get(id);
            if (song != null) {
                queueSong.setSong(song);
                changed = true;
            } else {
                song = songRepository.findOne(id);
                if (song != null) {
                    queueSong.setSong(song);
                    sparseArray.put(id, song);
                    changed = true;
                }
            }
        }
        if (changed) {
            publishQueue();
        }
    }

    public synchronized void add(QueueSong queueSong) {
        if (queue.isEmpty()) {
            queueIndex = 0;
        }
        queueSong.setQueueNumber(queue.size());
        queue.add(queueSong);
        publishQueue();
        events.postValue(QueueEvent.ADDED_TO_QUEUE);
    }

    public synchronized void addAll(List<QueueSong> queueSongs) {
        if (queueSongs == null || queueSongs.isEmpty()) {
            return;
        }
        if (queue.isEmpty()) {
            queueIndex = 0;
        }
        for (QueueSong queueSong : queueSongs) {
            queueSong.setQueueNumber(queue.size());
            queue.add(queueSong);
        }
        publishQueue();
        events.postValue(QueueEvent.ADDED_TO_QUEUE);
    }

    public synchronized QueueSong remove(QueueSong queueSong) {
        int index = queue.indexOf(queueSong);
        if (index < 0) {
            return null;
        }
        queue.remove(index);
        for (int i = index; i < queue.size(); i++) {
            queue.get(i).setQueueNumber(i);
        }
        if (queue.isEmpty()) {
            queueIndex = -1;
        }
        publishQueue();
        return queueSong;
    }

    public synchronized QueueSong removeAt(int position) {
        if (position < 0 || position >= queue.size()) {
            return null;
        }
        return remove(queue.get(position));
    }

    public synchronized void swap(int from, int to) {
        if (from < 0 || to < 0 || from >= queue.size() || to >= queue.size() || from == to) {
            return;
        }
        QueueSong first = queue.get(from);
        QueueSong second = queue.get(to);
        int firstNumber = first.getQueueNumber();
        first.setQueueNumber(second.getQueueNumber());
        second.setQueueNumber(firstNumber);
        queue.set(from, second);
        queue.set(to, first);
        publishQueue();
    }

    public synchronized void clear() {
        queue.clear();
        queueIndex = -1;
        loadedFromDatabase = true;
        publishQueue();
        events.postValue(QueueEvent.QUEUE_CLEARED);
    }

    public void setQueueIndex(int index) {
        synchronized (this) {
            queueIndex = index;
        }
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(appContext);
        sharedPreferences.edit().putInt("queueIndex", index).apply();
    }

    private void publishQueue() {
        queueLiveData.postValue(new ArrayList<>(queue));
    }
}
