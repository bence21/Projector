package projector.controller;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.MultipleSelectionModel;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
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
import projector.model.Language;
import projector.utils.BibleVerseTextFlow;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static projector.utils.ColorUtil.getSubduedTextColor;
import static projector.utils.StringUtils.stripAccents;

import static projector.controller.BibleController.addTextWithBackGround;
import static projector.controller.BibleController.setFoundTextColor;
import static projector.controller.BibleController.setGeneralTextColor;
import static projector.controller.BibleController.setReferenceTextColor;

public class BibleSearchController {

    private static final Logger LOG = LoggerFactory.getLogger(BibleSearchController.class);
    private static final String CATEGORY_HEADER_USER_DATA = "bible-category-header";
    private static final int CATEGORY_HEADER_INDEX = -1;

    @FXML
    private TextField bibleSearchTextField;
    @FXML
    private ListView<TextFlow> searchListView;
    @FXML
    private Label searchScopeLabel;
    @FXML
    private FlowPane searchScopePane;
    @FXML
    private MenuButton addBibleMenuButton;
    @FXML
    private HBox searchScopeSuggestionsPane;
    @FXML
    private Hyperlink addSameLanguageLink;
    @FXML
    private Hyperlink addAllBiblesLink;

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
    private final LinkedHashSet<Bible> includedBibles = new LinkedHashSet<>();
    private boolean updatingScopeUi = false;

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
            if (index < 0 || isCategoryHeaderIndex(index)) {
                return;
            }
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
        });
        initializeScopeControls();
        resetScopeToCurrent();
    }

    private void initializeScopeControls() {
        ResourceBundle bundle = Settings.getInstance().getResourceBundle();
        searchScopeLabel.setText(bundle.getString("Bible search in"));
        addBibleMenuButton.setText(bundle.getString("Add"));
        addAllBiblesLink.setText(bundle.getString("Search all bibles"));
        addSameLanguageLink.setOnAction(event -> includeSameLanguageBibles());
        addAllBiblesLink.setOnAction(event -> includeAllBibles());
    }

    private boolean isCategoryHeaderIndex(int index) {
        return searchIBook == null
                || index >= searchIBook.size()
                || searchIBook.get(index) == null
                || searchIBook.get(index) == CATEGORY_HEADER_INDEX;
    }

    public boolean contains(String a, String b) {
        return a != null && b != null && a.contains(b);
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
                List<Bible> biblesToSearch = getBiblesToSearch();
                if (biblesToSearch.isEmpty()) {
                    fillResults(tmpSearchListView, tmpSearchIBook, tmpSearchIPart, tmpSearchIVerse, tmpSearchIBible);
                    return;
                }
                boolean categorizeByBible = biblesToSearch.size() > 1;
                for (Bible bible : biblesToSearch) {
                    searchInBibleCategorized(text3, tmpSearchListView, tmpSearchIBook, tmpSearchIPart, tmpSearchIVerse,
                            bible, tmpSearchIBible, categorizeByBible);
                }
                fillResults(tmpSearchListView, tmpSearchIBook, tmpSearchIPart, tmpSearchIVerse, tmpSearchIBible);
            }
        });
        thread.start();
    }

    private List<Bible> getBiblesToSearch() {
        return orderedIncludedBibles();
    }

    private void searchInBibleCategorized(String text3, List<TextFlow> tmpSearchListView, List<Integer> tmpSearchIBook,
                                          List<Integer> tmpSearchIPart, List<Integer> tmpSearchIVerse, Bible bible,
                                          List<Bible> tmpSearchIBible, boolean categorizeByBible) {
        int sizeBefore = tmpSearchListView.size();
        searchInBible(text3, tmpSearchListView, tmpSearchIBook, tmpSearchIPart, tmpSearchIVerse, bible, tmpSearchIBible,
                !categorizeByBible);
        if (categorizeByBible && tmpSearchListView.size() > sizeBefore) {
            insertCategoryHeader(sizeBefore, bible, tmpSearchListView, tmpSearchIBook, tmpSearchIPart, tmpSearchIVerse, tmpSearchIBible);
        }
    }

    private void insertCategoryHeader(int index, Bible bible, List<TextFlow> tmpSearchListView, List<Integer> tmpSearchIBook,
                                      List<Integer> tmpSearchIPart, List<Integer> tmpSearchIVerse, List<Bible> tmpSearchIBible) {
        tmpSearchListView.add(index, createBibleCategoryHeader(bible));
        tmpSearchIBible.add(index, null);
        tmpSearchIBook.add(index, CATEGORY_HEADER_INDEX);
        tmpSearchIPart.add(index, CATEGORY_HEADER_INDEX);
        tmpSearchIVerse.add(index, CATEGORY_HEADER_INDEX);
    }

    private TextFlow createBibleCategoryHeader(Bible bible) {
        TextFlow textFlow = new TextFlow();
        textFlow.setUserData(CATEGORY_HEADER_USER_DATA);
        Text label = new Text(bibleCategoryLabel(bible));
        label.setFill(getSubduedTextColor());
        label.setStyle("-fx-font-style: italic;");
        Font font = label.getFont();
        label.setFont(Font.font(font.getName(), FontWeight.BOLD, FontPosture.ITALIC, font.getSize() + 1));
        textFlow.getChildren().add(label);
        textFlow.setPrefWidth(500.0);
        return textFlow;
    }

    private static String bibleCategoryLabel(Bible bible) {
        String shortName = bible.getShortName();
        if (shortName != null && !shortName.isBlank()) {
            return shortName;
        }
        String name = bible.getName();
        return name != null ? name : "";
    }

    private void searchInBible(String text3, List<TextFlow> tmpSearchListView, List<Integer> tmpSearchIBook,
                               List<Integer> tmpSearchIPart, List<Integer> tmpSearchIVerse, Bible bible,
                               List<Bible> tmpSearchIBible, boolean addBibleAbbreviation) {
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
                        if (text2 == null) {
                            String text = bibleVerse.getText();
                            if (text != null) {
                                text2 = stripAccents(text.toLowerCase(Locale.US));
                            }
                        }
                    }
                    if (contains(text2, text3)) {
                        TextFlow textFlow = new TextFlow();
                        if (addBibleAbbreviation) {
                            addBibleAbbreviationForOther(textFlow, bible);
                        }
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
        if (currentBible != null && currentBible.equivalent(bible)) {
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

    private void resetScopeToCurrent() {
        includedBibles.clear();
        if (currentBible != null) {
            includedBibles.add(currentBible);
        }
        rebuildScopeUi();
    }

    private void rebuildScopeUi() {
        if (!initialized || searchScopePane == null) {
            return;
        }
        updatingScopeUi = true;
        searchScopePane.getChildren().clear();
        for (Bible bible : orderedIncludedBibles()) {
            CheckBox checkBox = new CheckBox(bibleCategoryLabel(bible));
            checkBox.setSelected(true);
            checkBox.setUserData(bible);
            checkBox.selectedProperty().addListener((obs, oldValue, selected) -> {
                if (updatingScopeUi || selected) {
                    return;
                }
                includedBibles.remove(bible);
                rebuildScopeUi();
                search();
            });
            searchScopePane.getChildren().add(checkBox);
        }
        rebuildAddBibleMenu();
        updateScopeSuggestions();
        updatingScopeUi = false;
    }

    private List<Bible> orderedIncludedBibles() {
        List<Bible> ordered = new ArrayList<>();
        if (currentBible != null && includedBibles.contains(currentBible)) {
            ordered.add(currentBible);
        }
        if (bibles != null) {
            for (Bible bible : bibles) {
                if (includedBibles.contains(bible) && !ordered.contains(bible)) {
                    ordered.add(bible);
                }
            }
        }
        for (Bible bible : includedBibles) {
            if (!ordered.contains(bible)) {
                ordered.add(bible);
            }
        }
        return ordered;
    }

    private void rebuildAddBibleMenu() {
        addBibleMenuButton.getItems().clear();
        if (bibles == null) {
            addBibleMenuButton.setVisible(false);
            return;
        }
        boolean hasAddable = false;
        for (Bible bible : bibles) {
            if (includedBibles.contains(bible)) {
                continue;
            }
            hasAddable = true;
            MenuItem item = new MenuItem(bibleCategoryLabel(bible));
            item.setOnAction(event -> includeBible(bible));
            addBibleMenuButton.getItems().add(item);
        }
        addBibleMenuButton.setVisible(hasAddable);
    }

    private void updateScopeSuggestions() {
        ResourceBundle bundle = Settings.getInstance().getResourceBundle();
        List<Bible> sameLanguage = getSameLanguageBiblesNotIncluded();
        boolean hasSameLanguage = !sameLanguage.isEmpty();
        addSameLanguageLink.setVisible(hasSameLanguage);
        if (hasSameLanguage) {
            String languageLabel = languageLabel(currentBible != null ? currentBible.getLanguage() : null);
            addSameLanguageLink.setText(MessageFormat.format(bundle.getString("Search same language bibles"), languageLabel));
        }
        boolean hasOtherBibles = hasBiblesNotIncluded();
        addAllBiblesLink.setVisible(hasOtherBibles && !hasSameLanguage);
        boolean showSuggestions = hasSameLanguage || (hasOtherBibles && !hasSameLanguage);
        searchScopeSuggestionsPane.setVisible(showSuggestions);
        searchScopeSuggestionsPane.setManaged(showSuggestions);
    }

    private static String languageLabel(Language language) {
        if (language == null) {
            return "";
        }
        String nativeName = language.getNativeName();
        if (nativeName != null && !nativeName.isBlank()) {
            return nativeName;
        }
        String englishName = language.getEnglishName();
        return englishName != null ? englishName : "";
    }

    private List<Bible> getSameLanguageBiblesNotIncluded() {
        List<Bible> result = new ArrayList<>();
        if (currentBible == null || currentBible.getLanguage() == null || bibles == null) {
            return result;
        }
        Language currentLanguage = currentBible.getLanguage();
        for (Bible bible : bibles) {
            if (includedBibles.contains(bible)) {
                continue;
            }
            Language language = bible.getLanguage();
            if (language != null && language.equivalent(currentLanguage)) {
                result.add(bible);
            }
        }
        return result;
    }

    private boolean hasBiblesNotIncluded() {
        if (bibles == null) {
            return false;
        }
        for (Bible bible : bibles) {
            if (!includedBibles.contains(bible)) {
                return true;
            }
        }
        return false;
    }

    private void includeBible(Bible bible) {
        if (bible == null || includedBibles.contains(bible)) {
            return;
        }
        includedBibles.add(bible);
        rebuildScopeUi();
        search();
    }

    private void includeSameLanguageBibles() {
        for (Bible bible : getSameLanguageBiblesNotIncluded()) {
            includedBibles.add(bible);
        }
        rebuildScopeUi();
        search();
    }

    private void includeAllBibles() {
        if (bibles == null) {
            return;
        }
        includedBibles.addAll(bibles);
        rebuildScopeUi();
        search();
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
        if (bible != null && currentBible != null && bible.equivalent(currentBible)) {
            return;
        }
        this.currentBible = bible;
        if (initialized) {
            resetScopeToCurrent();
            search();
        }
    }

    public void setBibles(List<Bible> bibles) {
        this.bibles = bibles;
        if (initialized) {
            includedBibles.retainAll(bibles != null ? bibles : Set.of());
            if (includedBibles.isEmpty() && currentBible != null) {
                includedBibles.add(currentBible);
            }
            rebuildScopeUi();
        }
    }
}
