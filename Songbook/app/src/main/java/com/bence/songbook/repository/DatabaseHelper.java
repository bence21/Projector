package com.bence.songbook.repository;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.preference.PreferenceManager;
import android.util.Log;

import com.bence.songbook.R;
import com.bence.songbook.models.FavouriteSong;
import com.bence.songbook.models.Language;
import com.bence.songbook.models.LoggedInUser;
import com.bence.songbook.models.QueueSong;
import com.bence.songbook.models.Song;
import com.bence.songbook.models.SongCollection;
import com.bence.songbook.models.SongCollectionElement;
import com.bence.songbook.models.SongList;
import com.bence.songbook.models.SongListElement;
import com.bence.songbook.models.SongVerse;
import com.bence.songbook.repository.dao.CustomDao;
import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

import java.sql.SQLException;

public class DatabaseHelper extends OrmLiteSqliteOpenHelper {
    private static final String TAG = DatabaseHelper.class.getSimpleName();
    private static final String DATABASE_NAME = "songbook.db";
    private static final int DATABASE_VERSION = 27;

    @SuppressLint("StaticFieldLeak")
    private static DatabaseHelper instance;
    private Context context;

    private CustomDao<Song, Long> songDao;
    private CustomDao<SongVerse, Long> songVerseDao;
    private CustomDao<Language, Long> languageDao;
    private CustomDao<SongCollection, Long> songCollectionDao;
    private CustomDao<SongCollectionElement, Long> songCollectionElementDao;
    private CustomDao<FavouriteSong, Long> favouriteSongDao;
    private CustomDao<QueueSong, Long> queueSongDao;
    private CustomDao<SongList, Long> songListDao;
    private CustomDao<SongListElement, Long> songListElementDao;
    private CustomDao<LoggedInUser, Long> loggedInUserDao;

