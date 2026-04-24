package projector.controller.song;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.util.Callback;
import projector.api.SongApiBean;
import projector.api.SongApiBean.SongUploadFailureKind;
import projector.api.SongCollectionApiBean;
import projector.application.Settings;
import projector.model.Language;
import projector.model.Song;
import projector.model.SongCollection;
import projector.service.LanguageService;
import projector.service.ServiceManager;
import projector.service.SongCollectionService;
import projector.service.SongService;

import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicBoolean;

public class UploadSongsController {
    private LanguageService languageService;
    @FXML
    private Button uploadButton;
    @FXML
    private Label uploadingLabel;
    @FXML
    private ListView<Song> songListView;
    private SongService songService;
    private List<Song> publishingSongs;
    private final AtomicBoolean uploadInProgress = new AtomicBoolean(false);

    public void initialize() {
        languageService = ServiceManager.getLanguageService();
        songListView.setCellFactory(new SongCell().invoke());
        songService = ServiceManager.getSongService();
        final List<Song> songs = songService.findAll();
        publishingSongs = new ArrayList<>();
        for (Song song : songs) {
            if (song.isPublish() && !song.isPublished()) {
                publishingSongs.add(song);
            }
        }
        ObservableList<Song> songListViewItems = songListView.getItems();
        songListViewItems.addAll(publishingSongs);
        if (publishingSongs.size() == 0) {
            uploadButton.setDisable(true);
        } else {
            uploadButton.setOnAction(event -> {
                if (!uploadInProgress.compareAndSet(false, true)) {
                    return;
                }
                uploadButton.setDisable(true);
                Thread thread = new Thread(() -> {
                    SongApiBean songApi = new SongApiBean();
                    List<Song> publishedSongs = new ArrayList<>(publishingSongs.size());
                    boolean[] allPublished = {true};
                    SongUploadFailureKind[] dominantFailure = {null};
                    for (Song song : new ArrayList<>(publishingSongs)) {
                        Language language = song.getLanguage();
                        if (language != null) {
                            song.setLanguage(languageService.findById(language.getId()));
                        }
                        SongApiBean.SongUploadResult uploadResult = songApi.uploadSong(song);
                        Song uploadedSong = uploadResult.getSong();
                        if (uploadedSong != null) {
                            String uuid = song.getUuid();
                            if (uuid == null || uuid.trim().isEmpty()) {
                                song.setUuid(uploadedSong.getUuid());
                            }
                            song.setPublished(true);
                            songService.update(song);
                            publishedSongs.add(song);
                        } else {
                            allPublished[0] = false;
                            SongUploadFailureKind k = uploadResult.getFailureKind();
                            if (dominantFailure[0] == null) {
                                dominantFailure[0] = k;
                            }
                            if (k == SongUploadFailureKind.NEEDS_SIGN_IN
                                    || k == SongUploadFailureKind.SESSION_NOT_REFRESHED) {
                                dominantFailure[0] = k;
                            }
                        }
                    }
                    SongUploadFailureKind finalFailure = dominantFailure[0];
                    Platform.runLater(() -> {
                        publishingSongs.removeAll(publishedSongs);
                        songListViewItems.clear();
                        songListViewItems.addAll(publishingSongs);
                        if (!allPublished[0]) {
                            if (finalFailure == SongUploadFailureKind.NEEDS_SIGN_IN
                                    || finalFailure == SongUploadFailureKind.SESSION_NOT_REFRESHED) {
                                uploadAuthMessage(finalFailure);
                            } else {
                                noInternetMessage();
                            }
                            uploadButton.setDisable(publishingSongs.isEmpty());
                        } else {
                            uploadButton.setDisable(true);
                            uploadingLabel.setText("Finished!");
                        }
                        uploadInProgress.set(false);
                    });
                });
                thread.start();
            });
        }
        Thread thread = new Thread(() -> {
            SongCollectionService songCollectionService = ServiceManager.getSongCollectionService();
            List<SongCollection> songCollections = songCollectionService.findAll();
            List<SongCollection> publishingSongCollections = new ArrayList<>();
            for (SongCollection songCollection : songCollections) {
                if (songCollection.isNeedUpload()) {
                    publishingSongCollections.add(songCollection);
                }
            }
            if (publishingSongCollections.size() < 1) {
                return;
            }
            SongCollectionApiBean songCollectionApiBean = new SongCollectionApiBean();
            for (SongCollection songCollection : publishingSongCollections) {
                SongCollection uploadedSongCollection = songCollectionApiBean.uploadSongCollection(songCollection);
                if (uploadedSongCollection != null) {
                    String uuid = songCollection.getUuid();
                    if (uuid == null || uuid.trim().isEmpty()) {
                        songCollection.setUuid(uploadedSongCollection.getUuid());
                    }
                    songCollection.setNeedUpload(false);
                    songCollectionService.update(songCollection);
                }
            }
        });
        thread.start();
    }

    private void noInternetMessage() {
        final ResourceBundle resourceBundle = Settings.getInstance().getResourceBundle();
        final String no_internet_connection = resourceBundle.getString("No internet connection");
        final String try_again_later = resourceBundle.getString("Try again later");
        Platform.runLater(() -> uploadingLabel.setText(no_internet_connection + "! " + try_again_later + "!"));
    }

    private void uploadAuthMessage(SongUploadFailureKind kind) {
        final ResourceBundle resourceBundle = Settings.getInstance().getResourceBundle();
        final String message = kind == SongUploadFailureKind.NEEDS_SIGN_IN
                ? resourceBundle.getString("Upload needs sign in")
                : resourceBundle.getString("Upload session expired");
        uploadingLabel.setText(message);
    }

    private static class SongCell {
        Callback<ListView<Song>, ListCell<Song>> invoke() {
            return param -> new ListCell<>() {
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
            };
        }
    }
}
