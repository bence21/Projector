package projector.controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.text.TextFlow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import projector.application.Settings;
import projector.model.Bible;
import projector.model.BibleVerse;
import projector.model.Book;
import projector.model.Chapter;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import static projector.controller.BibleController.addTextWithBackGround;
import static projector.controller.BibleController.setFoundTextColor;
import static projector.controller.BibleController.setGeneralTextColor;
import static projector.controller.BibleController.setReferenceTextColor;

public class BibleSearchController {

    private static final Logger LOG = LoggerFactory.getLogger(BibleSearchController.class);
    @FXML
    private TextField bibleSearchTextField;
    @FXML
    private ListView<TextFlow> searchListView;
    private BibleController bibleController;

    private List<Integer> searchIBook;
    private List<Integer> searchIPart;
    private List<Integer> searchIVerse;
    private Integer searchSelected = 0;
    private String newSearchText = "";
    private String searchText = "";
    private int maxResults;
    private MyController mainController;
    private boolean initialized = false;
    private Bible bible;

    private static String strip(String s) {
        s = stripAccents(s).replaceAll("[^a-zA-Z]", "").toLowerCase(Locale.US).trim();
        return s;
    }

    private static String stripAccents(String s) {
        s = Normalizer.normalize(s, Normalizer.Form.NFD);
        s = s.replaceAll("[\\p{InCombiningDiacriticalMarks}]", "");
        return s;
    }

