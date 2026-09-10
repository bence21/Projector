package com.bence.songbook.repository.impl.ormLite;

import android.content.Context;
import android.util.Log;

import com.bence.songbook.models.FavouriteSong;
import com.bence.songbook.models.Song;
import com.bence.songbook.repository.DatabaseHelper;
import com.bence.songbook.repository.FavouriteSongRepository;
import com.bence.songbook.repository.SongRepository;
import com.bence.songbook.repository.dao.CustomDao;
import com.bence.songbook.repository.exception.RepositoryException;

import java.sql.SQLException;
import java.util.List;

public class FavouriteSongRepositoryImpl extends BaseRepositoryImpl<FavouriteSong> implements FavouriteSongRepository {
    private static final String TAG = FavouriteSongRepositoryImpl.class.getSimpleName();
    private final CustomDao<FavouriteSong, Long> favouriteSongDao;
    private final Context context;
    private SongRepository songRepository;

    public FavouriteSongRepositoryImpl(Context context) {
        super(FavouriteSong.class);
        this.context = context;
        try {
            DatabaseHelper databaseHelper = DatabaseHelper.getInstance(context);
            favouriteSongDao = databaseHelper.getFavouriteSongDao();
            super.setDao(favouriteSongDao);
        } catch (SQLException e) {
            String msg = "Failed to initialize FavouriteSongRepository";
            Log.e(TAG, msg);
            throw new RepositoryException(msg, e);
        }
    }

    /**
     * Looks up a favourite row by the local {@code songs} row id (column {@code song_id}).
     * This is the authoritative lookup for the UNIQUE constraint and works when the song has no server UUID.
     */
    private FavouriteSong findFavouriteByLocalSongId(Long localSongId) {
        if (localSongId == null) {
            return null;
        }
        String msg = "Could not find favouriteSong by song_id";
        try {
            List<FavouriteSong> favouriteSongs = favouriteSongDao.queryForEq("song_id", localSongId);
            if (favouriteSongs != null && !favouriteSongs.isEmpty()) {
                return favouriteSongs.get(0);
            }
            return null;
        } catch (SQLException e) {
            Log.e(TAG, msg, e);
            throw new RepositoryException(msg, e);
        }
    }

    @Override
    public FavouriteSong findFavouriteSongBySongUuid(String uuid) {
        if (uuid == null) {
            return null;
        }
        String msg = "Could not find favouriteSong";
        try {
            songRepository = getSongRepository();
            Song byUUID = songRepository.findByUUID(uuid);
            if (byUUID != null) {
                return findFavouriteByLocalSongId(byUUID.getId());
            }
            return null;
        } catch (Exception e) {
            Log.e(TAG, msg, e);
            throw new RepositoryException(msg, e);
        }
    }

    private SongRepository getSongRepository() {
        if (songRepository == null) {
            songRepository = new SongRepositoryImpl(context);
        }
        return songRepository;
    }

    @Override
    public void save(FavouriteSong favourite) {
        if (favourite == null) {
            return;
        }
        Song song = favourite.getSong();
        if (song == null) {
            return;
        }

        // Always fetch the song by UUID to ensure we have the LOCAL database version
        // This prevents Foreign Key errors if the incoming song has an ID not in this DB
        SongRepository songRepo = getSongRepository();
        Song localSong = null;

        // First attempt: Try to find by UUID if UUID is available
        String songUuid = song.getUuid();
        if (songUuid != null && !songUuid.trim().isEmpty()) {
            localSong = songRepo.findByUUID(songUuid);
        }

        // Fallback: If UUID lookup failed or UUID is null/empty, try finding by ID
        // This handles locally created songs that haven't been uploaded to the server yet
        if (localSong == null && song.getId() != null) {
            localSong = songRepo.findOne(song.getId());
        }

        if (localSong == null) {
            // Song not found locally - tried both UUID and ID lookup
            if (songUuid != null && !songUuid.trim().isEmpty()) {
                Log.e(TAG, "Cannot save Favourite: Song with UUID " + songUuid + " does not exist locally.");
            } else if (song.getId() != null) {
                Log.e(TAG, "Cannot save Favourite: Song with ID " + song.getId() + " does not exist locally (song has no UUID).");
            } else {
                Log.e(TAG, "Cannot save Favourite: Song has neither UUID nor ID, cannot be found locally.");
            }
            return;
        }
        if (localSong.getId() == null) {
            // Song was found but has no valid ID - this shouldn't happen for persisted songs
            String identifier = (songUuid != null && !songUuid.trim().isEmpty())
                    ? "UUID " + songUuid
                    : "ID " + song.getId();
            Log.e(TAG, "Cannot save Favourite: Song with " + identifier + " exists locally but has no valid ID (required for foreign key reference).");
            return;
        }

        // Attach the verified local song to the favourite object
        favourite.setSong(localSong);

        // Like before: look up an existing row by the local song's server UUID when present
        FavouriteSong existing = findFavouriteSongBySongUuid(localSong.getUuid());
        if (existing == null) {
            // Local-only or missing UUID: unique row is keyed by song_id
            existing = findFavouriteByLocalSongId(localSong.getId());
        }
        if (existing != null) {
            favourite.setId(existing.getId());
        }

        super.save(favourite);
    }

    @Override
    public void save(List<FavouriteSong> models) {
        for (FavouriteSong favourite : models) {
            save(favourite);
        }
    }
}
