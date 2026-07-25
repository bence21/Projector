package projector.controller.song;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;
import javafx.scene.control.MultipleSelectionModel;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import projector.BaseTest;
import projector.application.Settings;
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
    private static final String FIRST_VERSE_TEXT = "First verse";
    private static final String EDITED_VERSE_TEXT = "Edited text";
    private static final String SONG_VERSE_TEXT = "this is a song verse which I wrote";
    private final String il_iubesc_pe_el = "Il iubesc pe El";
    private final String songAuthor = "Pinter Bela";

    @Before
    public void setUp() {
        createLanguage();
        final TabPane tabPane = find("#tabPane");
        Platform.runLater(() -> tabPane.getSelectionModel().select(2));
        sleep(1000);
        refreshSongList();
    }

    private void refreshSongList() {
        TextField searchTextField = find("#searchTextField");
        interact(() -> {
            NewSongController.resetGlobalRootForTesting();
            SongController songController = MyController.getInstance().getSongController();
            songController.initializeSongs();
            searchTextField.setText("");
        });
        waitForSongListToStabilize();
    }

    private void waitForSongListToStabilize() {
        for (int attempt = 0; attempt < 100; attempt++) {
            ListView<SearchedSong> listView = find("#searchedSongListView");
            if (!listView.getItems().isEmpty()) {
                sleep(150);
                return;
            }
            sleep(100);
        }
        Assert.fail("Song list did not load");
    }

    private void createLanguage() {
        LanguageService languageService = ServiceManager.getLanguageService();
        languageService.delete(languageService.findAll());
        Settings.getInstance().setSongSelectedLanguage(null);
        Language language = new Language();
        language.setUuid("1239807kjfc1h20ojm");
        language.setEnglishName("Test");
        language.setNativeName("Just testing");
        language.setSelected(true);
        languageService.create(language);
        Language persistedLanguage = languageService.findByUuid(language.getUuid());
        createSong(persistedLanguage);
        Settings.getInstance().setSongSelectedLanguage(languageService.findByUuid(language.getUuid()));
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
        NewSongController.resetGlobalRootForTesting();
        Button newSongButton = find("#newSongButton");
        interact(newSongButton::fire);
        Pane root = waitForSongEditorRoot();
        fillNewSongEditor(root);
        waitForSongEditorToClose();
        refreshSongList();
        searchForTitle(test_songTitle, 1);
        editSong();
        deleteASong();
    }

    private void fillNewSongEditor(Pane root) {
        TextField titleTextField = find("#titleTextField", root);
        interact(() -> titleTextField.setText(test_songTitle));
        selectLanguageInSongEditor(root);
        TextArea verseTextArea = waitForSongEditorTextArea(root);
        interact(() -> verseTextArea.setText(FIRST_VERSE_TEXT));
        prepareVerseEditorForSave(root);
        Button saveButton = find("#saveButton", root);
        final boolean[] enabled = {false};
        interact(() -> enabled[0] = !saveButton.isDisabled() && saveButton.isVisible());
        Assert.assertTrue("Save button should be enabled", enabled[0]);
        interact(saveButton::fire);
    }

    private void selectLanguageInSongEditor(Pane root) {
        ComboBox<Language> languageComboBoxForNewSong = find("#languageComboBoxForNewSong", root);
        interact(() -> {
            for (Language language : languageComboBoxForNewSong.getItems()) {
                if ("1239807kjfc1h20ojm".equals(language.getUuid())) {
                    languageComboBoxForNewSong.getSelectionModel().select(language);
                    return;
                }
            }
            if (!languageComboBoxForNewSong.getItems().isEmpty()) {
                languageComboBoxForNewSong.getSelectionModel().selectFirst();
            }
        });
        final Language[] selectedLanguage = new Language[1];
        interact(() -> selectedLanguage[0] = languageComboBoxForNewSong.getSelectionModel().getSelectedItem());
        Assert.assertNotNull("Language must be selected before saving", selectedLanguage[0]);
    }

    private void searchForASong() {
        searchForTitle(il_iubesc_pe_el, 1);
    }

    private void clearAndSearch() {
        searchForTitle(test_songTitle, 1);
    }

    private void searchForTitle(String title, int expectedMatches) {
        TextField searchTextField = find("#searchTextField");
        interact(() -> searchTextField.setText(title));
        waitForSongSearch(title, expectedMatches);
    }

    private void waitForSongSearch(String title, int expectedMatches) {
        for (int attempt = 0; attempt < 100; attempt++) {
            ListView<SearchedSong> listView = find("#searchedSongListView");
            int matches = 0;
            if (title.isEmpty()) {
                matches = listView.getItems().size();
            } else {
                for (SearchedSong item : listView.getItems()) {
                    if (item.getSong().getTitle().equals(title)) {
                        matches++;
                    }
                }
            }
            if (matches == expectedMatches) {
                sleep(150);
                return;
            }
            sleep(100);
        }
        ListView<SearchedSong> listView = find("#searchedSongListView");
        Assert.fail("Expected " + expectedMatches + " search result(s) for '" + title + "', list size="
                + listView.getItems().size());
    }

    private void waitForAuthorField() {
        for (int attempt = 0; attempt < 50; attempt++) {
            TextField authorTextField = find("#authorTextField");
            if (songAuthor.equals(authorTextField.getText())) {
                return;
            }
            sleep(100);
        }
        TextField authorTextField = find("#authorTextField");
        Assert.assertEquals(songAuthor, authorTextField.getText());
    }

    private void waitForSongVerseListView() {
        for (int attempt = 0; attempt < 50; attempt++) {
            ListView<SongVersePartTextFlow> songListView = find("#songListView");
            int verseParts = 0;
            for (SongVersePartTextFlow item : songListView.getItems()) {
                if (item.getSongVerse() != null) {
                    verseParts++;
                }
            }
            if (verseParts >= 2) {
                sleep(150);
                return;
            }
            sleep(100);
        }
        Assert.fail("Song verse list did not load");
    }

    @Test
    public void checkAuthorTextField() {
        searchForASong();
        waitForAuthorField();
    }

    @Test
    public void checkDoubleSelectedSongVerse() {
        searchForASong();
        waitForSongVerseListView();
        final ListView<SongVersePartTextFlow> songListView = find("#songListView");
        Platform.runLater(() -> {
            MultipleSelectionModel<SongVersePartTextFlow> selectionModel = songListView.getSelectionModel();
            selectionModel.clearSelection();
            int selectedSongVerseParts = 0;
            ObservableList<SongVersePartTextFlow> items = songListView.getItems();
            for (int i = 0; i < items.size(); ++i) {
                SongVersePartTextFlow songVersePartTextFlow = items.get(i);
                if (songVersePartTextFlow.getSongVerse() != null) {
                    selectionModel.select(i);
                    ++selectedSongVerseParts;
                    if (selectedSongVerseParts == 2) {
                        break;
                    }
                }
            }
        });
        sleep(300);
        String activeText = "";
        for (int i = 0; i < 50; ++i) {
            activeText = MyController.getInstance().getProjectionScreenController().getActiveText();
            if (activeText != null && !activeText.isEmpty()) {
                break;
            }
            sleep(100);
        }
        Assert.assertNotNull(activeText);
        int activeTextLength = activeText.length();
        int songVerseTextLength2 = SONG_VERSE_TEXT.length() * 2;
        Assert.assertTrue(activeTextLength > songVerseTextLength2);
    }

    //	@Test
    private void editSong() {
        clearAndSearch();
        final ListView<SearchedSong> searchedSongListView = find("#searchedSongListView");
        NewSongController.resetGlobalRootForTesting();
        interact(() -> {
            for (SearchedSong item : searchedSongListView.getItems()) {
                if (item.getSong().getTitle().equals(test_songTitle)) {
                    try {
                        MyController.getInstance().getSongController().openSongEditor(item);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                    return;
                }
            }
            throw new AssertionError("Test song not found for editing");
        });
        Pane root = waitForSongEditorRoot();
        TextArea verseTextArea = waitForSongEditorTextArea(root);
        interact(() -> verseTextArea.appendText(EDITED_VERSE_TEXT));
        prepareVerseEditorForSave(root);
        Button saveButton = find("#saveButton", root);
        final boolean[] enabled = {false};
        interact(() -> enabled[0] = !saveButton.isDisabled() && saveButton.isVisible());
        Assert.assertTrue("Save button should be enabled", enabled[0]);
        interact(saveButton::fire);
        waitForSongEditorToClose();
        refreshSongList();
        searchForTitle(test_songTitle, 1);
        ListView<SearchedSong> listView = find("#searchedSongListView");
        SearchedSong editedSong = null;
        for (SearchedSong song : listView.getItems()) {
            if (song.getSong().getTitle().equals(test_songTitle)) {
                editedSong = song;
                break;
            }
        }
        Assert.assertNotNull(editedSong);
        Assert.assertTrue(editedSong.getSong().getVerses().get(0).getText().contains(EDITED_VERSE_TEXT));
    }

    private Pane waitForSongEditorRoot() {
        int count = 0;
        do {
            Pane root = NewSongController.getGlobalRoot();
            if (root != null && root.getScene() != null && root.getScene().getWindow() != null
                    && root.getScene().getWindow().isShowing()) {
                return root;
            }
            ++count;
            sleep(100);
        } while (count < 100);
        Assert.fail("Song editor window not found");
        return null;
    }

    private TextArea waitForSongEditorTextArea(Pane root) {
        RadioButton rawTextEditorRadioButton = find("#rawTextEditorRadioButton", root);
        interact(rawTextEditorRadioButton::fire);
        for (int count = 0; count < 100; count++) {
            try {
                TextArea textArea = find("#textArea", root);
                if (textArea.getScene() != null) {
                    return textArea;
                }
            } catch (Exception ignored) {
            }
            sleep(100);
        }
        throw new AssertionError("Song editor textArea not found");
    }

    private void prepareVerseEditorForSave(Pane root) {
        RadioButton verseEditorRadioButton = find("#verseEditorRadioButton", root);
        interact(verseEditorRadioButton::fire);
        sleep(150);
    }

    private void waitForSongEditorToClose() {
        for (int attempt = 0; attempt < 50; attempt++) {
            Pane root = NewSongController.getGlobalRoot();
            if (root == null || root.getScene() == null || root.getScene().getWindow() == null
                    || !root.getScene().getWindow().isShowing()) {
                return;
            }
            sleep(100);
        }
        Assert.fail("Song editor window did not close");
    }

    //	@Test
    private void deleteASong() {
        clearAndSearch();
        final ListView<SearchedSong> searchedSongListView = find("#searchedSongListView");
        interact(() -> {
            for (SearchedSong item : searchedSongListView.getItems()) {
                if (item.getSong().getTitle().equals(test_songTitle)) {
                    SongController songController = MyController.getInstance().getSongController();
                    Song song = songController.removeSongFromList(item);
                    if (song != null) {
                        try {
                            ServiceManager.getSongService().delete(song);
                            songController.initializeSongs();
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }
                    return;
                }
            }
            throw new AssertionError("Test song not found for deletion");
        });
        searchForTitle(test_songTitle, 0);
    }
}
