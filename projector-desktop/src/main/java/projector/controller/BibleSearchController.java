package projector.controller;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ListView;
import javafx.scene.control.MultipleSelectionModel;
import javafx.scene.control.TextField;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
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
import projector.utils.BibleVerseTextFlow;

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
    @FXML
    private CheckBox searchInAllCheckBox;
    private BibleController bibleController;

    private List<Bible> searchIBible;
    private List<Integer> searchIBook;
    private List<Integer> searchIPart;
    private List<Integer> searchIVerse;
    private Integer searchSelected = 0;
    private String newSearchText = "";
    private int maxResults;
    private MyController mainController;
    private boolean initialized = false;
    private Bible currentBible;
    private List<Bible> bibles;

    private static String strip(String s) {
        s = projector.utils.StringUtils.stripAccentsPreservingStructure(s).replaceAll("[^a-zA-Z]", "").toLowerCase(Locale.US).trim();
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
            search();
        });
        bibleSearchTextField.setOnKeyPressed(event -> mainController.globalKeyEventHandler().handle(event));
        searchListView.getSelectionModel().selectedIndexProperty().addListener((observable, oldValue, newValue) -> {
            int index = searchListView.getSelectionModel().selectedIndexProperty().get();
            if (index >= 0) {
                bibleController.selectBible(searchIBible.get(index));
                bibleController.addAllBooks();
                ListView<String> bookListView = bibleController.getBookListView();
                MultipleSelectionModel<String> bookListSelectionModel = bookListView.getSelectionModel();
                Integer bookIndex = searchIBook.get(index);
                if (bookListSelectionModel.getSelectedIndex() != bookIndex) {
                    searchSelected = 1;
                } else {
                    searchSelected = 0;
                }
                bookListSelectionModel.select(bookIndex);
                while (searchSelected == 1) {
                    try {
                        TimeUnit.MILLISECONDS.sleep(1);
                    } catch (InterruptedException e) {
                        LOG.error(e.getMessage(), e);
                    }
                }
                bookListView.scrollTo(bookIndex);
                ListView<Integer> partListView = bibleController.getPartListView();
                MultipleSelectionModel<Integer> partListViewSelectionModel = partListView.getSelectionModel();
                Integer chapterIndex = searchIPart.get(index);
                if (partListViewSelectionModel.getSelectedIndex() != chapterIndex) {
                    searchSelected = 2;
                } else {
                    searchSelected = 0;
                }
                int p = chapterIndex;
                partListViewSelectionModel.select(p);
                while (searchSelected == 2) {
                    try {
                        TimeUnit.MILLISECONDS.sleep(1);
                    } catch (InterruptedException e) {
                        LOG.error(e.getMessage(), e);
                    }
                }
                partListView.scrollTo(chapterIndex);
                ListView<BibleVerseTextFlow> verseListView = bibleController.getVerseListView();
                MultipleSelectionModel<BibleVerseTextFlow> verseListViewSelectionModel = verseListView.getSelectionModel();
                verseListViewSelectionModel.clearSelection();
                verseListViewSelectionModel.select(searchIVerse.get(index));
                verseListView.scrollTo(searchIVerse.get(index));
            }
        });
        initializeSearchInAllCheckBox();
    }

    private void initializeSearchInAllCheckBox() {
        searchInAllCheckBox.selectedProperty().addListener((o, old, newValue) -> search());
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

    private void search() {
        Thread thread = new Thread(() -> {
            String tmp2 = getNewSearchText();
            try {
                TimeUnit.MILLISECONDS.sleep(400);
            } catch (InterruptedException e) {
                LOG.error(e.getMessage(), e);
            }
            String tmp = getNewSearchText();

            String text3 = tmp2.toLowerCase(Locale.US).trim();

            if (!Settings.getInstance().isWithAccents()) {
                text3 = strip(text3);
            }
            text3 = text3.replace("]", "").replace("[", "");

            List<TextFlow> tmpSearchListView = new ArrayList<>();
            List<Bible> tmpSearchIBible = new ArrayList<>();
            List<Integer> tmpSearchIBook = new ArrayList<>();
            List<Integer> tmpSearchIPart = new ArrayList<>();
            List<Integer> tmpSearchIVerse = new ArrayList<>();
            if (tmp.equals(tmp2)) {
                searchInBible(text3, tmpSearchListView, tmpSearchIBook, tmpSearchIPart, tmpSearchIVerse, currentBible, tmpSearchIBible);
                if (searchingInAllBibles()) {
                    for (Bible bible : bibles) {
                        if (!bible.equivalent(currentBible)) {
                            searchInBible(text3, tmpSearchListView, tmpSearchIBook, tmpSearchIPart, tmpSearchIVerse, bible, tmpSearchIBible);
                        }
                    }
                }
                fillResults(tmpSearchListView, tmpSearchIBook, tmpSearchIPart, tmpSearchIVerse, tmpSearchIBible);
            }
        });
        thread.start();
    }

    private boolean searchingInAllBibles() {
        return searchInAllCheckBox.isSelected();
    }

    private void searchInBible(String text3, List<TextFlow> tmpSearchListView, List<Integer> tmpSearchIBook, List<Integer> tmpSearchIPart, List<Integer> tmpSearchIVerse, Bible bible, List<Bible> tmpSearchIBible) {
        boolean withAccents = Settings.getInstance().isWithAccents();
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
                    if (withAccents) {
                        text2 = bibleVerse.getText();
                    } else {
                        text2 = bibleVerse.getStrippedText();
                    }
                    if (contains(text2, text3)) {
                        TextFlow textFlow = new TextFlow();
                        addBibleAbbreviationForOther(textFlow, bible);
                        Text reference = new Text(book.getShortOrTitle() + " " + (iPart + 1) + ":" + (iVerse + 1) + " ");
                        setReferenceTextColor(reference);
                        textFlow.getChildren().add(reference);
                        char[] chars = projector.utils.StringUtils.stripAccentsPreservingStructure(verse).toLowerCase().toCharArray();
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
                        tmpSearchIBible.add(bible);
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
    }

    private void addBibleAbbreviationForOther(TextFlow textFlow, Bible bible) {
        if (currentBible.equivalent(bible)) {
            return;
        }
        Text bibleShortNameText = new Text(bible.getShortName() + " ");
        Font font = bibleShortNameText.getFont();
        bibleShortNameText.setFont(Font.font(font.getName(), FontWeight.BOLD, FontPosture.REGULAR, font.getSize()));
        setReferenceTextColor(bibleShortNameText);
        textFlow.getChildren().add(bibleShortNameText);
    }

    private void fillResults(List<TextFlow> tmpSearchListView, List<Integer> tmpSearchIBook, List<Integer> tmpSearchIPart, List<Integer> tmpSearchIVerse, List<Bible> tmpSearchIBible) {
        Platform.runLater(() -> {
            ObservableList<TextFlow> searchListViewItems = searchListView.getItems();
            searchListViewItems.clear();
            searchListViewItems.addAll(tmpSearchListView);
            searchIBible = tmpSearchIBible;
            searchIBook = tmpSearchIBook;
            searchIPart = tmpSearchIPart;
            searchIVerse = tmpSearchIVerse;
        });
    }

    void setBibleController(BibleController bibleController) {
        this.bibleController = bibleController;
    }

    void setSearchSelected(Integer searchSelected) {
        this.searchSelected = searchSelected;
    }

    void initializeBibles() {
        if (currentBible == null) {
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
        this.currentBible = bible;
    }

    public void setBibles(List<Bible> bibles) {
        this.bibles = bibles;
    }
}
