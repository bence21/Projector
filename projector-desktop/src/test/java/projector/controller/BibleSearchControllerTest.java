package projector.controller;

import javafx.application.Platform;
import javafx.scene.control.ListView;
import javafx.scene.control.TabPane;
import javafx.scene.input.MouseButton;
import javafx.scene.text.TextFlow;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import projector.BaseTest;

import static projector.application.MainTest.createABible;

@SuppressWarnings("SameParameterValue")
public class BibleSearchControllerTest extends BaseTest {

    private void openTab(int index) {
        final TabPane tabPane = find("#tabPane");
        Platform.runLater(() -> tabPane.getSelectionModel().select(index));
        sleep(1000);
    }

    @Before
    public void setUp() {
        createABible();
        openTab(1);
    }

    @Test
    public void should_click_to_all() {
        searchShouldFind("verse");
    }

    private void searchShouldFind(String searchText) {
        moveTo("#bibleSearchTextField");
        press(MouseButton.PRIMARY);
        release(MouseButton.PRIMARY);
        write_(searchText);
        sleep(500);
        ListView<TextFlow> searchListView = find("#searchListView");
        if (searchListView.getItems().size() == 0) {
            sleep(500);
        }
        Assert.assertNotEquals(searchListView.getItems().size(), 0);
    }
}