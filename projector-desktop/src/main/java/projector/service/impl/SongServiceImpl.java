package projector.service.impl;

import com.bence.projector.common.dto.SongFavouritesDTO;
import com.bence.projector.common.dto.SongViewsDTO;
import com.j256.ormlite.dao.Dao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import projector.model.FavouriteSong;
import projector.model.Language;
import projector.model.Song;
import projector.model.SongVerse;
import projector.repository.DAOFactory;
import projector.repository.RepositoryException;
import projector.repository.SongDAO;
import projector.repository.ormLite.DatabaseHelper;
import projector.service.FavouriteSongService;
import projector.service.ServiceException;
import projector.service.ServiceManager;
import projector.service.ForkMirrorMigrationResult;
import projector.service.SongService;
import projector.service.SongVerseService;
import projector.utils.ForkMirrorMigrationState;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import static projector.repository.ormLite.VerseIndexRepositoryImpl.countByField;

public class SongServiceImpl extends AbstractBaseService<Song> implements SongService {

    private final static Logger LOG = LoggerFactory.getLogger(SongServiceImpl.class);
    private final SongDAO songDAO = DAOFactory.getInstance().getSongDAO();
    private final Dao<Song, Long> dao;
    private final HashMap<Long, Song> hashMap = new HashMap<>();
    private final HashMap<String, Song> forkByOriginalUuidCache = new HashMap<>();
    private boolean forkIndexBuilt = false;
    @SuppressWarnings("FieldCanBeLocal")
    private final String TABLE_NAME = "SONG";

    public SongServiceImpl() throws SQLException {
        super(DAOFactory.getInstance().getSongDAO());
        dao = DatabaseHelper.getInstance().getSongDao();
    }

    @Override
    public Song findByUuid(String uuid) {
        Song song = super.findByUuid(uuid);
        return getFromMemoryOrSong(song);
    }

    private Song getSongFromHashMap(Song song, boolean updateMap) {
        if (song == null) {
            return null;
        }
        Long id = song.getId();
        if (hashMap.containsKey(id)) {
            return hashMap.get(id);
        } else if (updateMap) {
            hashMap.put(id, song);
        }
        return song;
    }

    @Override
    public Song findById(Long id) {
        Song song = super.findById(id);
        return getFromMemoryOrSong(song);
    }

    @Override
    public List<Song> findAll() throws ServiceException {
        try {
            List<Song> songList = songDAO.findAll();
            List<Song> songs = getSongsFromHashMap(songList);
            rebuildForkIndexFromSongs(songs);
            return songs;
        } catch (RepositoryException e) {
            LOG.error(e.getMessage(), e);
            throw new ServiceException(e.getMessage(), e);
        }
    }

    @Override
    public void rebuildForkIndex() throws ServiceException {
        try {
            List<Song> songList = songDAO.findAll();
            rebuildForkIndexFromSongs(getSongsFromHashMap(songList));
        } catch (RepositoryException e) {
            LOG.error(e.getMessage(), e);
            throw new ServiceException(e.getMessage(), e);
        }
    }

    private void rebuildForkIndexFromSongs(List<Song> allSongs) {
        Map<String, Song> byUuid = new HashMap<>(allSongs.size());
        for (Song song : allSongs) {
            song.clearForkLinks();
            if (song.getUuid() != null) {
                byUuid.put(song.getUuid(), song);
            }
        }
        forkByOriginalUuidCache.clear();
        for (Song fork : allSongs) {
            if (!fork.isFork()) {
                continue;
            }
            String originalUuid = fork.getOriginalSongUuid();
            if (originalUuid == null || originalUuid.trim().isEmpty()) {
                continue;
            }
            Song mirror = resolveMirrorInMemory(originalUuid, byUuid);
            if (mirror != null) {
                mirror.setLocalFork(fork);
            }
            fork.setLocalChangesCached(mirror == null || !songsContentEquals(fork, mirror));
            forkByOriginalUuidCache.put(originalUuid, fork);
        }
        forkIndexBuilt = true;
    }

