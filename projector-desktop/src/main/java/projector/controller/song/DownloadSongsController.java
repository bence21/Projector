package projector.controller.song;

import com.bence.projector.common.dto.SongFavouritesDTO;
import com.bence.projector.common.dto.SongViewsDTO;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import projector.utils.SceneUtils;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import projector.api.RemoteFetchFailureKind;
import projector.api.RemoteFetchResult;
import projector.api.SongApiBean;
import projector.api.SongCollectionApiBean;
import projector.application.Settings;
import projector.model.Language;
import projector.model.Song;
import projector.model.SongCollection;
import projector.model.SongVerse;
import projector.service.LanguageService;
import projector.service.ServiceManager;
import projector.service.SongCollectionService;
import projector.service.ServiceException;
import projector.service.SongService;
import projector.service.SongVerseService;
import projector.utils.ConnectionErrorMessages;
import projector.utils.DevelopmentMode;
import projector.utils.DownloadedSongLanguageSupport;
import projector.utils.DownloadWorkPlan;
import projector.utils.ForkMirrorMigrationState;
import projector.utils.MissingServerSongImporter;
import projector.utils.StringUtils;
import projector.utils.compare.CompareDiffHighlighter;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.ResourceBundle;

public class DownloadSongsController {
    public static final Logger LOG = LoggerFactory.getLogger(DownloadSongsController.class);
    private final ResourceBundle resourceBundle = Settings.getInstance().getResourceBundle();
    private final SongVerseService songVerseService = ServiceManager.getSongVerseService();
    @FXML
    private GridPane conflictGridPane;
    @FXML
    private Label downloadingLabel;
    @FXML
    private Label migrationWarningLabel;
    @FXML
    private Label downloadCompleteHintLabel;
    @FXML
    private Button closeWindowButton;
    @FXML
    private ProgressBar downloadProgressBar;
    @FXML
    private Label downloadProgressCountLabel;
    @FXML
    private Label downloadPlaceholderLabel;
    @FXML
    private ListView<Song> newSongListView;
    @FXML
    private Label conflictTitle;
    @FXML
    private TextFlow conflictSongTextFlow;
    @FXML
    private TextFlow conflictLocalSongTextFlow;
    @FXML
    private Button acceptButton;
    @FXML
    private Button acceptBothButton;
    @FXML
    private Button keepButton;
    @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
    private List<Song> conflictSongList;
    @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
    private List<Song> conflictLocalSongList;
    private int conflictIndex = 0;
    private SongService songService;
    private List<SongCollection> onlineModifiedSongCollections;
    private List<Language> languages;
    private int remainingLanguages;
    private int totalSongs;
    private int completedSongs;
    private boolean downloadComplete;
    private List<Song> songs;
    private DownloadWorkPlan downloadWorkPlan;
    private int pendingMigrationLanguages;

