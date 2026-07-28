package projector.controller;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.MultipleSelectionModel;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.text.TextFlow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import projector.application.Settings;
import projector.controller.biblesearch.BibleContentSnapshots;
import projector.controller.biblesearch.BibleSearchFilterSnapshot;
import projector.controller.biblesearch.BibleSearchMatcher;
import projector.controller.biblesearch.BibleSearchPreferences;
import projector.controller.biblesearch.BookFilterEntry;
import projector.model.Bible;
import projector.model.BibleVerse;
import projector.model.Book;
import projector.model.Chapter;
import projector.model.Language;
import projector.utils.BibleVerseTextFlow;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static projector.controller.BibleController.addTextWithBackGround;
import static projector.controller.BibleController.setFoundTextColor;
import static projector.controller.BibleController.setGeneralTextColor;
import static projector.controller.BibleController.setReferenceTextColor;

public class BibleSearchController {

    private static final Logger LOG = LoggerFactory.getLogger(BibleSearchController.class);
    private static final int STANDARD_OLD_TESTAMENT_BOOKS = 39;

    @FXML
    private TextField bibleSearchTextField;
    @FXML
    private ListView<TextFlow> searchListView;
    @FXML
    private Label filterSummaryLabel;
    @FXML
    private Hyperlink clearFiltersLink;
    @FXML
    private SplitPane mainSplitPane;
    @FXML
    private TitledPane biblesPane;
    @FXML
    private TitledPane optionsPane;
    @FXML
    private TitledPane rangePane;
    @FXML
    private VBox bibleCheckboxesPane;
    @FXML
    private CheckBox sameLanguageCheckBox;
    @FXML
    private CheckBox allBiblesCheckBox;
    @FXML
    private CheckBox caseSensitiveCheckBox;
    @FXML
    private CheckBox wholeWordCheckBox;
    @FXML
    private CheckBox accentsCheckBox;
    @FXML
    private Button restoreDefaultsButton;
    @FXML
    private CheckBox oldTestamentCheckBox;
    @FXML
    private CheckBox newTestamentCheckBox;
    @FXML
    private ListView<BookFilterEntry> bookFilterListView;
    @FXML
    private TextField chapterFromField;
    @FXML
    private TextField chapterToField;
    @FXML
    private FlowPane chapterCheckboxesPane;

    private BibleController bibleController;
    private MyController mainController;

    private List<Bible> searchIBible;
    private List<Integer> searchIBook;
    private List<Integer> searchIPart;
    private List<Integer> searchIVerse;
    private Integer searchSelected = 0;
    private String newSearchText = "";
    private int maxResults;
    private boolean initialized;
    private Bible currentBible;
    private List<Bible> bibles;
    private final LinkedHashSet<Bible> includedBibles = new LinkedHashSet<>();
    private final BibleSearchPreferences preferences = BibleSearchPreferences.load();
    private final ObservableList<BookFilterEntry> bookFilterEntries = FXCollections.observableArrayList();
    private boolean updatingScopeUi;
    private boolean updatingTestamentUi;
    private boolean updatingBulkBibleUi;
    private int selectedBookForChapters = -1;