    private static Song resolveMirrorInMemory(String serverUuid, Map<String, Song> byUuid) {
        if (serverUuid == null || serverUuid.trim().isEmpty()) {
            return null;
        }
        String currentUuid = serverUuid;
        Set<String> visited = new HashSet<>();
        while (currentUuid != null && !currentUuid.trim().isEmpty()) {
            if (!visited.add(currentUuid)) {
                return null;
            }
            Song song = byUuid.get(currentUuid);
            if (song == null) {
                return null;
            }
            if (!song.isFork()) {
                return song;
            }
            currentUuid = song.getOriginalSongUuid();
        }
        return null;
    }

    private Map<String, Song> buildUuidMapFromMemory() {
        Map<String, Song> byUuid = new HashMap<>(hashMap.size());
        for (Song song : hashMap.values()) {
            if (song.getUuid() != null) {
                byUuid.put(song.getUuid(), song);
            }
        }
        return byUuid;
    }

    private void refreshLocalChangesCached(Song fork) {
        if (fork == null || !fork.isFork()) {
            return;
        }
        Song mirror = resolveMirrorInMemory(fork.getOriginalSongUuid(), buildUuidMapFromMemory());
        fork.setLocalChangesCached(mirror == null || !songsContentEquals(fork, mirror));
    }

    private List<Song> getSongsFromHashMap(List<Song> songList) {
        List<Song> songs = new ArrayList<>(songList.size());
        for (Song song : songList) {
            songs.add(getFromMemoryOrSong(song));
        }
        return songs;
    }

    @Override
    public Song create(Song song) throws ServiceException {
        return updateSong(song);
    }

    private Song updateSong(Song song) {
        try {
            Song updatedSong = songDAO.create(song);
            if (updatedSong != null) {
                hashMap.put(updatedSong.getId(), updatedSong);
                if (updatedSong.isFork()) {
                    refreshLocalChangesCached(updatedSong);
                }
            }
            return updatedSong;
        } catch (RepositoryException e) {
            LOG.error(e.getMessage(), e);
            throw new ServiceException(e.getMessage(), e);
        }
    }

    @Override
    public Song update(Song song) throws ServiceException {
        return updateSong(song);
    }

    @Override
    public boolean delete(Song song) throws ServiceException {
        if (song == null) {
            return false;
        }
        try {
            boolean deleted = songDAO.delete(song);
            if (deleted) {
                if (song.getId() != null) {
                    hashMap.remove(song.getId());
                }
                rebuildForkIndex();
            }
            return deleted;
        } catch (RepositoryException e) {
            LOG.error(e.getMessage(), e);
            throw new ServiceException(e.getMessage(), e);
        }
    }

    @Override
    public boolean delete(List<Song> songs) throws ServiceException {
        try {
            boolean deleted = songDAO.deleteAll(songs);
            if (deleted) {
                for (Song song : songs) {
                    if (song != null && song.getId() != null) {
                        hashMap.remove(song.getId());
                    }
                }
                rebuildForkIndex();
            }
            return deleted;
        } catch (RepositoryException e) {
            LOG.error(e.getMessage(), e);
            throw new ServiceException(e.getMessage(), e);
        }
    }

    @Override
    public Song findByTitle(String title) {
        try {
            Song song = songDAO.findByTitle(title);
            return getFromMemoryOrSong(song);
        } catch (RepositoryException e) {
            LOG.error(e.getMessage(), e);
            throw new ServiceException(e.getMessage(), e);
        }
    }

    @Override
    public List<Song> findAllByVersionGroup(String versionGroup) {
        try {
            List<Song> songs = songDAO.findAllByVersionGroup(versionGroup);
            return getSongsFromHashMap(songs);
        } catch (RepositoryException e) {
            LOG.error(e.getMessage(), e);
            throw new ServiceException(e.getMessage(), e);
        }
    }

    @Override
    public void saveViews(List<SongViewsDTO> songViewsDTOS) {
        try {
            songDAO.saveViews(songViewsDTOS);
        } catch (RepositoryException e) {
            LOG.error(e.getMessage(), e);
            throw new ServiceException(e.getMessage(), e);
        }
    }

    @Override
    public void saveFavouriteCount(List<SongFavouritesDTO> songFavouritesDTOS) {
        try {
            songDAO.saveFavouriteCount(songFavouritesDTOS);
        } catch (RepositoryException e) {
            LOG.error(e.getMessage(), e);
            throw new ServiceException(e.getMessage(), e);
        }
    }

    @Override
    public Song getFromMemoryOrSong(Song song) {
        return getSongFromHashMap(song, true);
    }

