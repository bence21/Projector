package projector.controller.song;

import com.bence.projector.common.dto.SongFavouritesDTO;
import com.bence.projector.common.dto.SongViewsDTO;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import projector.service.SongService;
import projector.service.SongVerseService;
import projector.utils.StringUtils;

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

    private static void addTexts(String a, List<String> subStrings, TextFlow textFlow) {
        textFlow.getChildren().clear();
        for (int i = subStrings.size() - 1; i >= 0; --i) {
            final String x = subStrings.get(i);
            final int endIndex = a.indexOf(x);
            final Text text = new Text(a.substring(0, endIndex));
            text.setUnderline(true);
            text.setFill(Color.rgb(255, 0, 0));
            textFlow.getChildren().add(text);
            final Text text2 = new Text(x);
            text2.setFill(Color.rgb(36, 52, 40));
            textFlow.getChildren().add(text2);
            a = a.substring(endIndex + x.length());
        }
        if (a.length() > 0) {
            final Text text = new Text(a);
            text.setUnderline(true);
            text.setFill(Color.rgb(255, 0, 0));
            textFlow.getChildren().add(text);
        }
    }

    public void initialize() {
        conflictGridPane.setVisible(false);
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
        final List<Song> songs = songService.findAll();
        LanguageService languageService = ServiceManager.getLanguageService();
        languages = languageService.findAll();
        Thread thread = new Thread(() -> {
            SongApiBean songApi = new SongApiBean();
            remainingLanguages = getSelectedLanguageSize();
            for (Language language : languages) {
                if (language.isSelected()) {
                    Platform.runLater(() -> downloadingLabel.setText(resourceBundle.getString("Downloading") + ": " + language.getNativeName()));
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
                    final List<Song> songApiSongs = songApi.getSongsByLanguageAndAfterModifiedDate(language, getLastModifiedSongDate(language));
                    if (songApiSongs == null) {
                        noInternetMessage();
                        return;
                    } else {
                        if (songApiSongs.size() > 0) {
                            Platform.runLater(() -> downloadingLabel.setText(resourceBundle.getString("Saving") + ": " + language.getNativeName()));
                            HashMap<String, Song> uuidSongHashMap = new HashMap<>(songs.size());
                            for (Song song : songs) {
                                if (song.getUuid() != null) {
                                    uuidSongHashMap.put(song.getUuid(), song);
                                }
                            }
                            for (Song song : songApiSongs) {
                                final boolean containsKeyInUuid = uuidSongHashMap.containsKey(song.getUuid());
                                song.setLanguage(language);
                                if (!containsKeyInUuid) {
                                    if (!song.isDeleted()) {
                                        newSongList.add(song);
                                        saveSong(song);
                                    }
                                } else {
                                    final Song localSong = uuidSongHashMap.get(song.getUuid());
                                    if (song.isDeleted() && localSong.isPublished()) {
                                        songService.delete(localSong);
                                    } else {
                                        // Means modified song
                                        localSong.setServerModifiedDate(song.getServerModifiedDate());
                                        localSong.setCreatedDate(song.getCreatedDate());
                                        localSong.setModifiedDate(song.getModifiedDate());
                                        localSong.setLanguage(language);
                                        localSong.setVersionGroup(song.getVersionGroup());
                                        songVerseService.delete(localSong.getVerses());
                                        localSong.setTitle(song.getTitle());
                                        localSong.setVerses(song.getVerses());
                                        localSong.setViews(song.getViews());
                                        localSong.setFavouriteCount(song.getFavouriteCount());
                                        localSong.setAuthor(song.getAuthor());
                                        localSong.setVerseOrderList(song.getVerseOrderList());
                                        saveSong(localSong);
                                    }
                                }
                            }
                        }
                    }
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
                        onlineModifiedSongCollections = songCollectionApiBean.getSongCollections(language, lastModifiedDate);
                        if (onlineModifiedSongCollections == null) {
                            noInternetMessage();
                        } else {
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
        initializeButtons();
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

    private void noInternetMessage() {
        final String no_internet_connection = resourceBundle.getString("No internet connection");
        final String try_again_later = resourceBundle.getString("Try again later");
        Platform.runLater(() -> downloadingLabel.setText(no_internet_connection + "! " + try_again_later + "!"));
    }

    private Long getLastModifiedSongDate(Language language) {
        final List<Song> all = language.getSongs();
        Date lastModified = new Date(0);
        if (!language.isSectionTypeDownloadedCorrectly()) {
            return 0L;
        }
        for (Song song : all) {
            Date serverModifiedDate = song.getServerModifiedDate();
            if (serverModifiedDate != null && lastModified.compareTo(serverModifiedDate) < 0 && !song.isDownloadedSeparately()) {
                lastModified = serverModifiedDate;
            }
        }
        return lastModified.getTime();
    }

    private void setNextConflictSong() {
        if (conflictSongList.size() > conflictIndex) {
            conflictGridPane.setVisible(true);
            Song localSong = conflictLocalSongList.get(conflictIndex);
            conflictTitle.setText(localSong.getTitle());
            Song song = conflictSongList.get(conflictIndex);
            acceptBothButton.setDisable(localSong.getUuid() != null && localSong.getUuid().equals(song.getUuid()));
            String a = localSong.getVersesText();
            String b = song.getVersesText();
            try {
                final List<String> subStrings = StringUtils.highestCommonStrings(a, b);
                addTexts(a, subStrings, conflictLocalSongTextFlow);
                addTexts(b, subStrings, conflictSongTextFlow);
            } catch (Exception e) {
                LOG.error(e.getMessage() + " - setNextConflict()");
                ObservableList<Node> children = conflictLocalSongTextFlow.getChildren();
                children.clear();
                children.add(new Text(a));
                ObservableList<Node> conflictSongTextFlowChildren = conflictSongTextFlow.getChildren();
                conflictSongTextFlowChildren.clear();
                conflictSongTextFlowChildren.add(new Text(b));
            }
            ++conflictIndex;
        } else {
            conflictGridPane.setVisible(false);
            showCompletedMessage();
        }
    }

    private void showCompletedMessage() {
        if (remainingLanguages == 0) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle(resourceBundle.getString("Completed"));
            alert.setHeaderText(resourceBundle.getString("Close the window to finish!"));
            alert.showAndWait();
            Platform.runLater(() -> downloadingLabel.setText(resourceBundle.getString("Completed")));
        }
    }

    private void initializeButtons() {
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
