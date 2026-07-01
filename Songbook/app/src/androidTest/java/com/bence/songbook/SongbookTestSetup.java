package com.bence.songbook;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Looper;
import android.preference.PreferenceManager;

import androidx.test.platform.app.InstrumentationRegistry;

import com.bence.songbook.assertions.RecyclerViewSongInspector;
import com.bence.songbook.models.Song;
import com.bence.songbook.models.SongCollection;
import com.bence.songbook.models.SongCollectionElement;
import com.bence.songbook.repository.DatabaseHelper;
import com.bence.songbook.repository.QueueRepository;
import com.bence.songbook.repository.impl.ormLite.SongCollectionRepositoryImpl;
import com.bence.songbook.repository.impl.ormLite.SongRepositoryImpl;
import com.bence.songbook.ui.activity.LanguagesActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

public final class SongbookTestSetup {

    private static final String BUNDLED_DATABASE_ASSET = "songbook.db";
    private static final String DATABASE_NAME = "songbook.db";

    private SongbookTestSetup() {
    }

    public static void installBundledDatabase() {
        runOnMainThreadIfNeeded(SongbookTestSetup::installBundledDatabaseOnMainThread);
    }

    public static void prepareForFreshInstall() {
        runOnMainThreadIfNeeded(SongbookTestSetup::prepareForFreshInstallOnMainThread);
    }

    public static int getPersistedSongCount() {
        final int[] count = {0};
        runOnMainThreadIfNeeded(() -> count[0] = loadPersistedSongs().size());
        return count[0];
    }

    public static void preloadMemoryFromDatabase() {
        runOnMainThreadIfNeeded(SongbookTestSetup::preloadMemoryOnMainThread);
    }

    public static boolean hasPersistedSongA() {
        final boolean[] found = {false};
        runOnMainThreadIfNeeded(() -> {
            Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
            SongCollectionRepositoryImpl collectionRepository =
                    new SongCollectionRepositoryImpl(context);
            List<SongCollection> collections = collectionRepository.findAll();
            for (Song song : loadPersistedSongs()) {
                if (!RecyclerViewSongInspector.titlesMatch(
                        song.getTitle(), SongbookTestFixtures.SONG_A_TITLE)) {
                    continue;
                }
                for (SongCollection collection : collections) {
                    if (!RecyclerViewSongInspector.textsContainIgnoreCase(
                            collection.getName(), SongbookTestFixtures.SONG_A_COLLECTION_KEYWORD)) {
                        continue;
                    }
                    for (SongCollectionElement element : collection.getSongCollectionElements()) {
                        if (song.getUuid().equals(element.getSongUuid())
                                && RecyclerViewSongInspector.textsContain(
                                element.getOrdinalNumber(), SongbookTestFixtures.SONG_A_ORDINAL_NUMBER)) {
                            found[0] = true;
                            return;
                        }
                    }
                }
            }
        });
        return found[0];
    }

    private static void installBundledDatabaseOnMainThread() {
        Context targetContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        Context testContext = InstrumentationRegistry.getInstrumentation().getContext();

        resetApplicationStateOnMainThread();
        targetContext.deleteDatabase(DATABASE_NAME);

        File databaseFile = targetContext.getDatabasePath(DATABASE_NAME);
        File parent = databaseFile.getParentFile();
        if (parent != null && !parent.exists() && !parent.mkdirs()) {
            throw new IllegalStateException("Could not create database directory: " + parent);
        }

        try (InputStream inputStream = testContext.getAssets().open(BUNDLED_DATABASE_ASSET);
             OutputStream outputStream = new FileOutputStream(databaseFile)) {
            copyStream(inputStream, outputStream);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to install bundled database asset", e);
        }

        disableBackgroundSync(targetContext);
        initializeDatabaseVersionPreferences(targetContext);
        preloadMemoryOnMainThread();
    }

    private static void initializeDatabaseVersionPreferences(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        sharedPreferences.edit()
                .putInt("songDataBaseVersion", 13)
                .putInt("songVerseDataBaseVersion", 5)
                .putInt("languageDataBaseVersion", 8)
                .putInt("songCollectionDataBaseVersion", 5)
                .putInt("songCollectionElementDataBaseVersion", 5)
                .putInt("favouriteSongDataBaseVersion", 2)
                .putInt("queueSongDataBaseVersion", 1)
                .putInt("songListDataBaseVersion", 1)
                .putInt("songListDataElementBaseVersion", 1)
                .putInt("loggedInUserBaseVersion", 1)
                .apply();
    }

    private static void prepareForFreshInstallOnMainThread() {
        Context targetContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        resetApplicationStateOnMainThread();
        clearDatabaseVersionPreferences(targetContext);
        clearUiPreferences(targetContext);
        disableBackgroundSync(targetContext);
    }

    private static void clearUiPreferences(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        sharedPreferences.edit()
                .remove("shortCollectionName")
                .remove("reverseSortMethod")
                .apply();
    }

    private static void clearDatabaseVersionPreferences(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        sharedPreferences.edit()
                .remove("songDataBaseVersion")
                .remove("songVerseDataBaseVersion")
                .remove("languageDataBaseVersion")
                .remove("songCollectionDataBaseVersion")
                .remove("songCollectionElementDataBaseVersion")
                .remove("favouriteSongDataBaseVersion")
                .remove("queueSongDataBaseVersion")
                .remove("songListDataBaseVersion")
                .remove("songListDataElementBaseVersion")
                .remove("loggedInUserBaseVersion")
                .apply();
    }

    private static void resetApplicationStateOnMainThread() {
        Context targetContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        Memory.resetForTests();
        QueueRepository.resetForTests();
        DatabaseHelper.resetForTests();
        targetContext.deleteDatabase(DATABASE_NAME);
    }

    private static void disableBackgroundSync(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        long now = System.currentTimeMillis();
        sharedPreferences.edit()
                .putBoolean(LanguagesActivity.syncAutomatically, false)
                .putLong("lastSyncDateTime", now)
                .putLong("lastViewsSyncDateTime", now)
                .apply();
    }

    private static void preloadMemoryOnMainThread() {
        List<Song> songs = loadPersistedSongs();
        if (songs.isEmpty()) {
            return;
        }
        Memory memory = Memory.getInstance();
        memory.setSongs(songs);
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        SongCollectionRepositoryImpl songCollectionRepository =
                new SongCollectionRepositoryImpl(context);
        memory.setSongCollections(songCollectionRepository.findAll());
    }

    private static List<Song> loadPersistedSongs() {
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        SongRepositoryImpl songRepository = new SongRepositoryImpl(context);
        return songRepository.findAllExceptAsDeleted();
    }

    private static void copyStream(InputStream inputStream, OutputStream outputStream) throws IOException {
        byte[] buffer = new byte[8_192];
        int read;
        while ((read = inputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, read);
        }
        outputStream.flush();
    }

    private static void runOnMainThreadIfNeeded(Runnable runnable) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            runnable.run();
        } else {
            InstrumentationRegistry.getInstrumentation().runOnMainSync(runnable);
        }
    }
}