    @Override
    public Song getFromMemoryOrSongNoUpdate(Song song) {
        return getSongFromHashMap(song, false);
    }

    @Override
    public long countByLanguage(Language language) {
        if (language == null || language.getId() == null) {
            return 0;
        }
        try {
            return dao.queryBuilder().where()
                    .eq("LANGUAGE_ID", language.getId())
                    .countOf();
        } catch (SQLException e) {
            LOG.error(e.getMessage(), e);
            return countByField(TABLE_NAME, "LANGUAGE_ID", language.getId(), dao);
        }
    }

    @Override
    public List<Song> findByLanguage(Language language) throws ServiceException {
        if (language == null || language.getId() == null) {
            return new ArrayList<>();
        }
        try {
            List<Song> songList = dao.queryBuilder().where()
                    .eq("LANGUAGE_ID", language.getId())
                    .query();
            return getSongsFromHashMap(songList);
        } catch (SQLException e) {
            LOG.error(e.getMessage(), e);
            throw new ServiceException(e.getMessage(), e);
        }
    }

    private static final long FORK_RELEVANCE_SCORE_BOOST = 12;

    @Override
    public long getRelevanceScore(Song song) {
        song = getFromMemoryOrSong(song);
        long score = song.getScore();
        if (song.isFork()) {
            Song mirror = findMirrorByUuid(song.getOriginalSongUuid());
            if (mirror != null) {
                score = Math.max(score, mirror.getScore());
            }
            score += FORK_RELEVANCE_SCORE_BOOST;
        }
        return score;
    }

    @Override
    public Song findForkForSong(Song song) {
        if (song == null) {
            return null;
        }
        song = getFromMemoryOrSong(song);
        if (song.isFork()) {
            return song;
        }
        if (forkIndexBuilt) {
            return song.getLocalFork();
        }
        return findForkByOriginalUuid(song.getServerSourceUuid());
    }

    @Override
    public boolean hasLocalFork(Song song) {
        if (song == null) {
            return false;
        }
        song = getFromMemoryOrSong(song);
        return !song.isFork() && song.hasLocalFork();
    }

    @Override
    public Song findMirrorByUuid(String serverUuid) {
        if (serverUuid == null || serverUuid.trim().isEmpty()) {
            return null;
        }
        String currentUuid = serverUuid;
        Set<String> visited = new HashSet<>();
        while (currentUuid != null && !currentUuid.trim().isEmpty()) {
            if (!visited.add(currentUuid)) {
                LOG.warn("Cycle detected while resolving mirror for uuid: {}", serverUuid);
                return null;
            }
            Song song = findByUuid(currentUuid);
            if (song == null) {
                return null;
            }
            if (!song.isFork()) {
                return song;
            }
            currentUuid = song.getOriginalSongUuid();
        }
        return null;
    }

    @Override
    public Song findForkByOriginalUuid(String serverUuid) {
        if (serverUuid == null || serverUuid.trim().isEmpty()) {
            return null;
        }
        if (forkByOriginalUuidCache.containsKey(serverUuid)) {
            return forkByOriginalUuidCache.get(serverUuid);
        }
        if (forkIndexBuilt) {
            return null;
        }
        Song fork = songDAO.findByOriginalSongUuid(serverUuid);
        if (fork != null) {
            fork = getFromMemoryOrSong(fork);
            forkByOriginalUuidCache.put(serverUuid, fork);
        }
        return fork;
    }

    @Override
    public boolean hasLocalChanges(Song fork) {
        if (fork == null || !fork.isFork()) {
            return false;
        }
        fork = getFromMemoryOrSong(fork);
        Boolean cached = fork.getLocalChangesCached();
        if (cached != null) {
            return cached;
        }
        Song mirror = findMirrorByUuid(fork.getOriginalSongUuid());
        if (mirror == null) {
            return true;
        }
        return !songsContentEquals(fork, mirror);
    }

    @Override
    public Song createForkForEdit(Song source) throws ServiceException {
        source = getFromMemoryOrSong(source);
        if (source.isFork()) {
            return source;
        }
        if (!source.isPublished()) {
            return source;
        }
        String serverUuid = source.getUuid();
        if (serverUuid == null || serverUuid.trim().isEmpty()) {
            return source;
        }
        Song existingFork = findForkByOriginalUuid(serverUuid);
        if (existingFork != null) {
            return existingFork;
        }
        Song mirror = findMirrorByUuid(serverUuid);
        if (mirror == null) {
            mirror = source;
        }
        mirror.setServerMirror(true);
        mirror.setPublished(true);
        update(mirror);

        Song fork = prepareForkForCreate(mirror, serverUuid);
        create(fork);
        transferFavourite(mirror, fork);
        rebuildForkIndex();
        return fork;
    }

