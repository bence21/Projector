package projector.controller;

import javafx.application.Platform;
import javafx.geometry.Bounds;
import javafx.scene.control.ListView;
import javafx.scene.control.MultipleSelectionModel;
import javafx.scene.control.TabPane;
import javafx.scene.input.KeyCode;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import projector.BaseTest;
import projector.model.Bible;
import projector.model.BibleVerse;
import projector.model.Chapter;
import projector.service.BibleService;
import projector.service.ServiceManager;
import projector.utils.BibleVerseTextFlow;

import java.util.List;

import static projector.application.MainTest.createABible;

public class BibleControllerTest extends BaseTest {

    @Before
    public void setUp() {
        createABible();
        final TabPane tabPane = find("#tabPane");
        Platform.runLater(() -> tabPane.getSelectionModel().select(0));
        sleep(1000);
    }

    @Test
    public void goingToNextVerse() {
        clickOn("#bookTextField").write("book");
        clickOn("#partTextField").write("1");
        clickOn("#verseTextField").write("1");

        final ListView<BibleVerseTextFlow> verseListView = find("#verseListView");
        Bounds boundsInScene = verseListView.localToScene(verseListView.getBoundsInLocal());
        clickOn("#verseListView");
        final double x = boundsInScene.getMinX() + 10;
        final double y = boundsInScene.getMinY() + 10;
        rightClickOn(x, y).sleep(100);
        push(KeyCode.DOWN).sleep(100);
        BibleService bibleService = ServiceManager.getBibleService();
        List<Bible> bibles = bibleService.findAll();
        Bible bible = bibles.get(0);
        List<Chapter> chapters = bible.getBooks().get(0).getChapters();
        Chapter chapter = chapters.get(0);
        List<BibleVerse> verses = chapter.getVerses();
        BibleVerse bibleVerse = verses.get(1);
        MultipleSelectionModel<BibleVerseTextFlow> selectionModel = verseListView.getSelectionModel();
        Assert.assertEquals(bibleVerse, selectionModel.getSelectedItem().getBibleVerse());
        push(KeyCode.DOWN).sleep(100);
        Assert.assertEquals(chapters.get(1).getVerse(0), selectionModel.getSelectedItem().getBibleVerse());
    }

}
