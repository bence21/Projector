package projector.controller.song;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.geometry.Bounds;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import projector.BaseTest;
import projector.controller.MyController;
import projector.controller.song.util.SearchedSong;
import projector.model.Language;
import projector.model.Song;
import projector.model.SongVerse;
import projector.service.LanguageService;
import projector.service.ServiceManager;
import projector.service.SongService;
import projector.utils.scene.text.SongVersePartTextFlow;

import java.util.ArrayList;
import java.util.List;

public class SongControllerTest extends BaseTest {

    private static final String test_songTitle = "Test song";
    private static final String SONG_VERSE_TEXT = "this is a song verse which I wrote";
    private final String il_iubesc_pe_el = "Il iubesc pe El";
    private final String songAuthor = "Pinter Bela";

    @Before
    public void setUp() {
        createLanguage();
        final TabPane tabPane = find("#tabPane");
        Platform.runLater(() -> tabPane.getSelectionModel().select(2));
        sleep(1000);
    }

    private void createLanguage() {
        LanguageService languageService = ServiceManager.getLanguageService();
        languageService.delete(languageService.findAll());
        Language language = new Language();
        language.setUuid("1239807kjfc1h20ojm");
        language.setEnglishName("Test");
        language.setNativeName("Just testing");
        language.setSelected(true);
        languageService.create(language);
        createSong(language);
    }


    private void createSong(Language language) {
        Song testSong = new Song();
        testSong.setTitle(il_iubesc_pe_el);
        List<SongVerse> testVerses = new ArrayList<>();
        createAndAddSongVerse(testVerses);
        createAndAddSongVerse(testVerses);
        testSong.setVerses(testVerses);
        testSong.setAuthor(songAuthor);
        testSong.setLanguage(language);
        SongService songService = ServiceManager.getSongService();
        songService.create(testSong);
    }

    private static void createAndAddSongVerse(List<SongVerse> testVerses) {
        SongVerse testSongVerse = new SongVerse();
        testVerses.add(testSongVerse);
        testSongVerse.setText(SONG_VERSE_TEXT);
    }

    @Test
    public void clickNewSongButton() {
        int count = 0;
        do {
            try {
                find("#searchTextField");
                break;
            } catch (Exception e) {
                ++count;
                sleep(100);
            }
        } while (count < 100);
        clickOn("#newSongButton");
        clickOn("#titleTextField").write(test_songTitle);
        clickOn("#newVerseButton");
        clickOn("#textArea").write("First verse");
        clickOn("#languageComboBoxForNewSong").sleep(100);
        Pane root = NewSongController.getGlobalRoot();
        final ComboBox<Language> languageComboBox = find("#languageComboBoxForNewSong", root);
        Platform.runLater(() -> languageComboBox.getSelectionModel().selectFirst());
        sleep(100);
        clickOn("#saveButton");
        ListView<SearchedSong> listView = find("#searchedSongListView");
        boolean was = false;
        ObservableList<SearchedSong> items = listView.getItems();
        for (SearchedSong song : items) {
            if (song.getSong().getTitle().equals(test_songTitle)) {
                was = true;
                break;
            }
        }
        Assert.assertTrue(was);
        editSong();
        deleteASong();
    }


    private void searchForASong() {
        clickOn("#searchTextField").write(il_iubesc_pe_el);
    }

    @Test
    public void checkAuthorTextField() {
        searchForASong();
        TextField authorTextField = find("#authorTextField");
        Assert.assertEquals(songAuthor, authorTextField.getText());
    }

    @Test
    public void checkDoubleSelectedSongVerse() {
        searchForASong();
        final ListView<SongVersePartTextFlow> songListView = find("#songListView");
        Bounds boundsInScene = songListView.localToScene(songListView.getBoundsInLocal());
        clickOn("#songListView");
        final double x = boundsInScene.getMinX() + 10;
        final double y = boundsInScene.getMinY() + 60;
        clickOn(x, y).sleep(100).press(KeyCode.SHIFT).clickOn(x + 7, y + 40).release(KeyCode.SHIFT);
        String activeText = MyController.getInstance().getProjectionScreenController().getActiveText();
        Assert.assertTrue(activeText.length() > SONG_VERSE_TEXT.length() * 2);
    }

    //	@Test
    private void editSong() {
        clickOn("#searchTextField").write(test_songTitle);
        final ListView<SearchedSong> searchedSongListView = find("#searchedSongListView");
        Bounds boundsInScene = searchedSongListView.localToScene(searchedSongListView.getBoundsInLocal());
        clickOn("#searchedSongListView");
        final double x = boundsInScene.getMinX() + 10;
        final double y = boundsInScene.getMinY() + 10;
        rightClickOn(x, y).sleep(100).clickOn(x + 7, y + 7);
        final String edited_text = "Edited text";
        clickOn("#textArea").write(edited_text);
        clickOn("#saveButton");
        ListView<SearchedSong> listView = find("#searchedSongListView");
        SearchedSong editedSong = null;
        for (SearchedSong song : listView.getItems()) {
            if (song.getSong().getTitle().equals(test_songTitle)) {
                editedSong = song;
                break;
            }
        }
        Assert.assertNotNull(editedSong);
        Assert.assertTrue(editedSong.getSong().getVerses().get(0).getText().contains(edited_text));
    }

    //	@Test
    private void deleteASong() {
        doubleClickOn("#searchTextField").doubleClickOn("#searchTextField").write(test_songTitle);
        final ListView<SearchedSong> searchedSongListView = find("#searchedSongListView");
        Bounds boundsInScene = searchedSongListView.localToScene(searchedSongListView.getBoundsInLocal());
        clickOn("#searchedSongListView");
        final double x = boundsInScene.getMinX() + 10;
        final double y = boundsInScene.getMinY() + 10;
        rightClickOn(x, y).sleep(100).clickOn("#deleteMenuItem");
        sleep(100).clickOn("#confirmButton").sleep(50);
        doubleClickOn("#searchTextField").doubleClickOn("#searchTextField").write(test_songTitle);
        Assert.assertEquals(searchedSongListView.getItems().size(), 0);
    }
}