    public void initialize() {
        setConflictPaneVisible(false);
        if (downloadPlaceholderLabel != null) {
            downloadPlaceholderLabel.setText(resourceBundle.getString("Download in progress"));
        }
        newSongListView.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(Song item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null || item.getTitle() == null) {
                    setText(null);
                } else {
                    String text = item.getTitle();
                    setText(text);
                }
            }
        });
        conflictSongList = new ArrayList<>();
        conflictLocalSongList = new ArrayList<>();
        songService = ServiceManager.getSongService();
        songs = songService.findAll();
        LanguageService languageService = ServiceManager.getLanguageService();
        languages = languageService.findAll();
        hideMigrationWarningLabel();
        initializeButtons();
    }

    public void startDownload(DownloadWorkPlan downloadWorkPlan) {
        this.downloadWorkPlan = downloadWorkPlan;
        LanguageService languageService = ServiceManager.getLanguageService();
        Thread thread = new Thread(() -> {
            SongApiBean songApi = new SongApiBean();
            remainingLanguages = getSelectedLanguageSize();
            pendingMigrationLanguages = countPendingMigrationLanguages();
            initDownloadProgress();
            for (Language language : languages) {
                if (language.isSelected()) {
                    if (!migrateLegacySongsForLanguageIfNeeded(songApi, language)) {
                        return;
                    }
                    updateDownloadStatus(resourceBundle.getString("Downloading") + ": " + language.getNativeName());
                    final SongApiBean songApiBean = new SongApiBean();
                    List<SongViewsDTO> songViewsDTOS = songApiBean.getSongViewsByLanguage(language);
                    if (songViewsDTOS != null) {
                        songService.saveViews(songViewsDTOS);
                    }
                    List<SongFavouritesDTO> songFavouritesDTOS = songApiBean.getSongFavouritesByLanguage(language);
                    if (songFavouritesDTOS != null) {
                        songService.saveFavouriteCount(songFavouritesDTOS);
                    }
                    List<Song> newSongList = new ArrayList<>();
                    RemoteFetchResult<List<Song>> songsResult = songApi.getSongsByLanguageAndAfterModifiedDateResult(
                            language, getLastModifiedSongDate(language));
                    if (!songsResult.isSuccess()) {
                        showRemoteFetchFailure(songsResult.getFailureKind(), true);
                        return;
                    }
                    final List<Song> songApiSongs = songsResult.getData();
                    int downloadedCount = songApiSongs != null ? songApiSongs.size() : 0;
                    if (songApiSongs != null && !songApiSongs.isEmpty()) {
                        updateDownloadStatus(resourceBundle.getString("Saving") + ": " + language.getNativeName());
                        adjustDownloadEstimate(language, songApiSongs.size());
                            HashMap<String, Song> uuidSongHashMap = new HashMap<>(songs.size());
                            for (Song song : songs) {
                                if (song.getUuid() != null) {
                                    uuidSongHashMap.put(song.getUuid(), song);
                                }
                            }
                        DownloadedSongLanguageSupport.attachLanguage(songApiSongs, language);
                            for (Song song : songApiSongs) {
                                final String serverUuid = song.getUuid();
                                final boolean containsKeyInUuid = serverUuid != null && uuidSongHashMap.containsKey(serverUuid);
                                if (!containsKeyInUuid) {
                                    handleDownloadWithoutLocalUuid(song, newSongList);
                                } else {
                                    final Song localSong = uuidSongHashMap.get(serverUuid);
                                    updateExistingDownloadedSong(song, localSong);
                                }
                                incrementCompletedSongs(1);
                            }
                    }
                    reconcileDownloadEstimate(language, downloadedCount);
                    try {
                        SongCollectionApiBean songCollectionApiBean = new SongCollectionApiBean();
                        SongCollectionService songCollectionService = ServiceManager.getSongCollectionService();
                        List<SongCollection> songCollectionServiceAll = songCollectionService.findAllByLanguage(language);
                        Date lastModifiedDate = new Date(0);
                        for (SongCollection songCollection : songCollectionServiceAll) {
                            Date songCollectionModifiedDate = songCollection.getModifiedDate();
                            if (songCollectionModifiedDate.compareTo(lastModifiedDate) > 0) {
                                lastModifiedDate = songCollectionModifiedDate;
                            }
                        }
                        RemoteFetchResult<List<SongCollection>> collectionsResult =
                                songCollectionApiBean.getSongCollectionsResult(language, lastModifiedDate);
                        if (!collectionsResult.isSuccess()) {
                            showRemoteFetchFailure(collectionsResult.getFailureKind(), false);
                        } else {
                            onlineModifiedSongCollections = collectionsResult.getData();
                            saveSongCollections(songCollectionService, songCollectionServiceAll);
                        }
                    } catch (Exception e) {
                        LOG.error(e.getMessage(), e);
                    }
                    Platform.runLater(() -> {
                        setLanguageSectionTypeDownloadedCorrectly(language, languageService);
                        for (Song song : newSongList) {
                            newSongListView.getItems().add(song);
                        }
                        --remainingLanguages;
                        showConflictSong();
                    });
                }
            }
        });
        thread.start();
    }

    private void initDownloadProgress() {
        totalSongs = downloadWorkPlan != null ? downloadWorkPlan.getTotalWork() : 0;
        completedSongs = 0;
        downloadComplete = false;
        Platform.runLater(() -> {
            if (downloadProgressBar != null) {
                if (totalSongs <= 0) {
                    downloadProgressBar.setProgress(ProgressBar.INDETERMINATE_PROGRESS);
                } else {
                    downloadProgressBar.setProgress(0);
                    SceneUtils.setVisibleAndManaged(downloadProgressBar, true);
                }
            }
            updateDownloadProgressCountLabel(completedSongs, totalSongs);
        });
    }

    private void updateDownloadProgress(int completed, int total) {
        Platform.runLater(() -> {
            if (downloadProgressBar != null) {
                if (total <= 0) {
                    if (downloadComplete) {
                        hideDownloadProgress();
                    } else {
                        downloadProgressBar.setProgress(ProgressBar.INDETERMINATE_PROGRESS);
                        SceneUtils.setVisibleAndManaged(downloadProgressBar, true);
                    }
                } else {
                    downloadProgressBar.setProgress((double) completed / total);
                    SceneUtils.setVisibleAndManaged(downloadProgressBar, true);
                }
            }
            if (!downloadComplete || total > 0) {
                SceneUtils.setVisibleAndManaged(downloadProgressCountLabel, true);
                updateDownloadProgressCountLabel(completed, total);
            }
        });
    }

    private void adjustTotalSongs(int delta) {
        if (delta <= 0) {
            return;
        }
        totalSongs += delta;
        updateDownloadProgress(completedSongs, totalSongs);
    }

    private void adjustDownloadEstimate(Language language, int actualCount) {
        if (actualCount <= 0) {
            return;
        }
        if (downloadWorkPlan == null) {
            adjustTotalSongs(actualCount);
            return;
        }
        int estimated = downloadWorkPlan.getEstimatedDownloadSteps(language.getUuid());
        if (actualCount > estimated) {
            adjustTotalSongs(actualCount - estimated);
        }
    }

    private void reconcileDownloadEstimate(Language language, int actualCount) {
        if (downloadWorkPlan == null || actualCount < 0) {
            return;
        }
        int estimated = downloadWorkPlan.getEstimatedDownloadSteps(language.getUuid());
        if (actualCount < estimated) {
            int delta = estimated - actualCount;
            totalSongs = Math.max(completedSongs, totalSongs - delta);
            updateDownloadProgress(completedSongs, totalSongs);
        }
    }

    private void incrementCompletedSongs(int count) {
        if (count <= 0) {
            return;
        }
        completedSongs += count;
        updateDownloadProgress(completedSongs, totalSongs);
    }

    private void updateDownloadProgressCountLabel(int completed, int total) {
        if (downloadProgressCountLabel == null) {
            return;
        }
        if (total <= 0) {
            SceneUtils.setVisibleAndManaged(downloadProgressCountLabel, false);
            return;
        }
        SceneUtils.setVisibleAndManaged(downloadProgressCountLabel, true);
        if (DevelopmentMode.isActive()) {
            downloadProgressCountLabel.setText(
                    String.format(resourceBundle.getString("Download progress count"), completed, total));
        } else {
            int percent = (int) Math.round(100.0 * completed / total);
            downloadProgressCountLabel.setText(percent + "%");
        }
    }

    private void updateDownloadStatus(String text) {
        Platform.runLater(() -> downloadingLabel.setText(text));
    }

    private void setConflictPaneVisible(boolean visible) {
        SceneUtils.setVisibleAndManaged(conflictGridPane, visible);
        if (downloadPlaceholderLabel != null) {
            boolean showPlaceholder = !visible && !downloadComplete && remainingLanguages > 0;
            SceneUtils.setVisibleAndManaged(downloadPlaceholderLabel, showPlaceholder);
        }
    }

    private void hideMigrationWarningLabel() {
        if (migrationWarningLabel == null) {
            return;
        }
        SceneUtils.setVisibleAndManaged(migrationWarningLabel, false);
    }

    private void showMigrationInProgressWarning(boolean visible) {
        Platform.runLater(() -> {
            if (migrationWarningLabel == null) {
                return;
            }
            if (visible) {
                migrationWarningLabel.setText(resourceBundle.getString("Fork mirror migration in progress"));
            }
            SceneUtils.setVisibleAndManaged(migrationWarningLabel, visible);
        });
    }

    private int countPendingMigrationLanguages() {
        int count = 0;
        for (Language language : languages) {
            if (language.isSelected() && ForkMirrorMigrationState.needsMigration(language, songService)) {
                ++count;
            }
        }
        return count;
    }

    private void onMigrationLanguageFinished() {
        if (pendingMigrationLanguages > 0) {
            --pendingMigrationLanguages;
        }
        if (pendingMigrationLanguages <= 0) {
            showMigrationInProgressWarning(false);
        }
    }

    private boolean migrateLegacySongsForLanguageIfNeeded(SongApiBean songApi, Language language) {
        String languageUuid = language.getUuid();
        if (languageUuid == null || ForkMirrorMigrationState.isMigrated(languageUuid)) {
            return true;
        }
        showMigrationInProgressWarning(true);
        showForkMirrorMigrationProgress(language);
        RemoteFetchResult<List<Song>> fullServerResult = songApi.getSongsByLanguageAndAfterModifiedDateResult(language, 0L);
        if (!fullServerResult.isSuccess()) {
            showRemoteFetchFailure(fullServerResult.getFailureKind(), true);
            return false;
        }
        List<Song> fullServerSongs = fullServerResult.getData();
        DownloadedSongLanguageSupport.attachLanguage(fullServerSongs, language);
        try {
            songService.migrateLegacySongsForLanguage(language, fullServerSongs, () -> incrementCompletedSongs(1));
            songs = songService.findAll();
            List<Song> importedSongs = new ArrayList<>();
            int importedCount = importMissingServerSongs(language, fullServerSongs, importedSongs);
            if (importedCount > 0) {
                songs = songService.findAll();
                List<Song> importedForUi = new ArrayList<>(importedSongs);
                Platform.runLater(() -> newSongListView.getItems().addAll(importedForUi));
            }
            onMigrationLanguageFinished();
            return true;
        } catch (ServiceException e) {
            LOG.error(e.getMessage(), e);
            return false;
        }
    }

    private int importMissingServerSongs(Language language, List<Song> serverSongs, List<Song> newSongList) {
        List<Song> missingSongs = MissingServerSongImporter.findMissingServerSongs(songs, serverSongs);
        if (missingSongs.isEmpty()) {
            return 0;
        }
        DownloadedSongLanguageSupport.attachLanguage(missingSongs, language);
        adjustTotalSongs(missingSongs.size());
        for (Song serverSong : missingSongs) {
            handleDownloadWithoutLocalUuid(serverSong, newSongList);
            incrementCompletedSongs(1);
        }
        return missingSongs.size();
    }

    private void showForkMirrorMigrationProgress(Language language) {
        updateDownloadStatus(resourceBundle.getString("Checking local copies") + ": " + language.getNativeName());
    }

    private static void setLanguageSectionTypeDownloadedCorrectly(Language language, LanguageService languageService) {
        try {
            language.setSectionTypeDownloadedCorrectly(true);
            languageService.update(language);
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
    }

    private void saveSong(Song song) {
        try {
            songService.create(song);
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
    }

    private void handleDownloadWithoutLocalUuid(Song song, List<Song> newSongList) {
        final String serverUuid = song.getUuid();
        Song existingFork = songService.findForkByOriginalUuid(serverUuid);
        if (existingFork != null) {
            Song mirror = songService.findMirrorByUuid(serverUuid);
            if (mirror == null) { // original song somehow deleted locally
                Song mirrorCopy = new Song(song);
                mirrorCopy.setServerMirror(true);
                mirrorCopy.setPublished(true);
                saveSong(mirrorCopy);
            } else {
                try {
                    songService.updateMirrorFromServer(mirror, song);
                } catch (Exception e) {
                    LOG.error(e.getMessage(), e);
                }
            }
        } else if (!song.isDeleted()) {
            newSongList.add(song);
            saveSong(song);
        }
    }

    private void updateExistingDownloadedSong(Song song, Song localSong) {
        if (localSong.isFork()) {
            return;
        }
        final String serverUuid = song.getUuid();
        if (song.isDeleted() && localSong.isPublished()) {
            try {
                songService.deleteMirrorOnServerDelete(serverUuid);
            } catch (Exception e) {
                LOG.error(e.getMessage(), e);
            }
        } else {
            try {
                if (songService.findForkByOriginalUuid(serverUuid) != null) {
                    localSong.setServerMirror(true);
                }
                songService.updateMirrorFromServer(localSong, song);
            } catch (Exception e) {
                LOG.error(e.getMessage(), e);
            }
        }
    }

    private void showConflictSong() {
        if (!conflictGridPane.isVisible()) {
            setNextConflictSong();
        } else {
            showCompletedMessage();
        }
    }

    private int getSelectedLanguageSize() {
        int count = 0;
        for (Language language : languages) {
            if (language.isSelected()) {
                ++count;
            }
        }
        return count;
    }

    private void saveSongCollections(SongCollectionService songCollectionService, List<SongCollection> songCollections) {
        HashMap<String, SongCollection> songCollectionHashMap = new HashMap<>(songCollections.size());
        for (SongCollection songCollection : songCollections) {
            songCollectionHashMap.put(songCollection.getUuid(), songCollection);
        }
        List<SongCollection> needToDelete = new ArrayList<>();
        for (SongCollection songCollection : onlineModifiedSongCollections) {
            if (songCollectionHashMap.containsKey(songCollection.getUuid())) {
                SongCollection modifiedSongCollection = songCollectionHashMap.get(songCollection.getUuid());
                needToDelete.add(modifiedSongCollection);
            }
        }
        songCollectionService.delete(needToDelete);
        songCollectionService.create(onlineModifiedSongCollections);
    }

    private void showRemoteFetchFailure(RemoteFetchFailureKind failureKind, boolean fatal) {
        Platform.runLater(() -> {
            downloadingLabel.setText(ConnectionErrorMessages.getMessage(resourceBundle, failureKind));
            if (fatal) {
                hideDownloadProgress();
                SceneUtils.setVisibleAndManaged(downloadPlaceholderLabel, false);
            }
        });
    }

    private void hideDownloadProgress() {
        if (downloadProgressBar != null) {
            downloadProgressBar.setProgress(0);
            SceneUtils.setVisibleAndManaged(downloadProgressBar, false);
        }
        if (downloadProgressCountLabel != null) {
            SceneUtils.setVisibleAndManaged(downloadProgressCountLabel, false);
        }
    }

    private Long getLastModifiedSongDate(Language language) {
        final List<Song> all = language.getSongs();
        Date lastModified = new Date(0);
        if (!language.isSectionTypeDownloadedCorrectly()) {
            return 0L;
        }
        for (Song song : all) {
            if (song.isFork()) {
                continue;
            }
            Date serverModifiedDate = song.getServerModifiedDate();
            if (serverModifiedDate != null && lastModified.compareTo(serverModifiedDate) < 0 && !song.isDownloadedSeparately()) {
                lastModified = serverModifiedDate;
            }
        }
        return lastModified.getTime();
    }

    private void setNextConflictSong() {
        if (conflictSongList.size() > conflictIndex) {
            setConflictPaneVisible(true);
            Song localSong = conflictLocalSongList.get(conflictIndex);
            conflictTitle.setText(localSong.getTitle());
            Song song = conflictSongList.get(conflictIndex);
            acceptBothButton.setDisable(localSong.getUuid() != null && localSong.getUuid().equals(song.getUuid()));
            String a = localSong.getVersesText();
            String b = song.getVersesText();
            try {
                final List<String> subStrings = StringUtils.highestCommonStrings(a, b);
                CompareDiffHighlighter.render(conflictLocalSongTextFlow, a, subStrings);
                CompareDiffHighlighter.render(conflictSongTextFlow, b, subStrings);
            } catch (Exception e) {
                LOG.error("{} - setNextConflict()", e.getMessage());
                ObservableList<Node> children = conflictLocalSongTextFlow.getChildren();
                children.clear();
                children.add(new Text(a));
                ObservableList<Node> conflictSongTextFlowChildren = conflictSongTextFlow.getChildren();
                conflictSongTextFlowChildren.clear();
                conflictSongTextFlowChildren.add(new Text(b));
            }
            ++conflictIndex;
        } else {
            setConflictPaneVisible(false);
            showCompletedMessage();
        }
    }

    private void showCompletedMessage() {
        if (remainingLanguages == 0) {
            downloadComplete = true;
            if (totalSongs > 0 && completedSongs < totalSongs) {
                completedSongs = totalSongs;
            }
            updateDownloadProgress(completedSongs, totalSongs);
            Platform.runLater(() -> {
                downloadingLabel.setText(resourceBundle.getString("Completed"));
                SceneUtils.setVisibleAndManaged(downloadPlaceholderLabel, false);
                showMigrationInProgressWarning(false);
                if (downloadCompleteHintLabel != null) {
                    SceneUtils.setVisibleAndManaged(downloadCompleteHintLabel, true);
                }
                if (closeWindowButton != null) {
                    SceneUtils.setVisibleAndManaged(closeWindowButton, true);
                }
            });
        }
    }

    private void initializeButtons() {
        if (closeWindowButton != null) {
            closeWindowButton.setOnAction(event -> {
                Stage stage = (Stage) closeWindowButton.getScene().getWindow();
                SceneUtils.closeStage(stage);
            });
        }
        keepButton.setOnAction(event -> setNextConflictSong());
        acceptBothButton.setOnAction(event -> {
            Song song = conflictSongList.get(conflictIndex - 1);
            song.setServerModifiedDate(song.getModifiedDate());
            song.setPublished(true);
            song.setPublish(true);
            song.setUuid(song.getUuid());
            song.setVerses(song.getVerses());
            songService.create(song);
            setNextConflictSong();
        });
        acceptButton.setOnAction(event -> {
            Song localSong = conflictLocalSongList.get(conflictIndex - 1);
            Song song = conflictSongList.get(conflictIndex - 1);
            localSong.setCreatedDate(song.getCreatedDate());
            localSong.setModifiedDate(song.getModifiedDate());
            localSong.setServerModifiedDate(song.getModifiedDate());
            localSong.setVersionGroup(song.getVersionGroup());
            localSong.setLanguage(song.getLanguage());
            localSong.setPublished(true);
            localSong.setPublish(true);
            localSong.setUuid(song.getUuid());
            localSong.setTitle(song.getTitle());
            localSong.setVerseOrderList(song.getVerseOrderList());
            localSong.setAuthor(song.getAuthor());
            songVerseService.delete(localSong.getVerses());
            localSong.setVerses(song.getVerses());
            songService.create(localSong);
            setNextConflictSong();
        });
    }

    @SuppressWarnings("unused")
    private boolean equals(Song song, Song localSong) {
        if (!song.getTitle().equals(localSong.getTitle())) {
            return false;
        }
        final List<SongVerse> verses = song.getVerses();
        final List<SongVerse> localSongVerses = localSong.getVerses();
        if (verses.size() != localSongVerses.size()) {
            return false;
        }
        for (int i = 0; i < verses.size(); ++i) {
            final SongVerse songVerse = verses.get(i);
            final SongVerse songVerse2 = localSongVerses.get(i);
            if (!songVerse.getText().equals(songVerse2.getText())) {
                return false;
            }
            if (songVerse.isChorus() != songVerse2.isChorus()) {
                return false;
            }
        }
        return true;
    }
}
