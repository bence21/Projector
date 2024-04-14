package projector.application;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.geometry.Bounds;
import javafx.scene.control.ListView;
import javafx.scene.control.TabPane;
import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Test;
import projector.BaseTest;
import projector.controller.song.util.SearchedSong;
import projector.model.Bible;
import projector.model.BibleVerse;
import projector.model.Book;
import projector.model.Chapter;
import projector.model.VerseIndex;
import projector.service.BibleService;
import projector.service.ServiceManager;

import java.util.ArrayList;

import static javafx.scene.input.KeyCode.DOWN;
import static javafx.scene.input.KeyCode.ENTER;

public class MainTest extends BaseTest {

    private static long indexNumber = 1000;

    static public void createABible() {
        Bible bible = new Bible();
        bible.setName("Bible");
        ArrayList<Book> books = new ArrayList<>();
        Book book = new Book();
        book.setTitle("book");
        ArrayList<Chapter> chapters = new ArrayList<>();
        createChapter(chapters);
        createChapter(chapters);
        book.setChapters(chapters);
        books.add(book);
        bible.setBooks(books);
        BibleService bibleService = ServiceManager.getBibleService();
        bibleService.delete(bibleService.findAll());
        bibleService.create(bible);
    }

    private static void createChapter(ArrayList<Chapter> chapters) {
        Chapter chapter = new Chapter();
        short chapterIndex = (short) (chapters.size() + 1);
        chapter.setNumber(chapterIndex);
        ArrayList<BibleVerse> verses = new ArrayList<>();
        addBibleVerse(verses);
        addBibleVerse(verses);
        chapter.setVerses(verses);
        for (BibleVerse verse : verses) {
            verse.setText(chapter.getNumber() + ": " + verse.getText());
        }
        chapters.add(chapter);
    }

    private static void addBibleVerse(ArrayList<BibleVerse> verses) {
        BibleVerse bibleVerse = new BibleVerse();
        short index = (short) (verses.size() + 1);
        bibleVerse.setText("A verse " + index);
        bibleVerse.setNumber(index);
        ArrayList<VerseIndex> verseIndices = new ArrayList<>();
        VerseIndex verseIndex = new VerseIndex();
        verseIndex.setBibleId((long) 1);
        verseIndex.setIndexNumber(indexNumber);
        indexNumber += 1000;
        verseIndices.add(verseIndex);
        bibleVerse.setVerseIndices(verseIndices);
        verses.add(bibleVerse);
    }

    private void openTab(int index) {
        final TabPane tabPane = find("#tabPane");
        Platform.runLater(() -> tabPane.getSelectionModel().select(index));
        sleep(1000);
    }

    @Test
    public void should_drag_file_into_trashcan() {
        clickOn("#tabPane");
    }

    @Test
    public void should_click_to_all() {
        createABible();
        clickOn("#tabPane");
        openTab(0);
        clickOn("#bible");

        final String bookTitle = "book";
        clickOn("#bookTextField").sleep(50).clickOn("#bookTextField").sleep(50).write(bookTitle).sleep(50);
        final ListView<String> bookListView = find("#bookListView");
        final ObservableList<String> bookListViewItems = bookListView.getItems();
        for (String item : bookListViewItems) {
            Assert.assertThat(item, CoreMatchers.containsString(bookTitle));
        }
        final String partNumber = "1";
        clickOn("#partTextField").write(partNumber);
        final ListView<Integer> partListView = find("#partListView");
        Assert.assertTrue(partListView.getItems().size() > 0);
        final String verseNumber = "1";
        clickOn("#verseTextField").write(verseNumber);
        final ListView<Integer> verseListView = find("#verseListView");
        Assert.assertTrue(verseListView.getItems().size() > 0);
        clickOn("#verseListView").push(ENTER).sleep(100);
    }

    @Test
    public void should_be_none_in_recent() {
        openTab(2);
        clickOn("#searchTextField").write("100");

        final ListView<SearchedSong> searchedSongListView = find("#searchedSongListView");
        Bounds boundsInScene = searchedSongListView.localToScene(searchedSongListView.getBoundsInLocal());
        clickOn("#searchedSongListView");
        clickOn(boundsInScene.getMinX() + 10, boundsInScene.getMinY() + 10);
        push(DOWN);
        push(DOWN);
        push(DOWN);
        openTab(3);
        final ListView<String> recentListView = find("#listView");
        Assert.assertEquals(recentListView.getItems().size(), 0);
    }
}