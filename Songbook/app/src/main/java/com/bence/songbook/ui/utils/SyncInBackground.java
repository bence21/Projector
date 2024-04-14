package com.bence.songbook.ui.utils;

import static com.bence.songbook.models.Song.copyLocallySet;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;

import com.bence.projector.common.dto.SongTitleDTO;
import com.bence.projector.common.dto.SongViewsDTO;
import com.bence.songbook.api.SongApiBean;
import com.bence.songbook.api.SongCollectionApiBean;
import com.bence.songbook.models.Language;
import com.bence.songbook.models.Song;
import com.bence.songbook.models.SongCollection;
import com.bence.songbook.repository.LanguageRepository;
import com.bence.songbook.repository.SongCollectionRepository;
import com.bence.songbook.repository.SongRepository;
import com.bence.songbook.repository.impl.ormLite.LanguageRepositoryImpl;
import com.bence.songbook.repository.impl.ormLite.SongCollectionRepositoryImpl;
import com.bence.songbook.repository.impl.ormLite.SongRepositoryImpl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class SyncInBackground {

    private static SyncInBackground instance;
    private static boolean incorrectSyncSave;
    private Long syncFrom;
    private int finished = 0;

    private SyncInBackground() {
    }

    public static SyncInBackground getInstance() {
        if (instance == null) {
            instance = new SyncInBackground();
        }
        return instance;
    }

    public void sync(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        incorrectSyncSave = sharedPreferences.getBoolean("incorrectSyncSave", true);
        LanguageRepository languageRepository = new LanguageRepositoryImpl(context);
        List<Language> languages = languageRepository.findAllSelectedForDownload();
        for (Language language : languages) {
            new Downloader(language, context).execute();
        }
        sharedPreferences.edit().putBoolean("incorrectSyncSave", false).apply();
    }

    public void syncViews(final Context context) {
        Thread thread = new Thread(() -> {
            try {
                while (finished != 0) {
                    Thread.sleep(100);
                }
                LanguageRepository languageRepository = new LanguageRepositoryImpl(context);
                List<Language> languages = languageRepository.findAllSelectedForDownload();
                for (Language language : languages) {
                    new ViewsDownloader(context, language).execute();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        });
        thread.start();
    }

    private void sortSongs(List<Song> all) {
        Collections.sort(all, (lhs, rhs) -> rhs.getModifiedDate().compareTo(lhs.getModifiedDate()));
    }

    public void setSyncFrom() {
        this.syncFrom = 1524234911591L;
    }

    public void syncYoutubeUrl(final Context context) {
        Thread thread = new Thread(() -> {
            try {
                while (finished != 0) {
                    Thread.sleep(100);
                }
                new YoutubeUrlDownloader(context).execute();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        });
        thread.start();
    }

    @SuppressLint("StaticFieldLeak")
    class Downloader extends AsyncTask<Void, Integer, Void> {
        private final Context context;
        private final Language language;
        private List<SongCollection> onlineModifiedSongCollections;

        Downloader(Language language, Context context) {
            this.language = language;
            this.context = context;
            ++finished;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            SongRepository songRepository = new SongRepositoryImpl(context);
            LanguageRepository languageRepository = new LanguageRepositoryImpl(context);
            final SongApiBean songApiBean = new SongApiBean();
            List<Song> languageSongs = language.getSongs();
            sortSongs(languageSongs);
            long modifiedDate;
            if (languageSongs.size() > 0) {
                modifiedDate = languageSongs.get(0).getModifiedDate().getTime();
            } else {
                modifiedDate = 0L;
            }
            if (syncFrom != null) {
                modifiedDate = syncFrom;
            }
            List<Song> onlineModifiedSongs = songApiBean.getSongsByLanguageAndAfterModifiedDate(language, modifiedDate);
            if (onlineModifiedSongs != null) {
                saveSongs(songRepository, languageRepository, languageSongs, onlineModifiedSongs);
            }

            SongCollectionApiBean songCollectionApiBean = new SongCollectionApiBean();
            SongCollectionRepository songCollectionRepository = new SongCollectionRepositoryImpl(context);
            List<SongCollection> songCollectionRepositoryAll = songCollectionRepository.findAllByLanguage(language);
            Date lastModifiedDate = new Date(0);
            for (SongCollection songCollection : songCollectionRepositoryAll) {
                Date songCollectionModifiedDate = songCollection.getModifiedDate();
                if (songCollectionModifiedDate.compareTo(lastModifiedDate) > 0) {
                    lastModifiedDate = songCollectionModifiedDate;
                }
            }
            long incorrectSyncSave = 1524465966476L;
            if (SyncInBackground.incorrectSyncSave && lastModifiedDate.getTime() > incorrectSyncSave) {
                lastModifiedDate = new Date(incorrectSyncSave);
            }
            onlineModifiedSongCollections = songCollectionApiBean.getSongCollections(language, lastModifiedDate);
            if (onlineModifiedSongCollections != null) {
                saveSongCollections(songCollectionRepository, songCollectionRepositoryAll, languageRepository);
            }
            return null;
        }

        private void saveSongs(SongRepository songRepository, LanguageRepository languageRepository, List<Song> languageSongs, List<Song> onlineModifiedSongs) {
            HashMap<String, Song> songHashMap = new HashMap<>(languageSongs.size());
            for (Song song : languageSongs) {
                songHashMap.put(song.getUuid(), song);
            }
            List<Song> needToRemove = new ArrayList<>();
            for (Song song : onlineModifiedSongs) {
                if (songHashMap.containsKey(song.getUuid())) {
                    Song modifiedSong = songHashMap.get(song.getUuid());
                    assert modifiedSong != null;
                    copyLocallySet(song, modifiedSong);
                    needToRemove.add(modifiedSong);
                    languageSongs.remove(modifiedSong);
                }
            }
            sortSongs(onlineModifiedSongs);
            languageSongs.addAll(onlineModifiedSongs);
            language.setSongs(languageSongs);
            songRepository.deleteAll(needToRemove);
            songRepository.save(onlineModifiedSongs);
            languageRepository.save(language);
        }

        private void saveSongCollections(SongCollectionRepository songCollectionRepository, List<SongCollection> songCollectionRepositoryAll, LanguageRepository languageRepository) {
            HashMap<String, SongCollection> songCollectionHashMap = new HashMap<>(songCollectionRepositoryAll.size());
            for (SongCollection songCollection : songCollectionRepositoryAll) {
                songCollectionHashMap.put(songCollection.getUuid(), songCollection);
            }
            List<SongCollection> needToDelete = new ArrayList<>();
            for (SongCollection songCollection : onlineModifiedSongCollections) {
                if (songCollectionHashMap.containsKey(songCollection.getUuid())) {
                    SongCollection modifiedSongCollection = songCollectionHashMap.get(songCollection.getUuid());
                    needToDelete.add(modifiedSongCollection);
                }
            }
            songCollectionRepository.deleteAll(needToDelete);
            songCollectionRepository.save(onlineModifiedSongCollections);
            languageRepository.save(language);
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            --finished;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
        }
    }

    @SuppressLint("StaticFieldLeak")
    static
    class YoutubeUrlDownloader extends AsyncTask<Void, Integer, Void> {
        private final Context context;

        YoutubeUrlDownloader(Context context) {
            this.context = context;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            SongRepository songRepository = new SongRepositoryImpl(context);
            final SongApiBean songApiBean = new SongApiBean();
            List<SongTitleDTO> songTitleDTOS = songApiBean.getSongsContainingYoutubeUrl();
            if (songTitleDTOS != null) {
                for (SongTitleDTO dto : songTitleDTOS) {
                    Song byUUID = songRepository.findByUUID(dto.getId());
                    if (byUUID != null && byUUID.getYoutubeUrl() == null) {
                        byUUID.setYoutubeUrl(dto.getYoutubeUrl());
                        songRepository.save(byUUID);
                    }
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
        }
    }

    @SuppressLint("StaticFieldLeak")
    static
    class ViewsDownloader extends AsyncTask<Void, Integer, Void> {
        private final Context context;
        private final Language language;

        ViewsDownloader(Context context, Language language) {
            this.context = context;
            this.language = language;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            SongRepository songRepository = new SongRepositoryImpl(context);
            final SongApiBean songApiBean = new SongApiBean();
            List<SongViewsDTO> songViewsDTOS = songApiBean.getSongViewsByLanguage(language);
            if (songViewsDTOS != null) {
                songRepository.saveViews(songViewsDTOS);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
        }
    }
}
