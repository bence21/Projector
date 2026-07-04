package projector.service;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.table.TableUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import projector.application.ApplicationVersion;
import projector.model.Language;
import projector.model.Song;
import projector.model.SongVerse;
import projector.repository.DAOFactory;
import projector.repository.SongDAO;
import projector.repository.ormLite.DatabaseHelper;
import projector.service.impl.LanguageServiceImpl;
import projector.service.impl.SongServiceImpl;
import projector.utils.AppProperties;
import projector.utils.DownloadedSongLanguageSupport;
import projector.utils.MissingServerSongImporter;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class ForkMirrorMigrationLanguageTest {

    private LanguageServiceImpl languageService;
    private SongServiceImpl songService;
    private String savedDatabaseVersion;
    private Path databaseFolder;
    private DatabaseHelper databaseHelper;

    @Before
    public void setUp() throws Exception {
        ApplicationVersion.getInstance().setTesting(true);
        databaseFolder = Path.of(AppProperties.getInstance().getDatabaseFolder());
        Files.createDirectories(databaseFolder);
        savedDatabaseVersion = readDatabaseVersion();
        writeDatabaseVersion("0");
        deleteTempDatabaseFiles();

        DatabaseHelper.freeze();
        databaseHelper = DatabaseHelper.getInstance();
        ensureTestTables();

        languageService = new LanguageServiceImpl();
        songService = new SongServiceImpl();
    }

    @After
    public void tearDown() throws Exception {
        ApplicationVersion.getInstance().setTesting(false);
        if (databaseHelper != null) {
            databaseHelper.close();
            databaseHelper = null;
        }
        DatabaseHelper.unfreeze();
        deleteTempDatabaseFilesQuietly();
        if (savedDatabaseVersion == null) {
            Files.deleteIfExists(databaseFolder.resolve("database.version"));
        } else {
            writeDatabaseVersion(savedDatabaseVersion);
        }
    }

    @Test
    public void migrateLegacySongsForLanguage_preservesLanguageWhenServerSongsHaveLanguageAttached() throws Exception {
        Language language = createLanguage();
        List<Song> localSongs = createLegacyLocalSongs(language, 3);
        language.setSongs(localSongs);

        long initialCount = songService.countByLanguage(language);
        Assert.assertEquals(3, initialCount);

        List<Song> serverSongs = createServerSongs(localSongs);
        DownloadedSongLanguageSupport.attachLanguage(serverSongs, language);

        songService.migrateLegacySongsForLanguage(language, serverSongs);

        Assert.assertEquals(initialCount, songService.countByLanguage(language));
        Assert.assertEquals(0, countUnassignedSongs());
    }

    @Test(expected = AssertionError.class)
    public void migrateLegacySongsForLanguage_failsWhenServerSongsMissingLanguage() throws Exception {
        Language language = createLanguage();
        List<Song> localSongs = createLegacyLocalSongs(language, 1);
        language.setSongs(localSongs);

        List<Song> serverSongs = createServerSongs(localSongs);
        songService.migrateLegacySongsForLanguage(language, serverSongs);
    }

    @Test
    public void migrateLegacySongsForLanguage_unpublishedLocalCopy_createsForkAndMirror() throws Exception {
        Language language = createLanguage();
        Song local = new Song();
        String serverUuid = UUID.randomUUID().toString();
        local.setUuid(serverUuid);
        local.setTitle("Local edited title");
        local.setPublished(false);
        local.setPublish(true);
        local.setServerMirror(false);
        local.setLanguage(language);
        local.setModifiedDate(new Date(3_000_000L));
        local.setVerses(List.of(verse("Local edited verse")));
        local = songService.create(local);
        language.setSongs(List.of(local));

        Song server = new Song();
        server.setUuid(serverUuid);
        server.setTitle("Server title");
        server.setModifiedDate(new Date(2_000_000L));
        server.setServerModifiedDate(new Date(2_000_000L));
        server.setVerses(List.of(verse("Server verse")));
        List<Song> serverSongs = List.of(server);
        DownloadedSongLanguageSupport.attachLanguage(serverSongs, language);

        Assert.assertTrue(songService.isLegacyMigrationCandidate(local));

        ForkMirrorMigrationResult result = songService.migrateLegacySongsForLanguage(language, serverSongs);
        Assert.assertEquals(1, result.getForksCreated());

        Song mirror = songService.findMirrorByUuid(serverUuid);
        Assert.assertNotNull(mirror);
        Assert.assertTrue(mirror.isServerMirror());
        Assert.assertTrue(mirror.isPublished());
        Assert.assertTrue(songService.songsContentEquals(mirror, server));

        Song fork = songService.findForkByOriginalUuid(serverUuid);
        Assert.assertNotNull(fork);
        Assert.assertTrue(fork.isFork());
        Assert.assertFalse(fork.isPublished());
        Assert.assertEquals("Local edited title", fork.getTitle());
        Assert.assertEquals("Local edited verse", fork.getVerses().get(0).getText());

        SongDAO songDao = DAOFactory.getInstance().getSongDAO();
        Song forkFromDb = songDao.findById(fork.getId());
        Assert.assertEquals(1, forkFromDb.getVerses().size());
        Assert.assertEquals("Local edited verse", forkFromDb.getVerses().get(0).getText());

        Song mirrorFromDb = songDao.findById(mirror.getId());
        Assert.assertEquals(1, mirrorFromDb.getVerses().size());
        Assert.assertEquals("Server verse", mirrorFromDb.getVerses().get(0).getText());
    }

    @Test
    public void migrateLegacySongsForLanguage_unpublishedMatchingServer_becomesMirrorOnly() throws Exception {
        Language language = createLanguage();
        Song local = new Song();
        String serverUuid = UUID.randomUUID().toString();
        local.setUuid(serverUuid);
        local.setTitle("Shared title");
        local.setPublished(false);
        local.setPublish(true);
        local.setServerMirror(false);
        local.setLanguage(language);
        local.setModifiedDate(new Date(1_000_000L));
        local.setVerses(List.of(verse("Shared verse")));
        local = songService.create(local);
        language.setSongs(List.of(local));

        Song server = new Song();
        server.setUuid(serverUuid);
        server.setTitle("Shared title");
        server.setModifiedDate(new Date(2_000_000L));
        server.setServerModifiedDate(new Date(2_000_000L));
        server.setVerses(List.of(verse("Shared verse")));
        List<Song> serverSongs = List.of(server);
        DownloadedSongLanguageSupport.attachLanguage(serverSongs, language);

        ForkMirrorMigrationResult result = songService.migrateLegacySongsForLanguage(language, serverSongs);
        Assert.assertEquals(1, result.getMirrorsMarked());
        Assert.assertEquals(0, result.getForksCreated());

        Song mirror = songService.findMirrorByUuid(serverUuid);
        Assert.assertNotNull(mirror);
        Assert.assertEquals(local.getId(), mirror.getId());
        Assert.assertTrue(mirror.isServerMirror());
        Assert.assertTrue(mirror.isPublished());
        Assert.assertNull(songService.findForkByOriginalUuid(serverUuid));

        SongDAO songDao = DAOFactory.getInstance().getSongDAO();
        Song mirrorFromDb = songDao.findById(mirror.getId());
        Assert.assertEquals(1, mirrorFromDb.getVerses().size());
        Assert.assertEquals("Shared verse", mirrorFromDb.getVerses().get(0).getText());
    }

    @Test
    public void findMissingServerSongs_afterMigration_includesServerOnlySongs() throws Exception {
        Language language = createLanguage();
        List<Song> localSongs = createLegacyLocalSongs(language, 2);
        language.setSongs(localSongs);

        List<Song> serverSongs = createServerSongs(localSongs);
        Song serverOnly = new Song();
        serverOnly.setUuid(UUID.randomUUID().toString());
        serverOnly.setTitle("Server only");
        serverOnly.setModifiedDate(new Date(3_000_000L));
        serverOnly.setServerModifiedDate(new Date(3_000_000L));
        serverOnly.setVerses(List.of(verse("Server only verse")));
        serverSongs.add(serverOnly);

        DownloadedSongLanguageSupport.attachLanguage(serverSongs, language);
        songService.migrateLegacySongsForLanguage(language, serverSongs);

        List<Song> allLocalSongs = songService.findAll();
        List<Song> missing = MissingServerSongImporter.findMissingServerSongs(allLocalSongs, serverSongs);
        Assert.assertEquals(1, missing.size());
        Assert.assertEquals(serverOnly.getUuid(), missing.get(0).getUuid());

        Song imported = missing.get(0);
        imported.setLanguage(language);
        imported.setPublished(true);
        imported.setServerMirror(true);
        songService.create(imported);

        Assert.assertEquals(3, songService.countByLanguage(language));
        Assert.assertEquals(0, countUnassignedSongs());
    }

    @Test
    public void updateSongMetadataWithoutLoadingVerses_preservesVersesInDatabase() throws Exception {
        Language language = createLanguage();
        Song local = new Song();
        String serverUuid = UUID.randomUUID().toString();
        local.setUuid(serverUuid);
        local.setTitle("Title");
        local.setPublished(true);
        local.setLanguage(language);
        local.setVerses(List.of(verse("Verse text")));
        local = songService.create(local);

        SongDAO songDao = DAOFactory.getInstance().getSongDAO();
        Song fresh = songDao.findById(local.getId());
        fresh.setServerMirror(true);
        songService.update(fresh);

        Song reloaded = songDao.findById(local.getId());
        Assert.assertEquals(1, reloaded.getVerses().size());
        Assert.assertEquals("Verse text", reloaded.getVerses().get(0).getText());
    }

    @Test
    public void importMissingServerSong_whenForkExists_createsMirrorWithPersistedVerses() throws Exception {
        Language language = createLanguage();
        String serverUuid = UUID.randomUUID().toString();

        Song fork = new Song();
        fork.setUuid(UUID.randomUUID().toString());
        fork.setOriginalSongUuid(serverUuid);
        fork.setTitle("Local edited title");
        fork.setPublished(false);
        fork.setPublish(true);
        fork.setLanguage(language);
        fork.setVerses(List.of(verse("Local edited verse")));
        fork = songService.create(fork);

        Song server = new Song();
        server.setUuid(serverUuid);
        server.setTitle("Server title");
        server.setModifiedDate(new Date(2_000_000L));
        server.setServerModifiedDate(new Date(2_000_000L));
        server.setVerses(List.of(verse("Server verse one"), verse("Server verse two")));
        DownloadedSongLanguageSupport.attachLanguage(List.of(server), language);

        List<Song> missing = MissingServerSongImporter.findMissingServerSongs(songService.findAll(), List.of(server));
        Assert.assertEquals(1, missing.size());

        Song mirrorCopy = new Song(server);
        mirrorCopy.setServerMirror(true);
        mirrorCopy.setPublished(true);
        mirrorCopy.setLanguage(language);
        Song mirror = songService.create(mirrorCopy);

        Assert.assertEquals(2, mirror.getVerses().size());
        songService.updateMirrorFromServer(mirror, server);

        SongDAO songDao = DAOFactory.getInstance().getSongDAO();
        Song mirrorFromDb = songDao.findById(mirror.getId());
        Assert.assertEquals(2, mirrorFromDb.getVerses().size());
        Assert.assertEquals("Server verse one", mirrorFromDb.getVerses().get(0).getText());

        Song forkFromDb = songDao.findById(fork.getId());
        Assert.assertEquals(1, forkFromDb.getVerses().size());
    }

    private static void ensureTestTables() throws SQLException {
        DatabaseHelper databaseHelper = DatabaseHelper.getInstance();
        var connectionSource = databaseHelper.getSongDao().getConnectionSource();
        TableUtils.createTableIfNotExists(connectionSource, Language.class);
        TableUtils.createTableIfNotExists(connectionSource, Song.class);
        TableUtils.createTableIfNotExists(connectionSource, SongVerse.class);
    }

    private String readDatabaseVersion() throws Exception {
        Path versionFile = databaseFolder.resolve("database.version");
        if (!Files.exists(versionFile)) {
            return null;
        }
        return Files.readString(versionFile, StandardCharsets.UTF_8).trim();
    }

    private void writeDatabaseVersion(String version) throws Exception {
        Files.writeString(databaseFolder.resolve("database.version"), version, StandardCharsets.UTF_8);
    }

    private void deleteTempDatabaseFiles() {
        deleteTempDatabaseFilesQuietly();
    }

    private void deleteTempDatabaseFilesQuietly() {
        try {
            Files.deleteIfExists(databaseFolder.resolve("temp.mv.db"));
            Files.deleteIfExists(databaseFolder.resolve("temp.trace.db"));
        } catch (Exception ignored) {
        }
    }

    private Language createLanguage() {
        Language language = new Language();
        language.setUuid(UUID.randomUUID().toString());
        language.setNativeName("Magyar");
        language.setEnglishName("Hungarian");
        return languageService.create(language);
    }

    private List<Song> createLegacyLocalSongs(Language language, int count) {
        List<Song> songs = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            Song song = new Song();
            song.setUuid(UUID.randomUUID().toString());
            song.setTitle("Local title " + i);
            song.setPublished(true);
            song.setServerMirror(false);
            song.setLanguage(language);
            song.setModifiedDate(new Date(1_000_000L + i));
            song.setVerses(List.of(verse("Local verse " + i)));
            songs.add(songService.create(song));
        }
        return songs;
    }

    private List<Song> createServerSongs(List<Song> localSongs) {
        List<Song> serverSongs = new ArrayList<>(localSongs.size());
        for (int i = 0; i < localSongs.size(); i++) {
            Song local = localSongs.get(i);
            Song server = new Song();
            server.setUuid(local.getUuid());
            server.setTitle("Server title " + i);
            server.setModifiedDate(new Date(2_000_000L + i));
            server.setServerModifiedDate(new Date(2_000_000L + i));
            server.setVerses(List.of(verse("Server verse " + i)));
            serverSongs.add(server);
        }
        return serverSongs;
    }

    private static SongVerse verse(String text) {
        SongVerse songVerse = new SongVerse();
        songVerse.setText(text);
        return songVerse;
    }

    private long countUnassignedSongs() throws SQLException {
        Dao<Song, Long> songDao = DatabaseHelper.getInstance().getSongDao();
        return songDao.queryBuilder().where().isNull("LANGUAGE_ID").countOf();
    }
}
