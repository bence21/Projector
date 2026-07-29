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
import projector.controller.biblesearch.SearchHit;
import projector.model.Bible;
import projector.model.BibleVerse;
import projector.model.Book;
import projector.model.Chapter;
import projector.model.Language;
import projector.service.ServiceManager;
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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static projector.controller.BibleController.addTextWithBackGround;
import static projector.controller.BibleController.setFoundTextColor;
import static projector.controller.BibleController.setGeneralTextColor;
import static projector.controller.BibleController.setReferenceTextColor;

public class BibleSearchController {

    private static final Logger LOG = LoggerFactory.getLogger(BibleSearchController.class);
    private static final int STANDARD_OLD_TESTAMENT_BOOKS = 39;
    private static final int SEARCH_DEBOUNCE_MS = 400;
    private static final int SEARCH_CANCEL_CHECK_INTERVAL = 64;

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
    private Button includeLanguageBiblesButton;
    @FXML
    private Button includeAllBiblesButton;
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
    private boolean excludeCurrentBibleFromSearch;
    private final BibleSearchPreferences preferences = BibleSearchPreferences.load();
    private final ObservableList<BookFilterEntry> bookFilterEntries = FXCollections.observableArrayList();
    private boolean updatingScopeUi;
    private boolean updatingTestamentUi;
    private boolean suppressResultNavigation;
    private int selectedBookForChapters = -1;
    private ExecutorService searchExecutor;
    private final AtomicInteger searchGeneration = new AtomicInteger();

