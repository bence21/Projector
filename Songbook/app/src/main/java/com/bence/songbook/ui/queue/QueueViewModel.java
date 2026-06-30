package com.bence.songbook.ui.queue;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.bence.songbook.api.SongApiBean;
import com.bence.songbook.models.QueueSong;
import com.bence.songbook.models.Song;
import com.bence.songbook.repository.QueueEvent;
import com.bence.songbook.repository.QueueRepository;
import com.bence.songbook.repository.impl.ormLite.QueueSongRepositoryImpl;
import com.bence.songbook.repository.impl.ormLite.SongRepositoryImpl;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class QueueViewModel extends AndroidViewModel {

    private final QueueRepository queueRepository;
    private final QueueSongRepositoryImpl queueSongRepository;
    private final SongRepositoryImpl songRepository;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public QueueViewModel(@NonNull Application application) {
        super(application);
        queueRepository = QueueRepository.getInstance(application);
        queueSongRepository = new QueueSongRepositoryImpl(application);
        songRepository = new SongRepositoryImpl(application);
    }

    public LiveData<List<QueueSong>> getQueue() {
        return queueRepository.getQueueLiveData();
    }

    public LiveData<Integer> getQueueIndex() {
        return queueRepository.getQueueIndexLiveData();
    }

    public LiveData<QueueEvent> getEvents() {
        return queueRepository.getEvents();
    }

    public List<QueueSong> getQueueSnapshot() {
        return queueRepository.getQueueSnapshot();
    }

    public int getCurrentQueueIndex() {
        return queueRepository.getQueueIndex();
    }

    public void loadAndHydrate(List<Song> allSongs) {
        queueRepository.loadFromDatabase(queueSongRepository);
        queueRepository.hydrateSongs(allSongs, songRepository);
    }

    public void addSong(Song song) {
        if (song == null) {
            return;
        }
        QueueSong queueSong = new QueueSong();
        queueSong.setSong(song);
        queueRepository.add(queueSong);
        executor.execute(() -> queueSongRepository.save(queueSong));
    }

    public void addSongs(List<Song> songs) {
        if (songs == null || songs.isEmpty()) {
            return;
        }
        List<QueueSong> queueSongs = new ArrayList<>(songs.size());
        for (Song song : songs) {
            if (song == null) {
                continue;
            }
            QueueSong queueSong = new QueueSong();
            queueSong.setSong(song);
            queueSongs.add(queueSong);
        }
        if (queueSongs.isEmpty()) {
            return;
        }
        queueRepository.addAll(queueSongs);
        executor.execute(() -> queueSongRepository.save(queueSongs));
    }

    public void addSongsByUuids(String[] uuids, List<Song> localSongs) {
        if (uuids == null || uuids.length == 0) {
            return;
        }
        List<String> pendingFetch = new ArrayList<>();
        List<Song> resolved = new ArrayList<>();

        for (String uuid : uuids) {
            if (uuid == null || uuid.isEmpty()) {
                continue;
            }
            Song song = findSongByUuid(localSongs, uuid);
            if (song == null) {
                song = songRepository.findByUUID(uuid);
            }
            if (song != null) {
                resolved.add(song);
            } else {
                pendingFetch.add(uuid);
            }
        }

        addSongs(resolved);

        if (pendingFetch.isEmpty()) {
            return;
        }

        executor.execute(() -> {
            List<QueueSong> fetchedQueueSongs = new ArrayList<>();
            for (String uuid : pendingFetch) {
                SongApiBean songApiBean = new SongApiBean();
                Song newSong = songApiBean.getSong(uuid);
                if (newSong != null) {
                    QueueSong queueSong = new QueueSong();
                    queueSong.setSong(newSong);
                    fetchedQueueSongs.add(queueSong);
                }
            }
            if (fetchedQueueSongs.isEmpty()) {
                return;
            }
            queueRepository.addAll(fetchedQueueSongs);
            queueSongRepository.save(fetchedQueueSongs);
        });
    }

    public void swap(int from, int to) {
        queueRepository.swap(from, to);
        List<QueueSong> queue = queueRepository.getQueueSnapshot();
        if (from < 0 || to < 0 || from >= queue.size() || to >= queue.size()) {
            return;
        }
        QueueSong first = queue.get(from);
        QueueSong second = queue.get(to);
        executor.execute(() -> {
            queueSongRepository.save(first);
            queueSongRepository.save(second);
        });
    }

    public void removeAt(int position) {
        QueueSong removed = queueRepository.removeAt(position);
        if (removed == null) {
            return;
        }
        List<QueueSong> queue = queueRepository.getQueueSnapshot();
        List<QueueSong> toSave = new ArrayList<>();
        for (int i = position; i < queue.size(); i++) {
            toSave.add(queue.get(i));
        }
        executor.execute(() -> {
            if (!toSave.isEmpty()) {
                queueSongRepository.save(toSave);
            }
            queueSongRepository.delete(removed);
        });
    }

    public void clearAll() {
        List<QueueSong> all = queueRepository.getQueueSnapshot();
        queueRepository.clear();
        executor.execute(() -> {
            if (!all.isEmpty()) {
                queueSongRepository.deleteAll(all);
            }
        });
    }

    public void setQueueIndex(int index) {
        queueRepository.setQueueIndex(index);
    }

    private static Song findSongByUuid(List<Song> songs, String uuid) {
        if (songs == null) {
            return null;
        }
        for (Song song : songs) {
            if (song.getUuid() != null && song.getUuid().equals(uuid)) {
                return song;
            }
        }
        return null;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        executor.shutdown();
    }
}
