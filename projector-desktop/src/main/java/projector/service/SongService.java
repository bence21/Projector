package projector.service;

import com.bence.projector.common.dto.SongFavouritesDTO;
import com.bence.projector.common.dto.SongViewsDTO;
import projector.model.Language;
import projector.model.Song;

import java.util.List;

public interface SongService extends CrudService<Song> {
    Song findByTitle(String title);

    List<Song> findAllByVersionGroup(String versionGroup);

    void saveViews(List<SongViewsDTO> songViewsDTOS);

    void saveFavouriteCount(List<SongFavouritesDTO> songFavouritesDTOS);

    Song getFromMemoryOrSong(Song song);

    Song getFromMemoryOrSongNoUpdate(Song song);

    long countByLanguage(Language language);

    List<Song> findByLanguage(Language language) throws ServiceException;

    Song findMirrorByUuid(String serverUuid);

    Song findForkByOriginalUuid(String serverUuid);

    Song findForkForSong(Song song);

    long getRelevanceScore(Song song);

    boolean hasLocalFork(Song song);

    boolean hasLocalChanges(Song fork);

    Song createForkForEdit(Song source) throws ServiceException;

    void revertToOriginal(Song fork) throws ServiceException;

    void updateMirrorFromServer(Song mirror, Song serverSong) throws ServiceException;

    void deleteMirrorOnServerDelete(String serverUuid) throws ServiceException;

    boolean songsContentEquals(Song a, Song b);

    boolean isLegacyMigrationCandidate(Song song);

    boolean isLocallyModified(Song local, Song server);

    void migrateLegacySong(Song local, Song server) throws ServiceException;

    ForkMirrorMigrationResult migrateLegacySongsForLanguage(Language language, List<Song> serverSongs) throws ServiceException;

    ForkMirrorMigrationResult migrateLegacySongsForLanguage(Language language, List<Song> serverSongs, Runnable onSongProcessed) throws ServiceException;

    void rebuildForkIndex() throws ServiceException;
}