    void lazyInitialize() {
        if (initialized) {
            return;
        }
        initialized = true;
        maxResults = 1200;
        bibleSearchTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            setNewSearchText(newValue);
            search(newValue);
            System.out.println("newValue: " + newValue);
        });
        bibleSearchTextField.setOnKeyPressed(event -> mainController.globalKeyEventHandler().handle(event));
        searchListView.getSelectionModel().selectedIndexProperty().addListener((observable, oldValue, newValue) -> {
            int index = searchListView.getSelectionModel().selectedIndexProperty().get();
            if (index >= 0) {
                bibleController.addAllBooks();
                if (bibleController.getBookListView().getSelectionModel().getSelectedIndex() != searchIBook
                        .get(index)) {
                    searchSelected = 1;
                } else {
                    searchSelected = 0;
                }
                bibleController.getBookListView().getSelectionModel().select(searchIBook.get(index));
                while (searchSelected == 1) {
                    try {
                        TimeUnit.MILLISECONDS.sleep(1);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                bibleController.getBookListView().scrollTo(searchIBook.get(index));
                if (bibleController.getPartListView().getSelectionModel().getSelectedIndex() != searchIPart
                        .get(index)) {
                    searchSelected = 2;
                } else {
                    searchSelected = 0;
                }
                int p = searchIPart.get(index);
                bibleController.getPartListView().getSelectionModel().select(p);
                while (searchSelected == 2) {
                    try {
                        TimeUnit.MILLISECONDS.sleep(1);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                bibleController.getPartListView().scrollTo(searchIPart.get(index));
                bibleController.getVerseListView().getSelectionModel().clearSelection();
                bibleController.getVerseListView().getSelectionModel().select(searchIVerse.get(index));
                bibleController.getVerseListView().scrollTo(searchIVerse.get(index));
            }
        });
    }

    public boolean contains(String a, String b) {
        return a.contains(b);
    }

    private synchronized String getNewSearchText() {
        return newSearchText;
    }

    private synchronized void setNewSearchText(String newText) {
        this.newSearchText = newText;
    }

    private synchronized String getSearchText() {
        return searchText;
    }

    private synchronized void setSearchText(String searchText) {
        this.searchText = searchText;
    }

    private void search(String text) {
        System.out.println("service start");
        setSearchText(text);
        Thread thread = new Thread(() -> {
            System.out.println(Thread.currentThread().getName() + " service started");
            String tmp2 = getNewSearchText();
            try {
                TimeUnit.MILLISECONDS.sleep(400);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            String tmp = getNewSearchText();

            String text3 = tmp2.toLowerCase(Locale.US).trim();

            if (!Settings.getInstance().isWithAccents()) {
                text3 = strip(text3);
            }
            text3 = text3.replace("]", "").replace("[", "");

            List<TextFlow> tmpSearchListView = new ArrayList<>();
            List<Integer> tmpSearchIBook = new ArrayList<>();
            List<Integer> tmpSearchIPart = new ArrayList<>();
            List<Integer> tmpSearchIVerse = new ArrayList<>();
            int results = 0;
            List<Book> books = bible.getBooks();
            for (int iBook = 0; iBook < books.size() && results < maxResults; ++iBook) {
                Book book = books.get(iBook);
                List<Chapter> chapters = book.getChapters();
                for (int iPart = 0; iPart < chapters.size() && results < maxResults; ++iPart) {
                    Chapter chapter = chapters.get(iPart);
                    List<BibleVerse> bibleVerses = chapter.getVerses();
                    for (int iVerse = 0; iVerse < bibleVerses.size(); ++iVerse) {
                        String text2;
                        BibleVerse bibleVerse = bibleVerses.get(iVerse);
                        String verse = bibleVerse.getText();
                        if (Settings.getInstance().isWithAccents()) {
                            text2 = bibleVerse.getText();
                        } else {
                            text2 = bibleVerse.getStrippedText();
                        }
                        if (contains(text2, text3)) {
                            TextFlow textFlow = new TextFlow();
                            Text reference = new Text(book.getShortOrTitle() + " " + (iPart + 1) + ":" + (iVerse + 1) + " ");
                            setReferenceTextColor(reference);
                            textFlow.getChildren().add(reference);
                            char[] chars = stripAccents(verse).toLowerCase().toCharArray();
                            char[] searchTextChars = text3.toCharArray();
                            int verseIndex = 0;
                            int fromIndex = 0;
                            int lastAddedIndex = 0;
                            for (int i = 0; i < chars.length; ++i) {
                                if ('a' <= chars[i] && chars[i] <= 'z') {
                                    if (verseIndex < searchTextChars.length && chars[i] == searchTextChars[verseIndex]) {
                                        if (verseIndex == 0) {
                                            fromIndex = i;
                                        }
                                        ++verseIndex;
                                        if (verseIndex == searchTextChars.length) {
                                            if (lastAddedIndex != fromIndex) {
                                                Text text1 = new Text(verse.substring(lastAddedIndex, fromIndex));
                                                setGeneralTextColor(text1);
                                                textFlow.getChildren().add(text1);
                                            }
                                            Text foundText = new Text(verse.substring(fromIndex, i + 1));
                                            setFoundTextColor(foundText);
                                            foundText.setFont(Font.font(foundText.getFont().getFamily(), FontWeight.BOLD, foundText.getFont().getSize() + 1));
                                            addTextWithBackGround(textFlow, foundText);
                                            lastAddedIndex = i + 1;
                                            verseIndex = 0;
                                        }
                                    } else {
                                        if (verseIndex != 0) {
                                            --i;
                                            verseIndex = 0;
                                        }
                                    }
                                }
                            }
                            if (lastAddedIndex < verse.length()) {
                                Text text1 = new Text(verse.substring(lastAddedIndex));
                                setGeneralTextColor(text1);
                                textFlow.getChildren().add(text1);
                            }
                            textFlow.setTextAlignment(TextAlignment.JUSTIFY);
                            textFlow.setPrefWidth(500.0);
                            tmpSearchListView.add(textFlow);
                            tmpSearchIBook.add(iBook);
                            tmpSearchIPart.add(iPart);
                            tmpSearchIVerse.add(iVerse);
                            ++results;
                            if (results == maxResults) {
                                break;
                            }
                        }
                    }
                }
            }

            if (tmp.equals(tmp2)) {
                System.out.println(Thread.currentThread().getName() + " newText: " + getSearchText());
                Platform.runLater(() -> {
                    searchListView.getItems().clear();
                    for (TextFlow i : tmpSearchListView) {
                        searchListView.getItems().add(i);
                    }
                    searchIBook = tmpSearchIBook;
                    searchIPart = tmpSearchIPart;
                    searchIVerse = tmpSearchIVerse;
                    System.out.println(Thread.currentThread().getName() + " search ended");
                });
            }
            System.out.println(Thread.currentThread().getName() + " ed");
        });
        thread.start();
    }

    void setBibleController(BibleController bibleController) {
        this.bibleController = bibleController;
    }

    void setSearchSelected(Integer searchSelected) {
        this.searchSelected = searchSelected;
    }

    void initializeBibles() {
        if (bible == null) {
            bibleController.initializeBibles();
        }
    }

    void setMainController(MyController mainController) {
        try {
            this.mainController = mainController;
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
    }

    public void setBible(Bible bible) {
        this.bible = bible;
    }
}