    void lazyInitialize() {
        if (initialized) {
            return;
        }
        initialized = true;
        maxResults = 1200;
        searchExecutor = Executors.newSingleThreadExecutor(runnable -> {
            Thread thread = new Thread(runnable, "bible-search");
            thread.setDaemon(true);
            thread.setPriority(Thread.MIN_PRIORITY);
            return thread;
        });
        applyPreferencesToUi();
        bibleSearchTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            setNewSearchText(newValue);
            search();
        });
        bibleSearchTextField.setOnKeyPressed(event -> mainController.globalKeyEventHandler().handle(event));
        searchListView.getSelectionModel().selectedIndexProperty().addListener((observable, oldValue, newValue) -> {
            int index = searchListView.getSelectionModel().getSelectedIndex();
            if (index < 0 || suppressResultNavigation) {
                return;
            }
            navigateToVerse(index);
        });
        initializeFilterControls();
        restoreIncludedBiblesFromPreferences();
        pruneEmptyChapterPicks();
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
        includeLanguageBiblesButton.setOnAction(event -> includeSameLanguageBibles());
        includeAllBiblesButton.setOnAction(event -> includeAllRemainingBibles());
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
        clearFiltersLink.setOnAction(event -> clearFilters());
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
                if (id == null || !savedIds.contains(id)) {
                    continue;
                }
                if (isCurrentSearchBible(bible)) {
                    continue;
                }
                includedBibles.add(bible);
            }
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
        int generation = searchGeneration.incrementAndGet();
        searchExecutor.execute(() -> runSearch(generation));
    }

    private void runSearch(int generation) {
        String querySnapshot = getNewSearchText();
        try {
            TimeUnit.MILLISECONDS.sleep(SEARCH_DEBOUNCE_MS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return;
        }
        if (!isCurrentSearch(generation, querySnapshot)) {
            return;
        }
        boolean withAccents = resolveWithAccents();
        boolean caseSensitive = preferences.isCaseSensitive();
        boolean wholeWord = preferences.isWholeWord();
        BibleSearchFilterSnapshot filters = BibleSearchFilterSnapshot.from(preferences);
        if (querySnapshot.isBlank()) {
            publishResults(generation, List.of(), Map.of(), 0);
            return;
        }

        List<SearchHit> hits = new ArrayList<>();
        Map<Integer, Integer> bookCounts = new HashMap<>();
        List<Bible> biblesToSearch = new ArrayList<>(getBiblesToSearch());
        if (biblesToSearch.isEmpty()) {
            publishResults(generation, List.of(), bookCounts, 0);
            return;
        }
        for (Bible bible : biblesToSearch) {
            if (!isCurrentSearch(generation, querySnapshot)) {
                return;
            }
            collectSearchHits(querySnapshot, bible, withAccents, caseSensitive, wholeWord, filters, hits, bookCounts,
                    generation, querySnapshot);
        }
        if (!isCurrentSearch(generation, querySnapshot)) {
            return;
        }
        publishResults(generation, hits, bookCounts, hits.size());
    }

    private boolean isCurrentSearch(int generation, String querySnapshot) {
        return generation == searchGeneration.get() && querySnapshot.equals(getNewSearchText());
    }

    private void publishResults(int generation, List<SearchHit> hits, Map<Integer, Integer> bookCounts, int resultCount) {
        Platform.runLater(() -> {
            if (generation != searchGeneration.get()) {
                return;
            }
            boolean addAbbreviation = orderedIncludedBibles().size() > 1;
            List<TextFlow> textFlows = new ArrayList<>(hits.size());
            List<Bible> tmpSearchIBible = new ArrayList<>(hits.size());
            List<Integer> tmpSearchIBook = new ArrayList<>(hits.size());
            List<Integer> tmpSearchIPart = new ArrayList<>(hits.size());
            List<Integer> tmpSearchIVerse = new ArrayList<>(hits.size());
            boolean withAccents = resolveWithAccents();
            boolean caseSensitive = preferences.isCaseSensitive();
            boolean wholeWord = preferences.isWholeWord();
            String rawQuery = getNewSearchText();
            for (SearchHit hit : hits) {
                TextFlow textFlow = buildHighlightedResult(hit.getVerseText(), rawQuery, withAccents, caseSensitive, wholeWord);
                if (addAbbreviation) {
                    addBibleAbbreviationForOther(textFlow, hit.getBible());
                }
                Text reference = new Text(hit.getBookLabel() + " " + (hit.getChapterIndex() + 1) + ":" + (hit.getVerseIndex() + 1) + " ");
                setReferenceTextColor(reference);
                textFlow.getChildren().add(0, reference);
                textFlow.setTextAlignment(TextAlignment.JUSTIFY);
                textFlow.setPrefWidth(500.0);
                textFlows.add(textFlow);
                tmpSearchIBible.add(hit.getBible());
                tmpSearchIBook.add(hit.getBookIndex());
                tmpSearchIPart.add(hit.getChapterIndex());
                tmpSearchIVerse.add(hit.getVerseIndex());
            }
            ObservableList<TextFlow> searchListViewItems = searchListView.getItems();
            searchListViewItems.clear();
            searchListViewItems.addAll(textFlows);
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
            updateFilterSummary(resultCount);
        });
    }

    private void collectSearchHits(String rawQuery, Bible bible, boolean withAccents, boolean caseSensitive,
                                   boolean wholeWord, BibleSearchFilterSnapshot filters, List<SearchHit> hits,
                                   Map<Integer, Integer> bookCounts, int generation, String querySnapshot) {
        int results = 0;
        int versesScanned = 0;
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
                    if (++versesScanned % SEARCH_CANCEL_CHECK_INTERVAL == 0 && !isCurrentSearch(generation, querySnapshot)) {
                        return;
                    }
                    BibleVerse bibleVerse = bibleVerses.get(iVerse);
                    if (!BibleSearchMatcher.matchesVerse(bibleVerse, rawQuery, withAccents, caseSensitive, wholeWord)) {
                        continue;
                    }
                    bookCounts.merge(iBook, 1, Integer::sum);
                    if (results >= maxResults) {
                        continue;
                    }
                    String verse = bibleVerse.getText();
                    hits.add(new SearchHit(bible, iBook, iPart, iVerse, book.getShortOrTitle(), verse));
                    ++results;
                }
            }
        }
    }

    private List<Bible> getBiblesToSearch() {
        return orderedIncludedBibles();
    }

    private TextFlow buildHighlightedResult(String verse, String rawQuery, boolean withAccents, boolean caseSensitive,
                                            boolean wholeWord) {
        TextFlow textFlow = new TextFlow();
        if (verse == null || verse.isEmpty() || rawQuery == null || rawQuery.isBlank()) {
            return textFlow;
        }
        List<BibleSearchMatcher.MatchSpan> spans = BibleSearchMatcher.findMatchSpans(
                verse, rawQuery, withAccents, caseSensitive, wholeWord);
        if (spans.isEmpty()) {
            Text text = new Text(verse);
            setGeneralTextColor(text);
            textFlow.getChildren().add(text);
            return textFlow;
        }
        int lastAdded = 0;
        for (BibleSearchMatcher.MatchSpan span : spans) {
            if (lastAdded < span.start()) {
                Text text = new Text(verse.substring(lastAdded, span.start()));
                setGeneralTextColor(text);
                textFlow.getChildren().add(text);
            }
            Text foundText = new Text(verse.substring(span.start(), span.endExclusive()));
            setFoundTextColor(foundText);
            foundText.setFont(Font.font(foundText.getFont().getFamily(), FontWeight.BOLD, foundText.getFont().getSize() + 1));
            addTextWithBackGround(textFlow, foundText);
            lastAdded = span.endExclusive();
        }
        if (lastAdded < verse.length()) {
            Text text = new Text(verse.substring(lastAdded));
            setGeneralTextColor(text);
            textFlow.getChildren().add(text);
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

    private void rebuildScopeUi() {
        if (!initialized || bibleCheckboxesPane == null) {
            return;
        }
        updatingScopeUi = true;
        bibleCheckboxesPane.getChildren().clear();
        if (bibles != null) {
            for (Bible bible : bibles) {
                CheckBox checkBox = new CheckBox(bibleLabel(bible));
                checkBox.setSelected(isBibleInSearchScope(bible));
                checkBox.setUserData(bible);
                checkBox.selectedProperty().addListener((obs, oldValue, selected) -> {
                    if (updatingScopeUi) {
                        return;
                    }
                    Bible target = (Bible) checkBox.getUserData();
                    if (isCurrentSearchBible(target)) {
                        excludeCurrentBibleFromSearch = !selected;
                    } else if (selected) {
                        includedBibles.add(target);
                        persistIncludedBibles();
                    } else {
                        includedBibles.remove(target);
                        persistIncludedBibles();
                    }
                    syncBulkBibleButtons();
                    search();
                });
                bibleCheckboxesPane.getChildren().add(checkBox);
            }
        }
        syncBulkBibleButtons();
        updatingScopeUi = false;
        updateFilterSummary(searchListView.getItems().size());
    }

    private void syncBulkBibleButtons() {
        ResourceBundle bundle = Settings.getInstance().getResourceBundle();
        List<Bible> sameLanguageNotIncluded = getSameLanguageBiblesNotIncluded();
        boolean showLanguageButton = !sameLanguageNotIncluded.isEmpty();
        includeLanguageBiblesButton.setVisible(showLanguageButton);
        includeLanguageBiblesButton.setManaged(showLanguageButton);
        if (showLanguageButton) {
            String language = resolveCurrentLanguageLabel();
            if (language.isBlank()) {
                LOG.warn("Could not resolve language name for bible-search include button");
                includeLanguageBiblesButton.setVisible(false);
                includeLanguageBiblesButton.setManaged(false);
            } else {
                includeLanguageBiblesButton.setText(MessageFormat.format(
                        bundle.getString("Search in language bibles"), language));
            }
        }

        List<Bible> otherLanguageNotIncluded = getOtherLanguageBiblesNotIncluded();
        List<Bible> remainingNotIncluded = getBiblesNotIncluded();
        boolean showAllButton = !otherLanguageNotIncluded.isEmpty();
        includeAllBiblesButton.setVisible(showAllButton);
        includeAllBiblesButton.setManaged(showAllButton);
        if (showAllButton) {
            includeAllBiblesButton.setText(MessageFormat.format(
                    bundle.getString("Include all remaining bibles"), remainingNotIncluded.size()));
        }
    }

    private void includeSameLanguageBibles() {
        for (Bible bible : getSameLanguageBiblesNotIncluded()) {
            if (isCurrentSearchBible(bible)) {
                excludeCurrentBibleFromSearch = false;
            } else {
                includedBibles.add(bible);
            }
        }
        persistIncludedBibles();
        rebuildScopeUi();
        search();
    }

    private void includeAllRemainingBibles() {
        if (bibles == null) {
            return;
        }
        excludeCurrentBibleFromSearch = false;
        for (Bible bible : bibles) {
            if (!isCurrentSearchBible(bible)) {
                includedBibles.add(bible);
            }
        }
        persistIncludedBibles();
        rebuildScopeUi();
        search();
    }

    private List<Bible> getSameLanguageBiblesNotIncluded() {
        List<Bible> result = new ArrayList<>();
        for (Bible bible : getSameLanguageBibles()) {
            if (!isBibleInSearchScope(bible)) {
                result.add(bible);
            }
        }
        return result;
    }

    private List<Bible> getBiblesNotIncluded() {
        List<Bible> result = new ArrayList<>();
        if (bibles == null) {
            return result;
        }
        for (Bible bible : bibles) {
            if (!isBibleInSearchScope(bible)) {
                result.add(bible);
            }
        }
        return result;
    }

    private List<Bible> getOtherLanguageBiblesNotIncluded() {
        List<Bible> result = new ArrayList<>();
        if (bibles == null || currentBible == null || currentBible.getLanguage() == null) {
            return result;
        }
        Language currentLanguage = currentBible.getLanguage();
        for (Bible bible : bibles) {
            if (isBibleInSearchScope(bible)) {
                continue;
            }
            Language language = bible.getLanguage();
            if (language == null || !language.equivalent(currentLanguage)) {
                result.add(bible);
            }
        }
        return result;
    }

    private boolean isCurrentSearchBible(Bible bible) {
        return currentBible != null && currentBible.equivalent(bible);
    }

    private boolean isBibleInSearchScope(Bible bible) {
        if (isCurrentSearchBible(bible)) {
            return !excludeCurrentBibleFromSearch;
        }
        return includedBibles.contains(bible);
    }

    private List<Bible> orderedIncludedBibles() {
        List<Bible> ordered = new ArrayList<>();
        if (currentBible != null && !excludeCurrentBibleFromSearch) {
            ordered.add(currentBible);
        }
        if (bibles != null) {
            for (Bible bible : bibles) {
                if (includedBibles.contains(bible) && !ordered.contains(bible)) {
                    ordered.add(bible);
                }
            }
        } else {
            for (Bible bible : includedBibles) {
                if (!ordered.contains(bible)) {
                    ordered.add(bible);
                }
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

    private String resolveCurrentLanguageLabel() {
        if (currentBible == null || currentBible.getLanguage() == null) {
            return "";
        }
        String label = languageLabel(resolveLanguageRecord(currentBible.getLanguage()));
        if (!label.isBlank()) {
            return label;
        }
        Language songLanguage = Settings.getInstance().getSongSelectedLanguage();
        if (songLanguage != null && songLanguage.equivalent(currentBible.getLanguage())) {
            return languageLabel(resolveLanguageRecord(songLanguage));
        }
        return "";
    }

    private static String languageLabel(Language language) {
        Language resolved = resolveLanguageRecord(language);
        if (resolved == null) {
            return "";
        }
        Locale locale = Settings.getInstance().getPreferredLanguage();
        boolean preferNative = "hu".equalsIgnoreCase(locale.getLanguage())
                || "ro".equalsIgnoreCase(locale.getLanguage());
        String primary = preferNative ? resolved.getNativeName() : resolved.getEnglishName();
        String fallback = preferNative ? resolved.getEnglishName() : resolved.getNativeName();
        if (primary != null && !primary.isBlank()) {
            return primary;
        }
        return fallback != null ? fallback : "";
    }

    private static Language resolveLanguageRecord(Language language) {
        if (language == null) {
            return null;
        }
        if (hasLanguageName(language)) {
            return language;
        }
        var languageService = ServiceManager.getLanguageService();
        String uuid = language.getUuid();
        if (uuid != null && !uuid.isBlank()) {
            Language loaded = languageService.findByUuid(uuid);
            if (loaded != null && hasLanguageName(loaded)) {
                return loaded;
            }
        }
        Long id = language.getId();
        if (id != null) {
            Language loaded = languageService.findById(id);
            if (loaded != null && hasLanguageName(loaded)) {
                return loaded;
            }
        }
        for (Language candidate : languageService.findAll()) {
            if (candidate.equivalent(language) && hasLanguageName(candidate)) {
                return candidate;
            }
        }
        return language;
    }

    private static boolean hasLanguageName(Language language) {
        String nativeName = language.getNativeName();
        if (nativeName != null && !nativeName.isBlank()) {
            return true;
        }
        String englishName = language.getEnglishName();
        return englishName != null && !englishName.isBlank();
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
                if (updatingTestamentUi) {
                    return;
                }
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
        updatingTestamentUi = true;
        try {
            int from = oldTestament ? 0 : STANDARD_OLD_TESTAMENT_BOOKS;
            int to = oldTestament ? Math.min(STANDARD_OLD_TESTAMENT_BOOKS, bookFilterEntries.size()) : bookFilterEntries.size();
            for (int i = from; i < to; ++i) {
                bookFilterEntries.get(i).selectedProperty().set(selected);
            }
            persistExcludedBooks();
            syncTestamentCheckboxes();
            search();
        } finally {
            updatingTestamentUi = false;
        }
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
        Set<Integer> selectedChapters = preferences.getChaptersByBook().get(selectedBookForChapters);
        for (int chapter = 1; chapter <= chapterCount; ++chapter) {
            CheckBox checkBox = new CheckBox(String.valueOf(chapter));
            checkBox.setSelected(selectedChapters != null && selectedChapters.contains(chapter));
            int chapterNumber = chapter;
            checkBox.selectedProperty().addListener((obs, oldValue, selected) -> {
                Set<Integer> chapters = preferences.getChaptersByBook()
                        .computeIfAbsent(selectedBookForChapters, key -> new HashSet<>());
                if (selected) {
                    chapters.add(chapterNumber);
                } else {
                    chapters.remove(chapterNumber);
                    if (chapters.isEmpty()) {
                        preferences.getChaptersByBook().remove(selectedBookForChapters);
                    }
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
                suppressResultNavigation = true;
                try {
                    searchListView.getSelectionModel().select(i);
                    searchListView.scrollTo(i);
                } finally {
                    suppressResultNavigation = false;
                }
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
        int bibleCount = orderedIncludedBibles().size();
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
        int checkedChapterCount = countCheckedChapterPicks();
        if (checkedChapterCount > 0) {
            parts.add(MessageFormat.format(bundle.getString("Bible search summary chapter picks"), checkedChapterCount));
        }
        if (!getNewSearchText().isBlank()) {
            parts.add(MessageFormat.format(bundle.getString("Bible search summary results"), resultCount));
        }
        String summary = parts.isEmpty() ? "" : String.join(" · ", parts);
        boolean showClear = hasFiltersToClear();
        if (Platform.isFxApplicationThread()) {
            applyFilterSummaryUi(summary, showClear);
        } else {
            Platform.runLater(() -> applyFilterSummaryUi(summary, showClear));
        }
    }

    private void applyFilterSummaryUi(String summary, boolean showClear) {
        filterSummaryLabel.setText(summary);
        filterSummaryLabel.setManaged(!summary.isBlank());
        filterSummaryLabel.setVisible(!summary.isBlank());
        clearFiltersLink.setVisible(showClear);
        clearFiltersLink.setManaged(showClear);
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
        if (countCheckedChapterPicks() > 0) {
            return true;
        }
        if (hasNonDefaultBibleScope()) {
            return true;
        }
        return false;
    }

    private boolean hasNonDefaultBibleScope() {
        return !includedBibles.isEmpty() || excludeCurrentBibleFromSearch;
    }

    private void clearFilters() {
        applyFilterReset(preferences::clearFilterValues, false);
    }

    private void restoreDefaults() {
        applyFilterReset(preferences::resetToDefaults, true);
    }

    private void applyFilterReset(Runnable preferenceReset, boolean collapseOptionPanels) {
        preferenceReset.run();
        pruneEmptyChapterPicks();
        includedBibles.clear();
        excludeCurrentBibleFromSearch = false;
        persistIncludedBibles();
        caseSensitiveCheckBox.setSelected(false);
        wholeWordCheckBox.setSelected(false);
        accentsCheckBox.setSelected(Settings.getInstance().isWithAccents());
        chapterFromField.clear();
        chapterToField.clear();
        if (collapseOptionPanels) {
            optionsPane.setExpanded(false);
            rangePane.setExpanded(false);
        }
        rebuildBookFilterList();
        rebuildScopeUi();
        savePreferences();
        search();
        updateFilterSummary(searchListView.getItems().size());
    }

    private int countCheckedChapterPicks() {
        int count = 0;
        for (Set<Integer> chapters : preferences.getChaptersByBook().values()) {
            if (chapters != null) {
                count += chapters.size();
            }
        }
        return count;
    }

    private void pruneEmptyChapterPicks() {
        preferences.getChaptersByBook().entrySet().removeIf(entry -> entry.getValue() == null || entry.getValue().isEmpty());
    }

    private void persistIncludedBibles() {
        Set<Long> ids = new LinkedHashSet<>();
        for (Bible bible : includedBibles) {
            if (isCurrentSearchBible(bible)) {
                continue;
            }
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
            excludeCurrentBibleFromSearch = false;
            rebuildBookFilterList();
            rebuildScopeUi();
            syncBulkBibleButtons();
            search();
        }
    }

    public void setBibles(List<Bible> bibles) {
        this.bibles = bibles;
        if (initialized) {
            includedBibles.retainAll(bibles != null ? bibles : Set.of());
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
                if (isEmpty() || getItem() == null) {
                    return;
                }
                if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2) {
                    navigateToBookResults(getItem().getBookIndex());
                    event.consume();
                    return;
                }
                if (event.getClickCount() == 1 && getListView() != null) {
                    getListView().getSelectionModel().select(getItem());
                    event.consume();
                }
            });
            countLabel.setOnMouseClicked(event -> {
                if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2 && !isEmpty() && getItem() != null) {
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
