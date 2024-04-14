package projector.controller.song;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import projector.application.Settings;
import projector.model.Song;
import projector.model.SongCollection;
import projector.model.SongCollectionElement;
import projector.model.SongVerse;
import projector.service.ServiceManager;
import projector.service.SongService;
import projector.utils.StringUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.ResourceBundle;

import static projector.controller.song.SongController.setSongCollectionForSongsInHashMap;

public class CompareSimilarSongsController {
    public static final Logger LOG = LoggerFactory.getLogger(CompareSimilarSongsController.class);
    @FXML
    private Label secondTitle;
    @FXML
    private Button acceptButton;
    @FXML
    private GridPane conflictGridPane;
    @FXML
    private Label conflictTitle;
    @FXML
    private TextFlow conflictSongTextFlow;
    @FXML
    private TextFlow conflictLocalSongTextFlow;
    @FXML
    private Button deleteRight;
    @FXML
    private Button deleteLeft;
    private List<Song> conflictSongList;
    private List<Song> conflictLocalSongList;
    private int conflictIndex = 0;
    private boolean showingConflict = false;
    private SongService songService;

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
        conflictSongList = new ArrayList<>();
        conflictLocalSongList = new ArrayList<>();
        songService = ServiceManager.getSongService();
        final List<Song> songs = songService.findAll();
        List<SongCollection> songCollections = ServiceManager.getSongCollectionService().findAll();
        HashMap<String, Song> hashMap = new HashMap<>(songs.size());
        for (Song song : songs) {
            hashMap.put(song.getUuid(), song);
        }
        setSongCollectionForSongsInHashMap(songCollections, hashMap);
        Thread thread = new Thread(() -> {
            boolean work = false;
            for (int i = 0; i < songs.size() - 1; ++i) {
                if (!work) {
                    if (songs.get(i).getTitle().equals("Ifju társak hív a munka jöjjetek")) {
                        work = true;
                    } else {
                        continue;
                    }
                }
                for (int j = i + 1; j < songs.size() - 1; ++j) {
                    Song song = songs.get(i);
                    Song secondSong = songs.get(j);
                    if (song.getLanguage().getId().equals(secondSong.getLanguage().getId()) && !song.isDeleted() && !secondSong.isDeleted()) {
                        double x;
                        String text = getText(secondSong);
                        String secondText = getText(song);
                        x = StringUtils.highestCommonSubStringInt(text, secondText);
                        x = x / text.length();
                        if (x > 0.75) {
                            double y;
                            y = StringUtils.highestCommonSubStringInt(text, secondText);
                            y = y / secondText.length();
                            if (y > 0.75) {
                                List<SongCollection> songCollections1 = song.getSongCollections();
                                List<SongCollection> songCollections2 = secondSong.getSongCollections();
                                if (songCollections1 != null && songCollections2 != null && songCollections1.size() > 0 && songCollections2.size() > 0) {
                                    continue;
                                }
                                System.out.println("i = " + i);
                                System.out.println(secondSong.getId() + "    " + song.getId());
                                if (x != 1.0) {
                                    System.out.println(x + " " + secondSong.getTitle() + "   " + song.getTitle());
                                } else {
                                    System.out.println(x + " " + secondSong.getTitle() + "   " + song.getTitle());
                                }
                                conflictSongList.add(song);
                                conflictLocalSongList.add(secondSong);
                                if (!showingConflict) {
                                    setNextConflictSong();
                                }
                            }
                        }
                    }
                }
            }
        });
        thread.start();
        initializeButtons();
    }

    private String getText(Song song) {
        StringBuilder s = new StringBuilder();
        for (SongVerse songVerse : song.getVerses()) {
            s.append(songVerse.getText());
        }
        return s.toString();
    }

    private void setNextConflictSong() {
        showingConflict = true;
        Platform.runLater(() -> {
            if (conflictSongList.size() > conflictIndex) {
                conflictGridPane.setVisible(true);
                Song song = conflictSongList.get(conflictIndex);
                if (song.isDeleted()) {
                    ++conflictIndex;
                    return;
                }
                Song localSong = conflictLocalSongList.get(conflictIndex);
                if (localSong.isDeleted()) {
                    ++conflictIndex;
                    return;
                }
                String text = "";
                text = getSongCollectionString(localSong, text);
                text += localSong.getTitle();

                String secondText = getSongCollectionString(song, "");
                secondText += song.getTitle() + " ";
                conflictTitle.setText(text);
                this.secondTitle.setText(secondText);
                String a = localSong.getVersesText();
                String b = song.getVersesText();
                final List<String> subStrings = StringUtils.highestCommonStrings(a, b);
                addTexts(a, subStrings, conflictLocalSongTextFlow);
                addTexts(b, subStrings, conflictSongTextFlow);
                ++conflictIndex;
            } else {
                showingConflict = false;
                conflictGridPane.setVisible(false);
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                final ResourceBundle resourceBundle = Settings.getInstance().getResourceBundle();
                alert.setTitle(resourceBundle.getString("Completed"));
                alert.setHeaderText(resourceBundle.getString("Close the window to finish!"));
                alert.showAndWait();
            }
        });
    }

    private String getSongCollectionString(Song song, String text) {
        StringBuilder textBuilder = new StringBuilder(text);
        for (SongCollectionElement songCollectionElement : song.getSongCollectionElements()) {
            SongCollection songCollection = songCollectionElement.getSongCollection();
            textBuilder.append(songCollection.getName()).append(" ");
            textBuilder.append(songCollectionElement.getOrdinalNumber()).append("  ");
        }
        text = textBuilder.toString();
        return text;
    }

    private void initializeButtons() {
        deleteLeft.setOnAction(event -> {
            Song localSong = conflictLocalSongList.get(conflictIndex - 1);
            delete(localSong);
            setNextConflictSong();
        });
        acceptButton.setOnAction(event -> setNextConflictSong());
        deleteRight.setOnAction(event -> {
            Song song = conflictSongList.get(conflictIndex - 1);
            delete(song);
            setNextConflictSong();
        });
    }

    private void delete(Song song) {
        song.setDeleted(true);
        song.setModifiedDate(new Date());
        song.setPublished(false);
        songService.create(song);
    }
}