    void lazyInitialize() {
        if (initialized) {
            return;
        }
        initialized = true;
        maxResults = 1200;
        applyPreferencesToUi();
        bibleSearchTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            setNewSearchText(newValue);
            search();
        });
        bibleSearchTextField.setOnKeyPressed(event -> mainController.globalKeyEventHandler().handle(event));
        searchListView.getSelectionModel().selectedIndexProperty().addListener((observable, oldValue, newValue) -> {
            int index = searchListView.getSelectionModel().getSelectedIndex();
            if (index < 0) {
                return;
            }
            navigateToVerse(index);
        });
        initializeFilterControls();
        restoreIncludedBiblesFromPreferences();
        rebuildScopeUi();
        rebuildBookFilterList();
        search();
    }

    private void applyPreferencesToUi() {
        mainSplitPane.setDividerPositions(preferences.getSplitDividerPosition());
        mainSplitPane.getDividers().get(0).positionProperty().addListener((obs, oldValue, newValue) -> {
            preferences.setSplitDividerPosition(newValue.doubleValue());
            savePreferences();
        });
        biblesPane.setExpanded(preferences.isBiblesPaneExpanded());
        optionsPane.setExpanded(preferences.isOptionsPaneExpanded());
        rangePane.setExpanded(preferences.isRangePaneExpanded());
        biblesPane.expandedProperty().addListener((obs, oldValue, expanded) -> {
            preferences.setBiblesPaneExpanded(expanded);
            savePreferences();
        });
        optionsPane.expandedProperty().addListener((obs, oldValue, expanded) -> {
            preferences.setOptionsPaneExpanded(expanded);
            savePreferences();
        });
        rangePane.expandedProperty().addListener((obs, oldValue, expanded) -> {
            preferences.setRangePaneExpanded(expanded);
            savePreferences();
        });
        caseSensitiveCheckBox.setSelected(preferences.isCaseSensitive());
        wholeWordCheckBox.setSelected(preferences.isWholeWord());
        accentsCheckBox.setSelected(resolveWithAccents());
        if (preferences.getChapterFrom() != null) {
            chapterFromField.setText(String.valueOf(preferences.getChapterFrom()));
        }
        if (preferences.getChapterTo() != null) {
            chapterToField.setText(String.valueOf(preferences.getChapterTo()));
        }
    }

    private void initializeFilterControls() {
        ResourceBundle bundle = Settings.getInstance().getResourceBundle();
        clearFiltersLink.setText(bundle.getString("Clear filters"));
        restoreDefaultsButton.setText(bundle.getString("Restore defaults"));
        sameLanguageCheckBox.setOnAction(event -> toggleSameLanguageBibles());
        allBiblesCheckBox.setOnAction(event -> toggleAllBibles());
        caseSensitiveCheckBox.setOnAction(event -> {
            preferences.setCaseSensitive(caseSensitiveCheckBox.isSelected());
            savePreferences();
            search();
        });
        wholeWordCheckBox.setOnAction(event -> {
            preferences.setWholeWord(wholeWordCheckBox.isSelected());
            savePreferences();
            search();
        });
        accentsCheckBox.setOnAction(event -> {
            boolean global = Settings.getInstance().isWithAccents();
            if (accentsCheckBox.isSelected() == global) {
                preferences.setAccentsOverride(null);
            } else {
                preferences.setAccentsOverride(accentsCheckBox.isSelected());
            }
            savePreferences();
            search();
        });
        restoreDefaultsButton.setOnAction(event -> restoreDefaults());
        clearFiltersLink.setOnAction(event -> restoreDefaults());
        oldTestamentCheckBox.setOnAction(event -> applyTestamentSelection(true, oldTestamentCheckBox.isSelected()));
        newTestamentCheckBox.setOnAction(event -> applyTestamentSelection(false, newTestamentCheckBox.isSelected()));
        chapterFromField.focusedProperty().addListener((obs, oldValue, focused) -> {
            if (!focused) {
                applyChapterRangeFields();
            }
        });
        chapterToField.focusedProperty().addListener((obs, oldValue, focused) -> {
            if (!focused) {
                applyChapterRangeFields();
            }
        });
        bookFilterListView.setItems(bookFilterEntries);
        bookFilterListView.setCellFactory(listView -> new BookFilterCell());
        bookFilterListView.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, entry) -> {
            if (entry != null) {
                selectedBookForChapters = entry.getBookIndex();
                rebuildChapterCheckboxes();
            }
        });
    }

    private void restoreIncludedBiblesFromPreferences() {
        includedBibles.clear();
        Set<Long> savedIds = preferences.getIncludedBibleIds();
        if (bibles != null && !savedIds.isEmpty()) {
            for (Bible bible : bibles) {
                Long id = bible.getId();
                if (id != null && savedIds.contains(id)) {
                    includedBibles.add(bible);
                }
            }
        }
        if (includedBibles.isEmpty() && currentBible != null) {
            includedBibles.add(currentBible);
        }
    }

    private synchronized String getNewSearchText() {
        return newSearchText;
    }

    private synchronized void setNewSearchText(String newText) {
        this.newSearchText = newText;
    }

    private boolean resolveWithAccents() {
        Boolean override = preferences.getAccentsOverride();
        if (override != null) {
            return override;
        }
        return Settings.getInstance().isWithAccents();
    }

    private void search() {
        Thread thread = new Thread(() -> {
            String querySnapshot = getNewSearchText();
            try {
                TimeUnit.MILLISECONDS.sleep(400);
            } catch (InterruptedException e) {
                LOG.error(e.getMessage(), e);
            }
            if (!querySnapshot.equals(getNewSearchText())) {
                return;
            }
            boolean withAccents = resolveWithAccents();
            boolean caseSensitive = preferences.isCaseSensitive();
            boolean wholeWord = preferences.isWholeWord();
            String normalizedQuery = BibleSearchMatcher.normalizeQuery(querySnapshot, withAccents, caseSensitive);
            BibleSearchFilterSnapshot filters = BibleSearchFilterSnapshot.from(preferences);
            if (normalizedQuery.isEmpty()) {
                fillResults(List.of(), List.of(), List.of(), List.of(), List.of(), Map.of());
                updateFilterSummary(0);
                return;
            }

            List<TextFlow> tmpSearchListView = new ArrayList<>();
            List<Bible> tmpSearchIBible = new ArrayList<>();
            List<Integer> tmpSearchIBook = new ArrayList<>();
            List<Integer> tmpSearchIPart = new ArrayList<>();
            List<Integer> tmpSearchIVerse = new ArrayList<>();
            Map<Integer, Integer> bookCounts = new HashMap<>();
            List<Bible> biblesToSearch = new ArrayList<>(getBiblesToSearch());
            if (biblesToSearch.isEmpty()) {
                fillResults(tmpSearchListView, tmpSearchIBook, tmpSearchIPart, tmpSearchIVerse, tmpSearchIBible, bookCounts);
                updateFilterSummary(0);
                return;
            }
            boolean addAbbreviation = biblesToSearch.size() > 1;
            for (Bible bible : biblesToSearch) {
                searchInBible(normalizedQuery, tmpSearchListView, tmpSearchIBook, tmpSearchIPart, tmpSearchIVerse,
                        bible, tmpSearchIBible, addAbbreviation, withAccents, caseSensitive, wholeWord, filters, bookCounts);
            }
            fillResults(tmpSearchListView, tmpSearchIBook, tmpSearchIPart, tmpSearchIVerse, tmpSearchIBible, bookCounts);
            updateFilterSummary(tmpSearchListView.size());
        });
        thread.start();
    }

    private List<Bible> getBiblesToSearch() {
        return orderedIncludedBibles();
    }

    private void searchInBible(String normalizedQuery, List<TextFlow> tmpSearchListView, List<Integer> tmpSearchIBook,
                               List<Integer> tmpSearchIPart, List<Integer> tmpSearchIVerse, Bible bible,
                               List<Bible> tmpSearchIBible, boolean addBibleAbbreviation,
                               boolean withAccents, boolean caseSensitive, boolean wholeWord,
                               BibleSearchFilterSnapshot filters, Map<Integer, Integer> bookCounts) {
        int results = 0;
        List<Book> books = BibleContentSnapshots.books(bible);
        for (int iBook = 0; iBook < books.size(); ++iBook) {
            if (!filters.isBookIncluded(iBook)) {
                continue;
            }
            Book book = books.get(iBook);
            List<Chapter> chapters = BibleContentSnapshots.chapters(book);
            for (int iPart = 0; iPart < chapters.size(); ++iPart) {
                if (!filters.isChapterIncluded(iBook, iPart + 1)) {
                    continue;
                }
                Chapter chapter = chapters.get(iPart);
                List<BibleVerse> bibleVerses = BibleContentSnapshots.verses(chapter);
                for (int iVerse = 0; iVerse < bibleVerses.size(); ++iVerse) {
                    BibleVerse bibleVerse = bibleVerses.get(iVerse);
                    String searchableText = BibleSearchMatcher.verseTextForSearch(bibleVerse, withAccents, caseSensitive);
                    if (!BibleSearchMatcher.matches(searchableText, normalizedQuery, wholeWord)) {
                        continue;
                    }
                    bookCounts.merge(iBook, 1, Integer::sum);
                    if (results >= maxResults) {
                        continue;
                    }
                    String verse = bibleVerse.getText();
                    TextFlow textFlow = buildHighlightedResult(verse, normalizedQuery, withAccents, caseSensitive);
                    if (addBibleAbbreviation) {
                        addBibleAbbreviationForOther(textFlow, bible);
                    }
                    Text reference = new Text(book.getShortOrTitle() + " " + (iPart + 1) + ":" + (iVerse + 1) + " ");
                    setReferenceTextColor(reference);
                    textFlow.getChildren().add(0, reference);
                    textFlow.setTextAlignment(TextAlignment.JUSTIFY);
                    textFlow.setPrefWidth(500.0);
                    tmpSearchListView.add(textFlow);
                    tmpSearchIBible.add(bible);
                    tmpSearchIBook.add(iBook);
                    tmpSearchIPart.add(iPart);
                    tmpSearchIVerse.add(iVerse);
                    ++results;
                }
            }
        }
    }

    private TextFlow buildHighlightedResult(String verse, String normalizedQuery, boolean withAccents, boolean caseSensitive) {
        TextFlow textFlow = new TextFlow();
        if (verse == null || normalizedQuery.isEmpty()) {
            return textFlow;
        }
        if (!withAccents) {
            return buildHighlightedResultLetterScan(verse, normalizedQuery, caseSensitive);
        }
        String compareVerse = verse;
        String query = normalizedQuery;
        if (!caseSensitive) {
            compareVerse = compareVerse.toLowerCase(Locale.US);
            query = query.toLowerCase(Locale.US);
        }
        int from = 0;
        while (from <= compareVerse.length() - query.length()) {
            int index = compareVerse.indexOf(query, from);
            if (index < 0) {
                break;
            }
            if (from < index) {
                Text text1 = new Text(verse.substring(from, index));
                setGeneralTextColor(text1);
                textFlow.getChildren().add(text1);
            }
            Text foundText = new Text(verse.substring(index, index + query.length()));
            setFoundTextColor(foundText);
            foundText.setFont(Font.font(foundText.getFont().getFamily(), FontWeight.BOLD, foundText.getFont().getSize() + 1));
            addTextWithBackGround(textFlow, foundText);
            from = index + query.length();
        }
        if (from < verse.length()) {
            Text text1 = new Text(verse.substring(from));
            setGeneralTextColor(text1);
            textFlow.getChildren().add(text1);
        }
        return textFlow;
    }

    private TextFlow buildHighlightedResultLetterScan(String verse, String normalizedQuery, boolean caseSensitive) {
        TextFlow textFlow = new TextFlow();
        String preparedVerse = projector.utils.StringUtils.stripAccentsPreservingStructure(verse);
        char[] queryChars = normalizedQuery.toCharArray();
        int queryIndex = 0;
        int matchStart = -1;
        int lastAdded = 0;
        for (int i = 0; i < preparedVerse.length(); ++i) {
            char verseChar = preparedVerse.charAt(i);
            if (!Character.isLetter(verseChar)) {
                if (queryIndex != 0) {
                    i = matchStart;
                    queryIndex = 0;
                    matchStart = -1;
                }
                continue;
            }
            char compareChar = caseSensitive ? verseChar : Character.toLowerCase(verseChar);
            char queryChar = queryChars[queryIndex];
            if (compareChar == queryChar) {
                if (queryIndex == 0) {
                    matchStart = i;
                }
                ++queryIndex;
                if (queryIndex == queryChars.length) {
                    if (lastAdded < matchStart) {
                        Text text1 = new Text(verse.substring(lastAdded, matchStart));
                        setGeneralTextColor(text1);
                        textFlow.getChildren().add(text1);
                    }
                    Text foundText = new Text(verse.substring(matchStart, i + 1));
                    setFoundTextColor(foundText);
                    foundText.setFont(Font.font(foundText.getFont().getFamily(), FontWeight.BOLD, foundText.getFont().getSize() + 1));
                    addTextWithBackGround(textFlow, foundText);
                    lastAdded = i + 1;
                    queryIndex = 0;
                    matchStart = -1;
                }
            } else if (queryIndex != 0) {
                i = matchStart;
                queryIndex = 0;
                matchStart = -1;
            }
        }
        if (lastAdded < verse.length()) {
            Text text1 = new Text(verse.substring(lastAdded));
            setGeneralTextColor(text1);
            textFlow.getChildren().add(text1);
        }
        return textFlow;
    }

    private void addBibleAbbreviationForOther(TextFlow textFlow, Bible bible) {
        if (currentBible != null && currentBible.equivalent(bible)) {
            return;
        }
        Text bibleShortNameText = new Text(bible.getShortName() + " ");
        Font font = bibleShortNameText.getFont();
        bibleShortNameText.setFont(Font.font(font.getName(), FontWeight.BOLD, FontPosture.REGULAR, font.getSize()));
        setReferenceTextColor(bibleShortNameText);
        textFlow.getChildren().add(0, bibleShortNameText);
    }

    private void fillResults(List<TextFlow> tmpSearchListView, List<Integer> tmpSearchIBook,
                             List<Integer> tmpSearchIPart, List<Integer> tmpSearchIVerse, List<Bible> tmpSearchIBible,
                             Map<Integer, Integer> bookCounts) {
        Platform.runLater(() -> {
            ObservableList<TextFlow> searchListViewItems = searchListView.getItems();
            searchListViewItems.clear();
            searchListViewItems.addAll(tmpSearchListView);
            searchIBible = tmpSearchIBible;
            searchIBook = tmpSearchIBook;
            searchIPart = tmpSearchIPart;
            searchIVerse = tmpSearchIVerse;
            for (BookFilterEntry entry : bookFilterEntries) {
                if (bookCounts.isEmpty()) {
                    entry.matchCountProperty().set(-1);
                } else {
                    entry.matchCountProperty().set(bookCounts.getOrDefault(entry.getBookIndex(), 0));
                }
            }
            bookFilterListView.refresh();
        });
    }

    private void rebuildScopeUi() {
        if (!initialized || bibleCheckboxesPane == null) {
            return;
        }
        updatingScopeUi = true;
        bibleCheckboxesPane.getChildren().clear();
        if (bibles != null) {
            for (Bible bible : bibles) {
                CheckBox checkBox = new CheckBox(bibleLabel(bible));
                checkBox.setSelected(includedBibles.contains(bible));
                checkBox.setUserData(bible);
                checkBox.selectedProperty().addListener((obs, oldValue, selected) -> {
                    if (updatingScopeUi) {
                        return;
                    }
                    Bible target = (Bible) checkBox.getUserData();
                    if (selected) {
                        includedBibles.add(target);
                    } else {
                        includedBibles.remove(target);
                    }
                    persistIncludedBibles();
                    syncBulkBibleCheckboxes();
                    search();
                });
                bibleCheckboxesPane.getChildren().add(checkBox);
            }
        }
        syncBulkBibleCheckboxes();
        updatingScopeUi = false;
        updateFilterSummary(searchListView.getItems().size());
    }

    private void syncBulkBibleCheckboxes() {
        updatingBulkBibleUi = true;
        ResourceBundle bundle = Settings.getInstance().getResourceBundle();
        List<Bible> sameLanguage = getSameLanguageBibles();
        boolean allSameLanguageIncluded = !sameLanguage.isEmpty() && includedBibles.containsAll(sameLanguage);
        sameLanguageCheckBox.setVisible(!sameLanguage.isEmpty());
        sameLanguageCheckBox.setManaged(!sameLanguage.isEmpty());
        if (!sameLanguage.isEmpty()) {
            String language = languageLabel(currentBible != null ? currentBible.getLanguage() : null);
            if (language.isBlank()) {
                sameLanguageCheckBox.setText(bundle.getString("Search in same-language bibles"));
            } else {
                sameLanguageCheckBox.setText(MessageFormat.format(bundle.getString("Search in language bibles"), language));
            }
            sameLanguageCheckBox.setSelected(allSameLanguageIncluded);
        }
        boolean showAll = hasBiblesOutsideCurrentLanguage();
        allBiblesCheckBox.setVisible(showAll);
        allBiblesCheckBox.setManaged(showAll);
        if (showAll) {
            allBiblesCheckBox.setSelected(bibles != null && includedBibles.containsAll(bibles));
        }
        updatingBulkBibleUi = false;
    }

    private void toggleSameLanguageBibles() {
        if (updatingBulkBibleUi) {
            return;
        }
        if (sameLanguageCheckBox.isSelected()) {
            includedBibles.addAll(getSameLanguageBibles());
        } else {
            includedBibles.removeAll(getSameLanguageBibles());
        }
        persistIncludedBibles();
        rebuildScopeUi();
        search();
    }

    private void toggleAllBibles() {
        if (updatingBulkBibleUi || bibles == null) {
            return;
        }
        if (allBiblesCheckBox.isSelected()) {
            includedBibles.addAll(bibles);
        } else if (currentBible != null) {
            includedBibles.clear();
            includedBibles.add(currentBible);
        } else {
            includedBibles.clear();
        }
        persistIncludedBibles();
        rebuildScopeUi();
        search();
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

    private List<Bible> getSameLanguageBibles() {
        List<Bible> result = new ArrayList<>();
        if (currentBible == null || currentBible.getLanguage() == null || bibles == null) {
            return result;
        }
        Language currentLanguage = currentBible.getLanguage();
        for (Bible bible : bibles) {
            Language language = bible.getLanguage();
            if (language != null && language.equivalent(currentLanguage)) {
                result.add(bible);
            }
        }
        return result;
    }

    private boolean hasBiblesOutsideCurrentLanguage() {
        if (bibles == null || currentBible == null || currentBible.getLanguage() == null) {
            return false;
        }
        Language currentLanguage = currentBible.getLanguage();
        for (Bible bible : bibles) {
            Language language = bible.getLanguage();
            if (language == null || !language.equivalent(currentLanguage)) {
                return true;
            }
        }
        return false;
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

    private static String bibleLabel(Bible bible) {
        String shortName = bible.getShortName();
        if (shortName != null && !shortName.isBlank()) {
            return shortName;
        }
        String name = bible.getName();
        return name != null ? name : "";
    }

    private void rebuildBookFilterList() {
        bookFilterEntries.clear();
        if (currentBible == null) {
            return;
        }
        List<Book> books = currentBible.getBooks();
        Set<Integer> excluded = new HashSet<>(preferences.getExcludedBookIndices());
        for (int i = 0; i < books.size(); ++i) {
            BookFilterEntry entry = new BookFilterEntry(i, books.get(i).getShortOrTitle(), !excluded.contains(i));
            entry.selectedProperty().addListener((obs, oldValue, selected) -> {
                persistExcludedBooks();
                syncTestamentCheckboxes();
                search();
            });
            entry.matchCountProperty().addListener((obs, oldValue, newValue) -> {
                if (bookFilterListView.getSelectionModel().getSelectedItem() == entry) {
                    bookFilterListView.refresh();
                }
            });
            bookFilterEntries.add(entry);
        }
        bookFilterListView.getSelectionModel().selectFirst();
        selectedBookForChapters = bookFilterEntries.isEmpty() ? -1 : 0;
        syncTestamentCheckboxes();
        rebuildChapterCheckboxes();
    }

    private void syncTestamentCheckboxes() {
        if (updatingTestamentUi || bookFilterEntries.isEmpty()) {
            return;
        }
        updatingTestamentUi = true;
        oldTestamentCheckBox.setSelected(isTestamentFullySelected(0, STANDARD_OLD_TESTAMENT_BOOKS));
        int newTestamentStart = Math.min(STANDARD_OLD_TESTAMENT_BOOKS, bookFilterEntries.size());
        newTestamentCheckBox.setSelected(isTestamentFullySelected(newTestamentStart, bookFilterEntries.size()));
        updatingTestamentUi = false;
    }

    private boolean isTestamentFullySelected(int fromInclusive, int toExclusive) {
        for (int i = fromInclusive; i < toExclusive && i < bookFilterEntries.size(); ++i) {
            if (!bookFilterEntries.get(i).selectedProperty().get()) {
                return false;
            }
        }
        return fromInclusive < toExclusive;
    }

    private void applyTestamentSelection(boolean oldTestament, boolean selected) {
        if (updatingTestamentUi) {
            return;
        }
        int from = oldTestament ? 0 : STANDARD_OLD_TESTAMENT_BOOKS;
        int to = oldTestament ? Math.min(STANDARD_OLD_TESTAMENT_BOOKS, bookFilterEntries.size()) : bookFilterEntries.size();
        for (int i = from; i < to; ++i) {
            bookFilterEntries.get(i).selectedProperty().set(selected);
        }
        persistExcludedBooks();
        search();
    }

    private void rebuildChapterCheckboxes() {
        chapterCheckboxesPane.getChildren().clear();
        if (currentBible == null || selectedBookForChapters < 0) {
            return;
        }
        List<Book> books = currentBible.getBooks();
        if (selectedBookForChapters >= books.size()) {
            return;
        }
        Book book = books.get(selectedBookForChapters);
        int chapterCount = book.getChapters().size();
        Set<Integer> selectedChapters = preferences.getChaptersByBook()
                .computeIfAbsent(selectedBookForChapters, key -> new HashSet<>());
        for (int chapter = 1; chapter <= chapterCount; ++chapter) {
            CheckBox checkBox = new CheckBox(String.valueOf(chapter));
            checkBox.setSelected(selectedChapters.contains(chapter));
            int chapterNumber = chapter;
            checkBox.selectedProperty().addListener((obs, oldValue, selected) -> {
                if (selected) {
                    selectedChapters.add(chapterNumber);
                } else {
                    selectedChapters.remove(chapterNumber);
                }
                if (selectedChapters.isEmpty()) {
                    preferences.getChaptersByBook().remove(selectedBookForChapters);
                }
                savePreferences();
                search();
            });
            chapterCheckboxesPane.getChildren().add(checkBox);
        }
    }

    private void applyChapterRangeFields() {
        preferences.setChapterFrom(parseChapterField(chapterFromField.getText()));
        preferences.setChapterTo(parseChapterField(chapterToField.getText()));
        savePreferences();
        search();
    }

    private static Integer parseChapterField(String text) {
        if (text == null || text.isBlank()) {
            return null;
        }
        try {
            int value = Integer.parseInt(text.trim());
            return value > 0 ? value : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private void navigateToVerse(int index) {
        if (searchIBook == null || index >= searchIBook.size()) {
            return;
        }
        bibleController.selectBible(searchIBible.get(index));
        bibleController.addAllBooks();
        ListView<String> bookListView = bibleController.getBookListView();
        MultipleSelectionModel<String> bookListSelectionModel = bookListView.getSelectionModel();
        Integer bookIndex = searchIBook.get(index);
        searchSelected = bookListSelectionModel.getSelectedIndex() != bookIndex ? 1 : 0;
        bookListSelectionModel.select(bookIndex);
        waitForSearchSelected(1);
        bookListView.scrollTo(bookIndex);
        ListView<Integer> partListView = bibleController.getPartListView();
        MultipleSelectionModel<Integer> partListViewSelectionModel = partListView.getSelectionModel();
        Integer chapterIndex = searchIPart.get(index);
        searchSelected = partListViewSelectionModel.getSelectedIndex() != chapterIndex ? 2 : 0;
        partListViewSelectionModel.select(chapterIndex);
        waitForSearchSelected(2);
        partListView.scrollTo(chapterIndex);
        ListView<BibleVerseTextFlow> verseListView = bibleController.getVerseListView();
        MultipleSelectionModel<BibleVerseTextFlow> verseListViewSelectionModel = verseListView.getSelectionModel();
        verseListViewSelectionModel.clearSelection();
        verseListViewSelectionModel.select(searchIVerse.get(index));
        verseListView.scrollTo(searchIVerse.get(index));
    }

    private void waitForSearchSelected(int expected) {
        while (searchSelected == expected) {
            try {
                TimeUnit.MILLISECONDS.sleep(1);
            } catch (InterruptedException e) {
                LOG.error(e.getMessage(), e);
                break;
            }
        }
    }

    private void navigateToBookResults(int bookIndex) {
        if (searchIBook == null) {
            return;
        }
        for (int i = 0; i < searchIBook.size(); ++i) {
            if (searchIBook.get(i).equals(bookIndex)) {
                searchListView.getSelectionModel().select(i);
                searchListView.scrollTo(i);
                return;
            }
        }
    }

    private void updateFilterSummary(int resultCount) {
        if (!initialized || filterSummaryLabel == null) {
            return;
        }
        ResourceBundle bundle = Settings.getInstance().getResourceBundle();
        List<String> parts = new ArrayList<>();
        int bibleCount = includedBibles.size();
        if (bibleCount > 1) {
            parts.add(MessageFormat.format(bundle.getString("Bible search summary bibles"), bibleCount));
        }
        long selectedBooks = bookFilterEntries.stream().filter(entry -> entry.selectedProperty().get()).count();
        if (!bookFilterEntries.isEmpty() && selectedBooks < bookFilterEntries.size()) {
            parts.add(MessageFormat.format(bundle.getString("Bible search summary books"), selectedBooks));
        }
        if (preferences.getChapterFrom() != null || preferences.getChapterTo() != null) {
            String from = preferences.getChapterFrom() != null ? String.valueOf(preferences.getChapterFrom()) : "1";
            String to = preferences.getChapterTo() != null ? String.valueOf(preferences.getChapterTo()) : "…";
            parts.add(MessageFormat.format(bundle.getString("Bible search summary chapters"), from, to));
        }
        if (!preferences.getChaptersByBook().isEmpty()) {
            parts.add(bundle.getString("Bible search summary chapter picks"));
        }
        if (!getNewSearchText().isBlank()) {
            parts.add(MessageFormat.format(bundle.getString("Bible search summary results"), resultCount));
        }
        String summary = parts.isEmpty() ? "" : String.join(" · ", parts);
        boolean showClear = hasFiltersToClear();
        Platform.runLater(() -> {
            filterSummaryLabel.setText(summary);
            filterSummaryLabel.setManaged(!summary.isBlank());
            filterSummaryLabel.setVisible(!summary.isBlank());
            clearFiltersLink.setVisible(showClear);
            clearFiltersLink.setManaged(showClear);
        });
    }

    private boolean hasFiltersToClear() {
        if (preferences.isCaseSensitive() || preferences.isWholeWord() || preferences.getAccentsOverride() != null) {
            return true;
        }
        if (!preferences.getExcludedBookIndices().isEmpty()) {
            return true;
        }
        if (preferences.getChapterFrom() != null || preferences.getChapterTo() != null) {
            return true;
        }
        if (!preferences.getChaptersByBook().isEmpty()) {
            return true;
        }
        if (includedBibles.size() > 1) {
            return true;
        }
        return currentBible != null && !includedBibles.contains(currentBible);
    }

    private void restoreDefaults() {
        preferences.resetToDefaults();
        if (currentBible != null) {
            includedBibles.clear();
            includedBibles.add(currentBible);
        } else {
            includedBibles.clear();
        }
        persistIncludedBibles();
        caseSensitiveCheckBox.setSelected(false);
        wholeWordCheckBox.setSelected(false);
        accentsCheckBox.setSelected(Settings.getInstance().isWithAccents());
        chapterFromField.clear();
        chapterToField.clear();
        optionsPane.setExpanded(false);
        rangePane.setExpanded(false);
        rebuildBookFilterList();
        rebuildScopeUi();
        savePreferences();
        search();
    }

    private void persistIncludedBibles() {
        Set<Long> ids = new LinkedHashSet<>();
        for (Bible bible : includedBibles) {
            if (bible.getId() != null) {
                ids.add(bible.getId());
            }
        }
        preferences.setIncludedBibleIds(ids);
        savePreferences();
    }

    private void persistExcludedBooks() {
        Set<Integer> excluded = new HashSet<>();
        for (BookFilterEntry entry : bookFilterEntries) {
            if (!entry.selectedProperty().get()) {
                excluded.add(entry.getBookIndex());
            }
        }
        preferences.setExcludedBookIndices(excluded);
        savePreferences();
    }

    private void savePreferences() {
        preferences.save();
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
            if (bible != null && !includedBibles.contains(bible)) {
                includedBibles.add(bible);
                persistIncludedBibles();
            }
            rebuildBookFilterList();
            rebuildScopeUi();
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
            restoreIncludedBiblesFromPreferences();
            rebuildScopeUi();
        }
    }

    private final class BookFilterCell extends ListCell<BookFilterEntry> {

        private final CheckBox checkBox = new CheckBox();
        private final Label nameLabel = new Label();
        private final Label countLabel = new Label();
        private final HBox content = new HBox(8.0);
        private BookFilterEntry boundEntry;

        private BookFilterCell() {
            content.setAlignment(Pos.CENTER_LEFT);
            checkBox.setMinWidth(Region.USE_PREF_SIZE);
            checkBox.setMaxWidth(Region.USE_PREF_SIZE);
            nameLabel.setMaxWidth(Double.MAX_VALUE);
            HBox.setHgrow(nameLabel, Priority.ALWAYS);
            countLabel.getStyleClass().add("subdued-label");
            nameLabel.setOnMouseClicked(event -> {
                if (!isEmpty() && getListView() != null) {
                    getListView().getSelectionModel().select(getItem());
                    event.consume();
                }
            });
            countLabel.setOnMouseClicked(event -> {
                if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2 && !isEmpty()) {
                    navigateToBookResults(getItem().getBookIndex());
                    event.consume();
                }
            });
            content.getChildren().addAll(checkBox, nameLabel, countLabel);
        }

        @Override
        protected void updateItem(BookFilterEntry entry, boolean empty) {
            super.updateItem(entry, empty);
            unbindEntry();
            if (empty || entry == null) {
                setGraphic(null);
                return;
            }
            boundEntry = entry;
            nameLabel.setText(entry.nameProperty().get());
            countLabel.setText(entry.getCountLabel());
            checkBox.selectedProperty().bindBidirectional(entry.selectedProperty());
            setGraphic(content);
        }

        private void unbindEntry() {
            if (boundEntry != null) {
                checkBox.selectedProperty().unbindBidirectional(boundEntry.selectedProperty());
                boundEntry = null;
            }
        }
    }
}