    private DatabaseHelper(final Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION, R.raw.ormlite_config);
    }

    public static DatabaseHelper getInstance(final Context context) {
        if (instance == null) {
            DatabaseHelper databaseHelper = new DatabaseHelper(context);
            databaseHelper.context = context;
            instance = databaseHelper;
        }
        return instance;
    }

    @Override
    public void onCreate(final SQLiteDatabase sqliteDatabase, final ConnectionSource connectionSource) {
        try {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
            sharedPreferences.edit().putInt("songDataBaseVersion", 13).apply();
            TableUtils.createTableIfNotExists(connectionSource, Song.class);
            sharedPreferences.edit().putInt("songVerseDataBaseVersion", 5).apply();
            TableUtils.createTableIfNotExists(connectionSource, SongVerse.class);
            sharedPreferences.edit().putInt("languageDataBaseVersion", 8).apply();
            TableUtils.createTableIfNotExists(connectionSource, Language.class);
            sharedPreferences.edit().putInt("songCollectionDataBaseVersion", 5).apply();
            TableUtils.createTableIfNotExists(connectionSource, SongCollection.class);
            sharedPreferences.edit().putInt("songCollectionElementDataBaseVersion", 5).apply();
            TableUtils.createTableIfNotExists(connectionSource, SongCollectionElement.class);
            sharedPreferences.edit().putInt("favouriteSongDataBaseVersion", 2).apply();
            TableUtils.createTableIfNotExists(connectionSource, FavouriteSong.class);
            sharedPreferences.edit().putInt("queueSongDataBaseVersion", 1).apply();
            TableUtils.createTableIfNotExists(connectionSource, QueueSong.class);
            sharedPreferences.edit().putInt("songListDataBaseVersion", 1).apply();
            TableUtils.createTableIfNotExists(connectionSource, SongList.class);
            sharedPreferences.edit().putInt("songListDataElementBaseVersion", 1).apply();
            TableUtils.createTableIfNotExists(connectionSource, SongListElement.class);
            sharedPreferences.edit().putInt("loggedInUserBaseVersion", 1).apply();
            TableUtils.createTableIfNotExists(connectionSource, LoggedInUser.class);
        } catch (final SQLException e) {
            Log.e(TAG, "Unable to create databases", e);
        }
    }

    @Override
    public void onUpgrade(final SQLiteDatabase sqliteDatabase, final ConnectionSource connectionSource, final int oldVer, final int newVer) {
        try {
            if (oldVer < newVer) {
                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
                int songDataBaseVersion = sharedPreferences.getInt("songDataBaseVersion", 0);
                if (songDataBaseVersion < 4) {
                    TableUtils.dropTable(connectionSource, Song.class, true);
                } else {
                    if (songDataBaseVersion == 4) {
                        getSongDao().executeRaw("ALTER TABLE `song` ADD COLUMN versionGroup VARCHAR(30);");
                    }
                    if (songDataBaseVersion < 6) {
                        getSongDao().executeRaw("ALTER TABLE `song` ADD COLUMN youtubeUrl VARCHAR(20);");
                    }
                    if (songDataBaseVersion == 7) {
                        try {
                            getSongDao().executeRaw("ALTER TABLE `song` DROP COLUMN favourite ;");
                            getSongDao().executeRaw("ALTER TABLE `song` DROP COLUMN favouritePublished ;");
                        } catch (Exception ignored) {
                        }
                    }
                    if (songDataBaseVersion < 9) {
                        getSongDao().executeRaw("ALTER TABLE `song` ADD COLUMN views INTEGER");
                    }
                    if (songDataBaseVersion < 10) {
                        getSongDao().executeRaw("ALTER TABLE `song` ADD COLUMN verseOrder VARCHAR(72)");
                    }
                    if (songDataBaseVersion < 11) {
                        getSongDao().executeRaw("ALTER TABLE `song` ADD COLUMN favourites INTEGER");
                    }
                    if (songDataBaseVersion < 12) {
                        getSongDao().executeRaw("ALTER TABLE `song` ADD COLUMN asDeleted BOOLEAN");
                    }
                    if (songDataBaseVersion < 13) {
                        getSongDao().executeRaw("ALTER TABLE `song` ADD COLUMN savedOnlyToDevice BOOLEAN");
                    }
                }
                int songVerseDataBaseVersion = sharedPreferences.getInt("songVerseDataBaseVersion", 0);
                if (songVerseDataBaseVersion < 4) {
                    TableUtils.dropTable(connectionSource, SongVerse.class, true);
                }
                if (songVerseDataBaseVersion < 5) {
                    getSongVerseDao().executeRaw("ALTER TABLE `songVerse` ADD COLUMN sectionTypeData INTEGER");
                }
                int languageDataBaseVersion = sharedPreferences.getInt("languageDataBaseVersion", 0);
                if (languageDataBaseVersion < 4) {
                    TableUtils.dropTable(connectionSource, Language.class, true);
                }
                if (languageDataBaseVersion < 7) {
                    executeSafe(getLanguageDao(), "ALTER TABLE `language` ADD COLUMN favouriteSongDate DATETIME");
                }
                if (languageDataBaseVersion < 9) {
                    executeSafe(getLanguageDao(), "ALTER TABLE `language` ADD COLUMN selectedForDownload BOOLEAN");
                }
                int songCollectionDataBaseVersion = sharedPreferences.getInt("songCollectionDataBaseVersion", 0);
                if (songCollectionDataBaseVersion < 5) {
                    TableUtils.dropTable(connectionSource, SongCollection.class, true);
                }
                int songCollectionElementDataBaseVersion = sharedPreferences.getInt("songCollectionElementDataBaseVersion", 0);
                if (songCollectionElementDataBaseVersion < 5) {
                    TableUtils.dropTable(connectionSource, SongCollectionElement.class, true);
                }
                int favouriteSongDataBaseVersion = sharedPreferences.getInt("favouriteSongDataBaseVersion", 0);
                if (favouriteSongDataBaseVersion < 0) {
                    TableUtils.dropTable(connectionSource, FavouriteSong.class, true);
                }
                if (favouriteSongDataBaseVersion == 1) {
                    getFavouriteSongDao().executeRaw("ALTER TABLE `favouriteSong` ADD COLUMN uploadedToServer BOOLEAN");
                }
                int queueSongDataBaseVersion = sharedPreferences.getInt("queueSongDataBaseVersion", 0);
                if (queueSongDataBaseVersion < 0) {
                    TableUtils.dropTable(connectionSource, QueueSong.class, true);
                }
            }
        } catch (final Exception e) {
            Log.e(TAG, "Unable to upgrade database from version " + oldVer + " to new " + newVer, e);
        }
        try {
            onCreate(sqliteDatabase, connectionSource);
        } catch (final Exception e) {
            Log.e(TAG, "Unable to create databases", e);
        }
    }

    private void executeSafe(CustomDao<Language, Long> dao, String statement) {
        try {
            dao.executeRaw(statement);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public CustomDao<Song, Long> getSongDao() throws SQLException {
        if (songDao == null) {
            songDao = getCustomDao(Song.class);
        }
        return songDao;
    }

    private <T> CustomDao<T, Long> getCustomDao(Class<T> aClass) throws SQLException {
        return new CustomDao<>(getDao(aClass));
    }

    public CustomDao<SongVerse, Long> getSongVerseDao() throws SQLException {
        if (songVerseDao == null) {
            songVerseDao = getCustomDao(SongVerse.class);
        }
        return songVerseDao;
    }

    public CustomDao<Language, Long> getLanguageDao() throws SQLException {
        if (languageDao == null) {
            languageDao = getCustomDao(Language.class);
        }
        return languageDao;
    }

    public CustomDao<SongCollection, Long> getSongCollectionDao() throws SQLException {
        if (songCollectionDao == null) {
            songCollectionDao = getCustomDao(SongCollection.class);
        }
        return songCollectionDao;
    }

    public CustomDao<SongCollectionElement, Long> getSongCollectionElementDao() throws SQLException {
        if (songCollectionElementDao == null) {
            songCollectionElementDao = getCustomDao(SongCollectionElement.class);
        }
        return songCollectionElementDao;
    }

    public CustomDao<FavouriteSong, Long> getFavouriteSongDao() throws SQLException {
        if (favouriteSongDao == null) {
            favouriteSongDao = new CustomDao<>(getDao(FavouriteSong.class));
        }
        return favouriteSongDao;
    }

    public CustomDao<QueueSong, Long> getQueueSongDao() throws SQLException {
        if (queueSongDao == null) {
            queueSongDao = getCustomDao(QueueSong.class);
        }
        return queueSongDao;
    }

    public CustomDao<SongList, Long> getSongListDao() throws SQLException {
        if (songListDao == null) {
            songListDao = getCustomDao(SongList.class);
        }
        return songListDao;
    }

    public CustomDao<SongListElement, Long> getSongListElementDao() throws SQLException {
        if (songListElementDao == null) {
            songListElementDao = getCustomDao(SongListElement.class);
        }
        return songListElementDao;
    }

    public CustomDao<LoggedInUser, Long> getLoggedInUserDao() throws SQLException {
        if (loggedInUserDao == null) {
            loggedInUserDao = getCustomDao(LoggedInUser.class);
        }
        return loggedInUserDao;
    }

    @Override
    public void close() {
        super.close();
        songDao = null;
        songVerseDao = null;
        languageDao = null;
        songCollectionDao = null;
        songCollectionElementDao = null;
        favouriteSongDao = null;
        queueSongDao = null;
        songListDao = null;
        songListElementDao = null;
        loggedInUserDao = null;
    }
}