    @Override
    public void revertToOriginal(Song fork) throws ServiceException {
        if (fork == null || !fork.isFork()) {
            return;
        }
        String serverUuid = fork.getOriginalSongUuid();
        Song mirror = findMirrorByUuid(serverUuid);
        transferFavourite(fork, mirror);
        delete(fork);
        if (mirror != null) {
            mirror.setServerMirror(false);
            update(mirror);
        }
    }

    @Override
    public void updateMirrorFromServer(Song mirror, Song serverSong) throws ServiceException {
        if (mirror == null || serverSong == null) {
            return;
        }
        SongVerseService songVerseService = ServiceManager.getSongVerseService();
        mirror.setServerModifiedDate(serverSong.getServerModifiedDate());
        mirror.setCreatedDate(serverSong.getCreatedDate());
        mirror.setModifiedDate(serverSong.getModifiedDate());
        mirror.setLanguage(serverSong.getLanguage());
        mirror.setVersionGroup(serverSong.getVersionGroup());
        songVerseService.delete(mirror.getVerses());
        mirror.setTitle(serverSong.getTitle());
        mirror.setVerses(serverSong.getVerses());
        mirror.setViews(serverSong.getViews());
        mirror.setFavouriteCount(serverSong.getFavouriteCount());
        mirror.setAuthor(serverSong.getAuthor());
        mirror.setVerseOrderList(serverSong.getVerseOrderList());
        mirror.setPublished(true);
        mirror.setDeleted(serverSong.isDeleted());
        update(mirror);
    }

    @Override
    public void deleteMirrorOnServerDelete(String serverUuid) throws ServiceException {
        Song mirror = findMirrorByUuid(serverUuid);
        if (mirror != null) {
            delete(mirror);
        }
    }

