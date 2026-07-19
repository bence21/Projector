package projector.controller.song;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;
import javafx.scene.control.MultipleSelectionModel;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
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
        sleep(300);
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
        NewSongController.resetGlobalRootForTesting();
        Button newSongButton = find("#newSongButton");
        interact(newSongButton::fire);
        Pane root = waitForSongEditorRoot();
        TextField titleTextField = find("#titleTextField", root);
        interact(() -> titleTextField.setText(test_songTitle));
        addFirstVerseInEditor(root);
        TextArea verseTextArea = waitForVisibleVerseTextArea(root);
        interact(() -> verseTextArea.setText("First verse"));
        final ComboBox<Language> languageComboBox = find("#languageComboBoxForNewSong", root);
        interact(() -> {
            if (!languageComboBox.getItems().isEmpty()) {
                languageComboBox.getSelectionModel().selectFirst();
            }
        });
        Button saveButton = find("#saveButton", root);
        interact(saveButton::fire);
        waitForSongSearch(test_songTitle, 1);
        Assert.assertNotNull(findSearchedSong(test_songTitle));
        editSong();
        deleteASong();
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
            for (SearchedSong item : listView.getItems()) {
                if (item.getSong().getTitle().equals(title)) {
                    matches++;
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

    private SearchedSong findSearchedSong(String title) {
        ListView<SearchedSong> listView = find("#searchedSongListView");
        for (SearchedSong item : listView.getItems()) {
            if (item.getSong().getTitle().equals(title)) {
                return item;
            }
        }
        return null;
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

    private void editSong() {
        clearAndSearch();
        SearchedSong songToEdit = findSearchedSong(test_songTitle);
        Assert.assertNotNull("Created song not found for edit", songToEdit);
        NewSongController.resetGlobalRootForTesting();
        SongController songController = MyController.getInstance().getSongController();
        interact(() -> {
            try {
                songController.openSongEditor(songToEdit);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        Pane root = waitForSongEditorRoot();
        final String edited_text = "Edited text";
        TextArea verseTextArea = waitForVisibleVerseTextArea(root);
        interact(() -> verseTextArea.appendText(edited_text));
        Button saveButton = waitForVisibleSaveButton(root);
        interact(saveButton::fire);
        waitForSongSearch(test_songTitle, 1);
        SearchedSong editedSong = findSearchedSong(test_songTitle);
        Assert.assertNotNull(editedSong);
        Assert.assertTrue(editedSong.getSong().getVerses().get(0).getText().contains(edited_text));
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

    private void addFirstVerseInEditor(Pane root) {
        Button newVerseButton = find("#newVerseButton", root);
        for (int attempt = 0; attempt < 50; attempt++) {
            if (findVisibleVerseTextArea(root) != null) {
                return;
            }
            interact(newVerseButton::fire);
            sleep(100);
        }
        Assert.fail("Could not add a verse in the song editor");
    }

    private TextArea waitForVisibleVerseTextArea(Pane root) {
        for (int attempt = 0; attempt < 100; attempt++) {
            TextArea textArea = findVisibleVerseTextArea(root);
            if (textArea != null) {
                return textArea;
            }
            sleep(100);
        }
        Assert.fail("Visible verse editor textArea not found");
        return null;
    }

    private TextArea findVisibleVerseTextArea(Pane root) {
        try {
            Pane textAreas = find("#textAreas", root);
            for (Node child : textAreas.getChildren()) {
                Node verseTextArea = child.lookup("#textArea");
                if (verseTextArea instanceof TextArea textArea && textArea.isVisible()) {
                    return textArea;
                }
            }
        } catch (Exception ignored) {
            // Editor UI still loading
        }
        return null;
    }

    private Button waitForVisibleSaveButton(Pane root) {
        int count = 0;
        do {
            try {
                Button saveButton = find("#saveButton", root);
                if (saveButton.isVisible() && saveButton.isManaged() && !saveButton.isDisabled()) {
                    return saveButton;
                }
            } catch (Exception ignored) {
                // Save button still hidden until editor reports changes
            }
            ++count;
            sleep(100);
        } while (count < 100);
        Assert.fail("Visible save button not found after editing");
        return null;
    }

    private void deleteASong() {
        clearAndSearch();
        SearchedSong songToDelete = findSearchedSong(test_songTitle);
        Assert.assertNotNull("Edited song not found for delete", songToDelete);
        SongController songController = MyController.getInstance().getSongController();
        interact(() -> songController.deleteSong(songToDelete));
        searchForTitle(test_songTitle, 0);
    }
}