    private void transferFavourite(Song from, Song to) {
        if (from == null || to == null || Objects.equals(from.getId(), to.getId())) {
            return;
        }
        try {
            FavouriteSongService favouriteSongService = ServiceManager.getFavouriteSongService();
            List<FavouriteSong> favouriteSongs = favouriteSongService.findAll();
            for (FavouriteSong favouriteSong : favouriteSongs) {
                Song favouriteSongSong = favouriteSong.getSong();
                if (favouriteSongSong != null && from.getId().equals(favouriteSongSong.getId())) {
                    favouriteSong.setSong(to);
                    favouriteSongService.update(favouriteSong);
                    to.setFavourite(favouriteSong);
                    from.setFavourite(null);
                    break;
                }
            }
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
    }

    @Override
    public boolean songsContentEquals(Song a, Song b) {
        if (a == null || b == null) {
            return false;
        }
        if (!Objects.equals(a.getTitle(), b.getTitle())) {
            return false;
        }
        if (!languageEquals(a, b)) {
            return false;
        }
        if (!Objects.equals(a.getAuthor(), b.getAuthor())) {
            return false;
        }
        if (!Objects.equals(a.getVerseOrderList(), b.getVerseOrderList())) {
            return false;
        }
        List<SongVerse> versesA = a.getVerses();
        List<SongVerse> versesB = b.getVerses();
        if (versesA.size() != versesB.size()) {
            return false;
        }
        for (int i = 0; i < versesA.size(); i++) {
            if (!verseContentEquals(versesA.get(i), versesB.get(i))) {
                return false;
            }
        }
        return true;
    }

    private static boolean languageEquals(Song a, Song b) {
        Language languageA = a.getLanguage();
        Language languageB = b.getLanguage();
        if (languageA == null || languageB == null) {
            return languageA == languageB;
        }
        return Objects.equals(languageA.getId(), languageB.getId());
    }

    private static boolean verseContentEquals(SongVerse verseA, SongVerse verseB) {
        if (verseA == null || verseB == null) {
            return verseA == verseB;
        }
        return Objects.equals(verseA.getText(), verseB.getText())
                && Objects.equals(verseA.getSecondText(), verseB.getSecondText())
                && verseA.getSectionType() == verseB.getSectionType();
    }

    @Override
    public boolean isLegacyMigrationCandidate(Song song) {
        if (song == null) {
            return false;
        }
        song = getFromMemoryOrSong(song);
        String uuid = song.getUuid();
        if (uuid == null || uuid.trim().isEmpty()) {
            return false;
        }
        if (song.isFork() || song.isServerMirror()) {
            return false;
        }
        return findForkByOriginalUuid(uuid) == null;
    }

    @Override
    public boolean isLocallyModified(Song local, Song server) {
        if (local == null) {
            return false;
        }
        local = getFromMemoryOrSong(local);
        if (local.isPublish() && !local.isPublished()) {
            return true;
        }
        Date serverModifiedDate = local.getServerModifiedDate();
        Date localModifiedDate = local.getModifiedDate();
        return serverModifiedDate != null && localModifiedDate != null && localModifiedDate.after(serverModifiedDate);
    }

    @Override
    public void migrateLegacySong(Song local, Song server) throws ServiceException {
        if (local == null || server == null) {
            return;
        }
        local = getFromMemoryOrSong(local);
        String serverUuid = local.getUuid();
        if (songsContentEquals(local, server)) {
            local.setServerMirror(true);
            local.setPublished(true);
            if (server.getServerModifiedDate() != null) {
                local.setServerModifiedDate(server.getServerModifiedDate());
            }
            update(local);
            return;
        }
        if (isLocallyModified(local, server)) {
            Song localSnapshot = new Song(local);
            updateMirrorFromServer(local, server);
            local.setServerMirror(true);
            local.setPublished(true);
            update(local);
            createForkFromSnapshot(localSnapshot, serverUuid);
            transferFavourite(local, findForkByOriginalUuid(serverUuid));
            return;
        }
        updateMirrorFromServer(local, server);
        local.setServerMirror(true);
        local.setPublished(true);
        update(local);
    }

    @Override
    public ForkMirrorMigrationResult migrateLegacySongsForLanguage(Language language, List<Song> serverSongs) throws ServiceException {
        return migrateLegacySongsForLanguage(language, serverSongs, null);
    }

    @Override
    public ForkMirrorMigrationResult migrateLegacySongsForLanguage(Language language, List<Song> serverSongs, Runnable onSongProcessed) throws ServiceException {
        ForkMirrorMigrationResult result = new ForkMirrorMigrationResult();
        if (language == null || serverSongs == null) {
            return result;
        }
        Map<String, Song> serverByUuid = new HashMap<>(serverSongs.size());
        for (Song serverSong : serverSongs) {
            if (serverSong.getUuid() != null) {
                serverByUuid.put(serverSong.getUuid(), serverSong);
            }
        }
        for (Song local : language.getSongs()) {
            if (!isLegacyMigrationCandidate(local)) {
                continue;
            }
            String uuid = local.getUuid();
            Song server = serverByUuid.get(uuid);
            if (server == null) {
                result.incrementSkipped();
            } else {
                boolean contentEquals = songsContentEquals(local, server);
                boolean locallyModified = isLocallyModified(local, server);
                migrateLegacySong(local, server);
                if (contentEquals) {
                    result.incrementMirrorsMarked();
                } else if (locallyModified) {
                    result.incrementForksCreated();
                } else {
                    result.incrementServerUpdated();
                }
            }
            if (onSongProcessed != null) {
                onSongProcessed.run();
            }
        }
        String languageUuid = language.getUuid();
        if (languageUuid != null) {
            ForkMirrorMigrationState.markMigrated(languageUuid);
        }
        language.setSongs(null);
        rebuildForkIndex();
        return result;
    }

    private void createForkFromSnapshot(Song snapshot, String serverUuid) throws ServiceException {
        create(prepareForkForCreate(snapshot, serverUuid));
    }

    private Song prepareForkForCreate(Song source, String serverUuid) {
        Song fork = new Song(source);
        fork.setId(null);
        fork.setUuid(UUID.randomUUID().toString());
        fork.setOriginalSongUuid(serverUuid);
        fork.setServerMirror(false);
        fork.setPublished(false);
        fork.setPublish(source.isPublish());
        fork.setModifiedDate(new Date());
        fork.setLanguage(source.getLanguage());
        fork.setVerses(fork.getVerses());
        return fork;
    }
}
