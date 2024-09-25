package projector.controller;

import com.bence.projector.common.dto.ProjectionDTO;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Orientation;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.MultipleSelectionModel;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.text.TextFlow;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import projector.MainDesktop;
import projector.api.assembler.ProjectionAssembler;
import projector.application.ProjectionType;
import projector.application.Reader;
import projector.application.Settings;
import projector.controller.eventHandler.NextButtonEventHandler;
import projector.controller.util.ProjectionData;
import projector.model.Bible;
import projector.model.BibleVerse;
import projector.model.Book;
import projector.model.Chapter;
import projector.model.Reference;
import projector.model.ReferenceBook;
import projector.model.ReferenceChapter;
import projector.model.VerseIndex;
import projector.service.BibleService;
import projector.service.ServiceManager;
import projector.service.VerseIndexService;
import projector.utils.BibleVerseTextFlow;
import projector.utils.StringUtils;
import projector.utils.Triplet;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.net.URL;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Vector;
import java.util.concurrent.TimeUnit;

import static projector.controller.song.SongController.getKeyEventEventHandler;
import static projector.utils.ColorUtil.getGeneralTextColor;
import static projector.utils.ColorUtil.getReferenceTextColor;
import static projector.utils.ContextMenuUtil.getDeleteMenuItem;
import static projector.utils.ContextMenuUtil.initializeContextMenu;
import static projector.utils.KeyEventUtil.getTextFromEvent;
import static projector.utils.SceneUtils.getAStage;
import static projector.utils.SceneUtils.getCustomStage2;

@SuppressWarnings("ResultOfMethodCallIgnored")
public class BibleController {

    private static final Logger LOG = LoggerFactory.getLogger(BibleController.class);
    private static final int verseRightMargin = 37;
    private final List<Bible> parallelBibles = new ArrayList<>();
    private final Settings settings = Settings.getInstance();
    private MyController mainController;
    private ProjectionScreenController projectionScreenController;
    private BibleSearchController bibleSearchController;
    private RecentController recentController;
    private HistoryController historyController;
    @FXML
    private ListView<String> bookListView;
    @FXML
    private ListView<Integer> partListView;
    @FXML
    private Label partLabel;
    @FXML
    private ListView<BibleVerseTextFlow> verseListView;
    @FXML
    private TextField bookTextField;
    @FXML
    private TextField partTextField;
    @FXML
    private TextField verseTextField;
    @FXML
    private ListView<Bible> bibleListView;
    @FXML
    private TextArea referenceTextArea;
    @FXML
    private Button sendProjectionScreenText;
    @FXML
    private Button referenceResetText;
    @FXML
    private Button referenceEditText;
    @FXML
    private ListView<String> referenceListView;
    @FXML
    private SplitPane horizontalSplitPane;
    @FXML
    private SplitPane verticalSplitPane;
    @FXML
    private TextField searchTextField;
    @FXML
    private Label foundLabel;
    @FXML
    private Button decreaseButton;
    @FXML
    private Button increaseButton;
    @FXML
    private Button nextButton;
    @FXML
    private ToggleButton abbreviationToggleButton;
    private Bible bible;
    private List<Integer> searchIBook;
    private Integer searchSelected = 0;
    private boolean isAllBooks;
    private boolean isSelecting;
    private boolean firstDrag = false;
    private int partListBookI = -1;
    private int selectedBook = -1;
    private int selectedPart = -1;
    private int selectedVerse = -1;
    private boolean isLastVerse = false;
    private Reference allReference;
    private Reference ref;
    private List<Reference> references;
    private boolean oldReplace = false;
    private boolean newReferenceAdded = false;
    private Font verseFont;
    private Date lastUpdateSelected;
    private boolean initialized = false;
    private boolean wasSelectionChange;
    private boolean splitDividersInitialized = false;
    private final List<BibleVerseTextFlow> foundedBibleVerseTextFlows = new ArrayList<>();
    private int currentIterationFoundBibleVerseTextFlowIndex = 0;

    private static String strip(String s) {
        try {
            s = stripAccents(s).replaceAll("[^a-zA-Z0-9]", "").toLowerCase(Locale.US).trim();
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
        return s;
    }

    private static String stripAccents(String s) {
        try {
            s = Normalizer.normalize(s, Normalizer.Form.NFD);
            //noinspection RegExpSimplifiable
            s = s.replaceAll("[\\p{InCombiningDiacriticalMarks}]", "");
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
        return s;
    }

    private static void oldReplaceScrollTo(int tmp, @SuppressWarnings("rawtypes") ListView listView) {
        try {
            if (tmp > listView.getItems().size()) {
                listView.scrollTo(listView.getItems().size() - 2);
                listView.getFocusModel().focus(listView.getItems().size() - 1);
            } else {
                listView.scrollTo(0);
                listView.getFocusModel().focus(0);
            }
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
    }

    private static void resetTextFieldValue(String oldValue, TextField partTextField) {
        try {
            if (!oldValue.trim().isEmpty()) {
                try {
                    Integer.parseInt(oldValue.trim());
                    partTextField.setText(oldValue.trim());
                } catch (NumberFormatException ne) {
                    partTextField.setText("");
                }
            } else {
                partTextField.setText("");
            }
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
    }

    public static void setSceneStyleFile(Scene scene) {
        try {
            Settings.getInstance().getBibleController().setStyleFile(scene);
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
    }

    private static Integer getFirstFromSelectedIndices(ObservableList<Integer> ob) {
        if (ob == null || ob.isEmpty()) {
            return -1;
        }
        return ob.get(0);
    }

    public static List<BibleVerse> getVersesByIndices(List<VerseIndex> verseIndices, Bible bible, int selectedBook, int selectedPart, List<Integer> ob) {
        ServiceManager.getBibleService().checkHasVerseIndices(bible);
        if (verseIndices != null && !verseIndices.isEmpty() && bible.hasVerseIndices()) {
            return getVersesByIndices_(verseIndices, bible);
        }
        List<BibleVerse> verses = new ArrayList<>();
        Book book = bible.getBook(selectedBook);
        if (book != null) {
            Chapter chapter = book.getChapter(selectedPart);
            if (chapter != null && ob != null) {
                for (int i : ob) {
                    BibleVerse bibleVerse = chapter.getVerse(i);
                    if (bibleVerse != null) {
                        verses.add(bibleVerse);
                    }
                }
            }
        }
        return verses;
    }

    private static List<BibleVerse> getVersesByIndices_(List<VerseIndex> verseIndices, Bible bible) {
        VerseIndexService verseIndexService = ServiceManager.getVerseIndexService();
        List<BibleVerse> verses = new ArrayList<>();
        List<Long> uniqueIndices = new ArrayList<>(verseIndices.size());
        for (VerseIndex verseIndex : verseIndices) {
            Long indexNumber = verseIndex.getIndexNumber();
            if (!uniqueIndices.contains(indexNumber)) {
                uniqueIndices.add(indexNumber);
            }
        }
        for (Long index : uniqueIndices) {
            List<BibleVerse> indices = verseIndexService.findByIndexAndBibleId(index, bible.getId());
            for (BibleVerse bibleVerse : indices) {
                if (!verses.contains(bibleVerse)) {
                    verses.add(bibleVerse);
                }
            }
        }
        return verses;
    }

    public static String getBibleVerseWithReferenceText(List<VerseIndex> verseIndices, Bible parallelBible, int selectedBook, int selectedPart, List<Integer> ob) {
        StringBuilder string = new StringBuilder();
        List<BibleVerse> verses = getVersesByIndices(verseIndices, parallelBible, selectedBook, selectedPart, ob);
        String s = getVersesAndReference(parallelBible, verses);
        if (!s.trim().equals("[]") && !s.trim().isEmpty()) {
            Color color = parallelBible.getColor();
            if (color != null) {
                string.append("<color=\"").append(color).append("\">");
            }
            string.append(s);
            if (color != null) {
                string.append("</color>");
            }
        }
        return string.toString();
    }

    private static String getVersesAndReference(Bible bible, List<BibleVerse> bibleVerses) {
        Reference reference = new Reference();
        reference.setBible(bible);
        StringBuilder result = new StringBuilder();
        for (BibleVerse bibleVerse : bibleVerses) {
            if (!result.isEmpty()) {
                result.append("\n");
            }
            if (bibleVerses.size() > 1) {
                result.append(bibleVerse.getNumber()).append(". ");
            }
            result.append(bibleVerse.getText());
            reference.addVerse(bibleVerse.getChapter().getBook(), bibleVerse.getChapter().getNumber(), bibleVerse.getNumber());
        }
        String versesText = result.toString();
        if (versesText.isEmpty()) {
            return "";
        }
        result.append("\n");
        Settings settings = Settings.getInstance();
        if (settings.isReferenceItalic()) {
            result.append("[");
        }
        result.append(reference.getReference());
        if (settings.getBibleShortName() && bible.isShowAbbreviation()) {
            result.append(" (").append(bible.getShortName()).append(")");
        }
        if (settings.isReferenceItalic()) {
            result.append("]");
        }
        return result.toString();
    }

    public static void setReferenceTextColor(Text reference) {
        reference.setFill(getReferenceTextColor());
    }

    public static void setGeneralTextColor(Text text) {
        text.setFill(getGeneralTextColor());
    }

    public static void setFoundTextColor(Text foundText) {
        setGeneralTextColor(foundText);
    }

    public static void addTextWithBackGround(TextFlow textFlow, Text foundText) {
        TextFlow foundTextFlow = new TextFlow(foundText);
        foundTextFlow.getStyleClass().add("found");
        textFlow.getChildren().add(foundTextFlow);
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    void lazyInitialize() {
        if (initialized) {
            return;
        }
        initialized = true;
        try {
            // double x = System.currentTimeMillis();
            // System.out.println("Ido4: " + (System.currentTimeMillis() - x));
            // double x = System.currentTimeMillis();
            bibleListView.orientationProperty().set(Orientation.HORIZONTAL);
            final MultipleSelectionModel<BibleVerseTextFlow> verseListViewSelectionModel = verseListView.getSelectionModel();
            bibleListView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
                try {
                    if (newValue == null) {
                        return;
                    }
                    if (oldValue != null) {
                        parallelBibles.add(oldValue);
                    }
                    parallelBibles.remove(newValue);
                    sortParallelBibles();
                    int bookI = selectedBook;// bookListView.getSelectionModel().getSelectedIndex();
                    int partI = selectedPart;// partListView.getSelectionModel().getSelectedIndex();
                    ObservableList<Integer> obVerseI = verseListViewSelectionModel.getSelectedIndices();
                    Vector<Integer> verseI = new Vector<>(obVerseI.size());
                    verseI.addAll(obVerseI);
                    Reader.setBooksRead(false);
                    bible = newValue;
                    setAbbreviationButtonVisibility();
                    addAllBooks();
                    bibleSearchController.setBible(bible);
                    historyController.setBible(bible);

                    if (bookI > bible.getBooks().size() - 1) {
                        bookI = bible.getBooks().size() - 1;
                    }
                    if (bookI >= 0) {
                        bookListView.getSelectionModel().select(bookI);
                        bookListView.scrollTo(bookI);
                        if (partI > bible.getBooks().get(bookI).getChapters().size() - 1) {
                            partI = bible.getBooks().get(bookI).getChapters().size() - 1;
                        }
                        if (partI >= 0) {
                            partListView.getSelectionModel().select(partI);
                            partListView.scrollTo(partI);
                            if (!verseI.isEmpty()) {
                                setSelecting(true);
                                try {
                                    for (Integer aVerseI : verseI) {
                                        if (aVerseI > bible.getBooks().get(bookI).getChapters().get(partI).getVerses().size() - 1) {
                                            verseListViewSelectionModel.select(bible.getBooks().get(bookI).getChapters().get(partI).getVerses().size() - 1);
                                            break;
                                        } else {
                                            verseListViewSelectionModel.select(aVerseI);
                                        }
                                    }
                                    verseListView.scrollTo(verseI.get(0));
                                } finally {
                                    setSelecting(false);
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    LOG.error(e.getMessage(), e);
                }
            });
            bibleListView.setCellFactory(new Callback<>() {
                @Override
                public ListCell<Bible> call(ListView<Bible> bibleListView) {

                    return new ListCell<>() {
                        @Override
                        protected void updateItem(Bible bible, boolean empty) {
                            try {
                                super.updateItem(bible, empty);
                                if (bible != null && !empty) {
                                    setText(bible.getShortName());
                                    setUnderline(bible.getParallelNumber() != 0);
                                } else {
                                    setText(null);
                                }
                            } catch (Exception e) {
                                LOG.error(e.getMessage(), e);
                            }
                        }
                    };
                }
            });
            setBibleListViewOnMouseClicked();
            verseListViewSelectionModel.setSelectionMode(SelectionMode.MULTIPLE);
            bookTextField.textProperty().addListener((observable, oldValue, newValue) -> titleSearch(newValue));
            bookTextField.setOnKeyPressed(event -> {
                try {
                    if (event.getCode() == KeyCode.RIGHT) {
                        if (event.isAltDown() || bookTextField.getCaretPosition() == bookTextField.getText().length()) {
                            partTextField.requestFocus();
                        }
                    } else if (event.getCode() == KeyCode.ENTER || event.getCode() == KeyCode.SPACE) {
                        if (!bookListView.getItems().isEmpty()) {
                            bookListView.getSelectionModel().select(0);
                            if (!partTextField.getText().isEmpty()) {
                                oldReplace = true;
                                partTextField.setText("");
                            }
                            partTextField.requestFocus();
                        }
                    } else if (event.getCode() == KeyCode.DOWN) {
                        bookListView.requestFocus();
                    } else if (event.getCode() == KeyCode.F1) {
                        mainController.setBlank();
                    }
                } catch (Exception e) {
                    LOG.error(e.getMessage(), e);
                }
            });
            partTextField.textProperty().addListener((observable, oldValue, newValue) -> {
                try {
                    if (oldReplace) {
                        oldReplace = false;
                        partTextField.setText(partTextField.getText().trim());
                        return;
                    }
                    if (!newValue.isEmpty()) {
                        try {
                            int tmp = Integer.parseInt(newValue);
                            if (tmp <= partListView.getItems().size() && tmp > 0) {
                                partListView.scrollTo(tmp - 2);
                                partListView.getSelectionModel().select(tmp - 1);
                                if (tmp * 10 > partListView.getItems().size()) {
                                    // oldReplace = true;
                                    // verseTextField.setText("1");
                                    if (!verseTextField.getText().isEmpty()) {
                                        oldReplace = true;
                                        verseTextField.setText("");
                                    }
                                    verseTextField.requestFocus();
                                }
                            } else {
                                oldReplace = true;
                                if (!oldValue.isEmpty()) {
                                    partTextField.setText(oldValue.trim());
                                } else {
                                    partTextField.setText("");
                                }
                                oldReplaceScrollTo(tmp, partListView);
                            }
                        } catch (NumberFormatException e) {
                            oldReplace = true;
                            resetTextFieldValue(oldValue, partTextField);
                        }
                    }
                } catch (Exception e) {
                    LOG.error(e.getMessage(), e);
                }
            });
            partTextField.setOnKeyPressed(event -> {
                try {
                    if (event.getCode() == KeyCode.RIGHT) {
                        if (event.isAltDown() || partTextField.getCaretPosition() == partTextField.getText().length()) {
                            verseTextField.requestFocus();
                        }
                    } else if (event.getCode() == KeyCode.LEFT) {
                        if (event.isAltDown() || partTextField.getCaretPosition() == 0) {
                            bookTextField.requestFocus();
                        }
                    } else if (!partTextField.getText().trim().isEmpty() && (event.getCode() == KeyCode.SPACE || event.getCode() == KeyCode.ENTER || event.getCode() == KeyCode.PERIOD || event.getCode() == KeyCode.DECIMAL)) {
                        try {
                            int tmp = Integer.parseInt(partTextField.getText());
                            if (tmp <= partListView.getItems().size() && tmp > 0) {
                                partListView.scrollTo(tmp - 2);
                                partListView.getSelectionModel().select(tmp - 1);
                                if (!verseTextField.getText().isEmpty()) {
                                    oldReplace = true;
                                    verseTextField.setText("");
                                }
                                verseTextField.requestFocus();
                            }
                        } catch (NumberFormatException ignored) {
                        }
                    } else if (event.getCode() == KeyCode.DOWN) {
                        // &&
                        // event.isAltDown())
                        // {
                        partListView.requestFocus();
                    } else if (event.getCode() == KeyCode.F1) {
                        mainController.setBlank();
                    }
                } catch (Exception e) {
                    LOG.error(e.getMessage(), e);
                }
            });
            partTextField.addEventFilter(KeyEvent.KEY_TYPED, numeric_Validation());
            verseTextField.addEventFilter(KeyEvent.KEY_TYPED, numeric_Validation());
            verseTextField.textProperty().addListener((observable, oldValue, newValue) -> {
                try {
                    if (oldReplace) {
                        oldReplace = false;
                        return;
                    }
                    if (!newValue.isEmpty()) {
                        try {
                            int tmp = Integer.parseInt(newValue);
                            if (tmp <= verseListView.getItems().size() && tmp > 0) {
                                verseListView.scrollTo(tmp - 2);
                                verseListView.getFocusModel().focus(tmp - 1);
                                if (tmp * 10 > verseListView.getItems().size()) {
                                    verseListViewSelectionModel.clearSelection();
                                    if (settings.isFastMode()) {
                                        verseListViewSelectionModel.select(tmp - 1);
                                    } else {
                                        verseListView.getFocusModel().focus(tmp - 1);
                                    }
                                    verseListView.requestFocus();
                                }
                            } else {
                                oldReplace = true;
                                if (!oldValue.isEmpty()) {
                                    verseTextField.setText(oldValue.trim());
                                } else {
                                    verseTextField.setText("");
                                }
                                oldReplaceScrollTo(tmp, verseListView);
                            }
                        } catch (NumberFormatException e) {
                            oldReplace = true;
                            resetTextFieldValue(oldValue, verseTextField);
                        }
                    }
                } catch (Exception e) {
                    LOG.error(e.getMessage(), e);
                }
            });
            verseTextField.setOnKeyPressed(event -> {
                try {
                    if (event.getCode() == KeyCode.ENTER) {
                        try {
                            int tmp = Integer.parseInt(verseTextField.getText());
                            verseListView.scrollTo(tmp - 2);
                            if (settings.isFastMode()) {
                                verseListViewSelectionModel.clearAndSelect(tmp - 1);
                            } else {
                                verseListView.getFocusModel().focus(tmp - 1);
                            }
                            verseListView.requestFocus();
                        } catch (NumberFormatException ignored) {
                        }
                    } else if (event.getCode() == KeyCode.LEFT) {
                        if (event.isAltDown() || verseTextField.getCaretPosition() == 0) {
                            partTextField.requestFocus();
                        }
                    } else if (event.getCode() == KeyCode.DOWN) {
                        // &&event.isAltDown()) {
                        verseListView.requestFocus();
                    } else if (event.getCode() == KeyCode.F1) {
                        mainController.setBlank();
                    }
                } catch (Exception e) {
                    LOG.error(e.getMessage(), e);
                }
            });
            bookListView.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<>() {
                @Override
                public synchronized void changed(ObservableValue<? extends String> selected, String oldBook, String newBook) {
                    try {
                        if (!bookListView.getSelectionModel().isEmpty() && bible != null) {
                            partListBookI = searchIBook.get(bookListView.getSelectionModel().getSelectedIndex());
                            partListView.getItems().clear();
                            for (int i = 0; i < bible.getBooks().get(partListBookI).getChapters().size(); ++i) {
                                partListView.getItems().add(i + 1);
                            }
                            partListView.getFocusModel().focus(0);
                            searchSelected = 2;
                            bibleSearchController.setSearchSelected(searchSelected);
                            recentController.setSearchSelected(searchSelected);
                        }
                    } catch (Exception e) {
                        LOG.error(e.getMessage(), e);
                    }
                }
            });
            bookListView.setOnKeyPressed(event -> {
                try {
                    final KeyCode keyCode = event.getCode();
                    if (keyCode == KeyCode.UP && event.isAltDown()) {
                        bookTextField.requestFocus();
                    } else if (keyCode == KeyCode.RIGHT) {
                        partListView.requestFocus();
                    } else if (keyCode.isDigitKey() || keyCode.isKeypadKey() || keyCode.isLetterKey()) {
                        bookTextField.setText(getTextFromEvent(event));
                        bookTextField.requestFocus();
                        event.consume();
                    }
                } catch (Exception e) {
                    LOG.error(e.getMessage(), e);
                }
            });
            partListView.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<>() {
                @Override
                public synchronized void changed(ObservableValue<? extends Integer> observable, Integer oldPart, Integer newPart) {
                    try {
                        final int selectedPartIndex = partListView.getSelectionModel().getSelectedIndex();
                        if (selectedPartIndex >= 0 && bible != null) {
                            Book book = bible.getBooks().get(partListBookI);
                            partLabel.setText(book.getShortOrTitle().trim() + " " + (selectedPartIndex + 1));
                            selectedBook = partListBookI;
                            selectedPart = selectedPartIndex;
                            addAllVerse();
                            if (isLastVerse) {
                                isLastVerse = false;
                                selectedVerse = verseListView.getItems().size() - 1;
                                verseListViewSelectionModel.select(selectedVerse);
                                verseListView.scrollTo(selectedVerse);
                            }
                            searchSelected = 3;
                            bibleSearchController.setSearchSelected(searchSelected);
                            recentController.setSearchSelected(searchSelected);
                        }
                    } catch (Exception e) {
                        LOG.error(e.getMessage(), e);
                    }
                }
            });
            partListView.setOnKeyPressed(event -> {
                try {
                    final KeyCode keyCode = event.getCode();
                    if (keyCode == KeyCode.UP && event.isAltDown()) {
                        partTextField.requestFocus();
                    } else if (keyCode == KeyCode.LEFT) {
                        bookListView.requestFocus();
                    } else if (keyCode == KeyCode.RIGHT) {
                        verseListView.requestFocus();
                    } else {
                        if (keyCode.isDigitKey() || keyCode.isKeypadKey()) {
                            partTextField.setText(getTextFromEvent(event));
                            partTextField.requestFocus();
                            event.consume();
                        }
                    }
                } catch (Exception e) {
                    LOG.error(e.getMessage(), e);
                }
            });
            verseListViewSelectionModel.selectedIndexProperty().addListener((observable, oldValue, newValue) -> {
                try {
                    wasSelectionChange = true;
                    selectedVerse = newValue.intValue();
                } catch (Exception e) {
                    LOG.error(e.getMessage(), e);
                }
            });
            verseListView.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
                try {
                    final KeyCode keyCode = event.getCode();
                    if (keyCode == KeyCode.DOWN) {
                        if (setNextPart()) {
                            event.consume();
                        }
                    } else if (keyCode == KeyCode.UP) {
                        if (setPreviousPart()) {
                            event.consume();
                        }
                    } else if (keyCode == KeyCode.PAGE_DOWN) {
                        setNextVerse();
                        event.consume();
                    } else if (keyCode == KeyCode.PAGE_UP) {
                        setPreviousVerse();
                        event.consume();
                    }
                } catch (Exception e) {
                    LOG.error(e.getMessage(), e);
                }
            });
            verseListView.setOnKeyPressed(event -> {
                try {
                    final KeyCode keyCode = event.getCode();
                    if (keyCode == KeyCode.ENTER) {
                        if (settings.isFastMode()) {
                            projectionScreenController.setBlank(false);
                            mainController.getBlankButton().setSelected(false);
                        }
                    } else if (keyCode == KeyCode.UP && event.isAltDown()) {
                        verseTextField.requestFocus();
                    } else if (keyCode == KeyCode.LEFT) {
                        partListView.requestFocus();
                    } else if (keyCode == KeyCode.ESCAPE) {
                        verseListViewSelectionModel.clearSelection();
                    } else if (keyCode.equals(KeyCode.C) && event.isControlDown()) {
                        if (selectedBook >= 0 && selectedPart >= 0 && selectedVerse >= 0) {
                            ObservableList<Integer> ob = verseListViewSelectionModel.getSelectedIndices();
                            List<BibleVerse> bibleVerses = new ArrayList<>(ob.size());
                            for (int i : ob) {
                                BibleVerse bibleVerse = bible.getBooks().get(selectedBook).getChapters().get(selectedPart).getVerses().get(i);
                                bibleVerses.add(bibleVerse);
                            }
                            StringSelection stringSelection = new StringSelection(getVersesAndReference(bible, bibleVerses));
                            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                            clipboard.setContents(stringSelection, null);
                        }
                    } else if ((keyCode.equals(KeyCode.ADD) || keyCode.equals(KeyCode.EQUALS)) && event.isControlDown()) {
                        enlargeVerseListViewText();
                    } else if ((keyCode.equals(KeyCode.MINUS) || keyCode.equals(KeyCode.SUBTRACT)) && event.isControlDown()) {
                        shrinkVerseListViewText();
                    } else {
                        String text = getTextFromEvent(event);
                        if (keyCode.isDigitKey() || keyCode.isKeypadKey()) {
                            try {
                                verseTextField.setText(text);
                            } catch (Exception e) {
                                LOG.error(e.getMessage(), e);
                                LOG.error("Charachter: {}", text);
                            }
                            verseTextField.requestFocus();
                            event.consume();
                        } else if (keyCode.isLetterKey() && !event.isControlDown()) {
                            searchTextField.setText(text);
                            searchTextField.requestFocus();
                            event.consume();
                        }
                    }
                } catch (Exception e) {
                    LOG.error(e.getMessage(), e);
                }
            });
            verseListView.setCellFactory((ListView<BibleVerseTextFlow> list) -> {
                final ListCell<BibleVerseTextFlow> cell = new ListCell<>() {
                    @Override
                    protected void updateItem(BibleVerseTextFlow textFlow, boolean empty) {
                        try {
                            super.updateItem(textFlow, empty);
                            setText(null);
                            if (empty || textFlow == null) {
                                setGraphic(null);
                            } else {
                                setGraphic(textFlow);
                            }
                        } catch (Exception e) {
                            LOG.error(e.getMessage(), e);
                        }
                    }

                    @Override
                    public void updateSelected(boolean selected) {
                        super.updateSelected(selected);
                        lastUpdateSelected = new Date();
                    }
                };
                cell.setOnMousePressed(event -> {
                    if (event.isControlDown() && cell.isSelected() && (lastUpdateSelected == null || new Date().getTime() - lastUpdateSelected.getTime() > 1000)) {
                        verseListViewSelectionModel.clearSelection(cell.getIndex());
                        event.consume();
                    }
                });
                cell.setOnMouseDragEntered(t -> {
                    try {
                        if (isSelecting() && !firstDrag) {
                            setSelection(cell);
                        }
                        firstDrag = false;
                    } catch (Exception e) {
                        LOG.error(e.getMessage(), e);
                    }
                });
                cell.setOnDragDetected(t -> firstDrag = true);
                return cell;
            });
            bookTextField.requestFocus();
            sendProjectionScreenText.setOnAction(event -> {
                try {
                    projectionScreenController.setText(referenceTextArea.getText(), ProjectionType.REFERENCE, null);
                } catch (Exception e) {
                    LOG.error(e.getMessage(), e);
                }
            });
            verseListView.setOnMousePressed(event -> {
                if (event.getClickCount() == 2) {
                    verseSelected();
                }
            });
            referenceResetText.setOnAction(event -> {
                try {
                    ref.clear();
                    referenceTextArea.setText("");
                } catch (Exception e) {
                    LOG.error(e.getMessage(), e);
                }
            });
            referenceEditText.setOnAction(event -> {
                try {
                    Stage dialog = getAStage(getClass()); // new stage
                    dialog.initModality(Modality.APPLICATION_MODAL);
                    // Defines a modal window that blocks events from being
                    // delivered to any other application window.
                    // dialog.initOwner(primaryStage);
                    VBox vb = new VBox(15);
                    List<CheckBox> list = new LinkedList<>();
                    for (ReferenceBook rb : ref.getBookList()) {
                        for (ReferenceChapter ch : rb.getChapters()) {
                            for (int i : ch.getVerses()) {
                                Book book = bible.getBooks().get(rb.getBookNumber());
                                CheckBox tmp = new CheckBox(book.getShortOrTitle() + " " + ch.getChapterNumber() + ":" + i);
                                tmp.selectedProperty().set(true);
                                list.add(tmp);
                            }
                        }
                    }
                    vb.getChildren().addAll(list);
                    Group root = new Group();
                    int height = list.size() * 35;
                    if (height > 700) {
                        height = 700;
                    }
                    Scene dialogScene = new Scene(root, 300, height);
                    dialog.setScene(dialogScene);
                    ScrollPane scrollPane = new ScrollPane();
                    dialogScene.heightProperty().addListener((observable, oldValue, newValue) -> {
                        try {
                            double delta = newValue.doubleValue() - oldValue.doubleValue();
                            scrollPane.setPrefHeight(scrollPane.getPrefHeight() + delta);
                        } catch (Exception e) {
                            LOG.error(e.getMessage(), e);
                        }
                    });
                    dialogScene.widthProperty().addListener((observable, oldValue, newValue) -> {
                        try {
                            double delta = newValue.doubleValue() - oldValue.doubleValue();
                            scrollPane.setPrefWidth(scrollPane.getPrefWidth() + delta);
                        } catch (Exception e) {
                            LOG.error(e.getMessage(), e);
                        }
                    });
                    scrollPane.setPrefSize(300, height);
                    scrollPane.setFitToWidth(true);
                    scrollPane.setContent(vb);
                    root.getChildren().add(scrollPane);
                    double first = 0.9999789;
                    scrollPane.vvalueProperty().addListener((observable, oldValue, newValue) -> {
                        try {
                            if (oldValue.equals(first) && newValue.equals(0.0)) {
                                scrollPane.setVvalue(1.0);
                            }
                        } catch (Exception e) {
                            LOG.error(e.getMessage(), e);
                        }
                    });
                    scrollPane.setVvalue(first);
                    dialog.showAndWait();
                    int k = 0;
                    List<Triplet<Integer, Integer, Integer>> waitToRemove = new LinkedList<>();
                    for (ReferenceBook rb : ref.getBookList()) {
                        for (ReferenceChapter ch : rb.getChapters()) {
                            for (int i : ch.getVerses()) {
                                CheckBox tmp = list.get(k++);
                                if (!tmp.isSelected()) {
                                    waitToRemove.add(new Triplet<>(rb.getBookNumber(), ch.getChapterNumber(), i));
                                }
                            }
                        }
                    }
                    for (Triplet<Integer, Integer, Integer> i : waitToRemove) {
                        ref.removeVerse(i.getFirst(), i.getSecond(), i.getThird());
                    }
                    referenceTextArea.setText(ref.getReference());
                } catch (Exception e) {
                    LOG.error(e.getMessage(), e);
                }
            });
            ref = new Reference();
            ref.setBible(bible);
            allReference = new Reference();
            allReference.setBible(bible);
            referenceTextArea.selectionProperty().addListener((observable, oldValue, newValue) -> {
                try {
                    int startIndex = referenceTextArea.getSelection().getStart();
                    int endIndex = referenceTextArea.getSelection().getEnd();
                    String textAreaText = referenceTextArea.getText();
                    // if (endIndex - startIndex < textAreaText.size()() &&
                    if (startIndex != endIndex) {
                        // System.out.println(startIndex + " " + endIndex);
                        // System.out.println(referenceTextArea.getSelectedText());
                        String[] lines = textAreaText.split("\n");
                        String bookName;
                        int bookIndex = -1;
                        int part = -1;
                        for (String line : lines) {
                            bookName = line.split(":")[0];
                            int lastSpace = bookName.lastIndexOf(" ");
                            if (lastSpace > 0) {
                                try {
                                    part = Integer.parseInt(bookName.substring(lastSpace + 1).trim());
                                } catch (NumberFormatException ignored) {
                                }
                                bookName = bookName.substring(0, lastSpace);
                                bookIndex = bible.getBookIndex(bookName);
                                // System.out.println(bookName + " " + bookIndex);
                            } else {
                                part = Integer.parseInt(bookName.trim());
                            }
                            if (startIndex >= line.length()) {
                                startIndex -= line.length() + 1;
                                endIndex -= line.length() + 1;
                            } else {
                                String[] split = line.split(":");
                                startIndex -= split[0].length() + 1;
                                endIndex -= split[0].length() + 1;
                                if (startIndex < 0) {
                                    startIndex = 0;
                                }
                                if (startIndex < endIndex) {
                                    // System.out.println(split[1].charAt(startIndex));
                                    String verseNumber = split[1].substring(startIndex, startIndex + 1);
                                    boolean number = false;
                                    do {
                                        try {
                                            Integer.parseInt(verseNumber);
                                            number = true;
                                        } catch (NumberFormatException e) {
                                            ++startIndex;
                                            if (startIndex < split[1].length()) {
                                                verseNumber = split[1].substring(startIndex, startIndex + 1);
                                            } else {
                                                break;
                                            }
                                        }
                                    } while (!number);
                                    if (number && startIndex < endIndex) {
                                        ++startIndex;
                                        StringBuilder verseNumberBuffer = new StringBuilder();
                                        verseNumberBuffer.append(verseNumber);
                                        while (startIndex < split[1].length() && startIndex < endIndex) {
                                            String character = split[1].substring(startIndex, startIndex + 1);
                                            try {
                                                Integer.parseUnsignedInt(character);
                                                verseNumberBuffer.append(character);
//											verseNumber += character;
                                                ++startIndex;
                                            } catch (NumberFormatException e) {
                                                break;
                                            }
                                        }
                                        int verse = Integer.parseInt(verseNumberBuffer.toString());
                                        if (bookIndex >= 0 && part >= 0 && verse >= 0) {
                                            // System.out.println(bookIndex + " " +
                                            // part + " " + verse);
                                            setBookPartVerse(bookIndex, part - 1, verse - 1);
                                        }
                                    }
                                }
                                break;
                            }
                        }
                    }
                } catch (Exception e) {
                    LOG.error(e.getMessage(), e);
                }
            });
            referenceTextArea.setOnKeyPressed(event -> {
                try {
                    if (event.getCode() == KeyCode.F1) {
                        mainController.setBlank();
                    }
                } catch (Exception e) {
                    LOG.error(e.getMessage(), e);
                }
            });
            verseListViewSelectionModel.getSelectedIndices().addListener((ListChangeListener<Integer>) c -> {
                try {
                    if (!isSelecting()) {
                        verseSelected();
                    }
                } catch (Exception e) {
                    LOG.error(e.getMessage(), e);
                }
            });
            referenceListView.orientationProperty().set(Orientation.HORIZONTAL);
            referenceListView.getItems().add(settings.getResourceBundle().getString("All"));
            referenceListView.getItems().add("1");
            referenceListView.getItems().add(settings.getResourceBundle().getString("New"));
            references = new LinkedList<>();
            references.add(allReference);
            references.add(ref);
            referenceListView.getSelectionModel().getSelectedIndices().addListener(new ListChangeListener<>() {
                @Override
                synchronized public void onChanged(Change<? extends Integer> c) {
                    try {
                        if (c == null) {
                            return;
                        }
                        ObservableList<? extends Integer> integers = c.getList();
                        if (integers == null || integers.isEmpty()) {
                            return;
                        }
                        int selectedIndex = integers.get(0);
                        int lastIndex = referenceListView.getItems().size() - 1;
                        if (!newReferenceAdded) {
                            if (selectedIndex == lastIndex) {
                                newReferenceAdded = true;
                                Reference reference = new Reference();
                                reference.setBible(bible);
                                references.add(reference);
                                referenceListView.getItems().add(lastIndex, lastIndex + "");
                            }
                            ref = references.get(selectedIndex);
                            refreshReferenceTextArea();
                        } else {
                            newReferenceAdded = false;
                            referenceListView.getSelectionModel().select(lastIndex - 1);
                        }
                    } catch (Exception e) {
                        LOG.error(e.getMessage(), e);
                    }
                }
            });
            horizontalSplitPane.setDividerPositions(settings.getBibleTabHorizontalSplitPaneDividerPosition());
            verticalSplitPane.setDividerPositions(settings.getBibleTabVerticalSplitPaneDividerPosition());
            splitDividersInitialized = true;
            SplitPane.setResizableWithParent(verticalSplitPane, false);
            nextButton.setOnAction(event -> setNextVerse());
            nextButton.addEventHandler(KeyEvent.KEY_PRESSED, new NextButtonEventHandler(nextButton, LOG) {
            });
            partLabel.setText("");
            foundLabel.setText("");
            searchTextField.textProperty().addListener((observable, oldValue, newValue) -> search(newValue));
            searchTextField.setOnKeyPressed(event -> {
                mainController.globalKeyEventHandler().handle(event);
                try {
                    if (event.getCode() == KeyCode.F3) {
                        if (event.isShiftDown()) {
                            goToPreviousFoundBibleVerse();
                        } else {
                            goToNextFoundBibleVerse();
                        }
                        event.consume();
                    }
                } catch (Exception e) {
                    LOG.error(e.getMessage(), e);
                }
            });
            verseFont = Font.font(settings.getVerseListViewFontSize());
            initializeDecreaseIncreaseButtons();
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
    }

    private void goToPreviousFoundBibleVerse() {
        --currentIterationFoundBibleVerseTextFlowIndex;
        goToChangedFoundBibleVerse();
    }

    private void goToNextFoundBibleVerse() {
        ++currentIterationFoundBibleVerseTextFlowIndex;
        goToChangedFoundBibleVerse();
    }

    private void goToChangedFoundBibleVerse() {
        int size = foundedBibleVerseTextFlows.size();
        if (size == 0) {
            return;
        }
        if (currentIterationFoundBibleVerseTextFlowIndex >= size) {
            currentIterationFoundBibleVerseTextFlowIndex = 0;
        }
        if (currentIterationFoundBibleVerseTextFlowIndex < 0) {
            currentIterationFoundBibleVerseTextFlowIndex = size - 1;
        }
        scrollToCurrentIterationFoundBibleVerseTextFlow();
    }

    private void scrollToCurrentIterationFoundBibleVerseTextFlow() {
        BibleVerseTextFlow currentIterationFoundBibleVerseTextFlow = getCurrentIterationFoundBibleVerseTextFlow();
        if (currentIterationFoundBibleVerseTextFlow != null) {
            verseListView.scrollTo(currentIterationFoundBibleVerseTextFlow);
        }
    }

    private BibleVerseTextFlow getCurrentIterationFoundBibleVerseTextFlow() {
        int size = foundedBibleVerseTextFlows.size();
        if (currentIterationFoundBibleVerseTextFlowIndex >= size) {
            return null;
        }
        if (currentIterationFoundBibleVerseTextFlowIndex < 0) {
            return null;
        }
        return foundedBibleVerseTextFlows.get(currentIterationFoundBibleVerseTextFlowIndex);
    }

    private void setBibleListViewOnMouseClicked() {
        ContextMenu contextMenu = new ContextMenu();
        initializeContextMenu(contextMenu, LOG);
        MenuItem deleteMenuItem = getDeleteMenuItem();
        contextMenu.getItems().addAll(deleteMenuItem);
        bibleListView.setOnMouseClicked(event -> {
            try {
                if (event.getButton() == MouseButton.SECONDARY) {
                    Bible selectedItem = bibleListView.getSelectionModel().getSelectedItem();
                    if (selectedItem == null) {
                        return;
                    }
                    contextMenu.show(bibleListView, event.getScreenX(), event.getScreenY());
                } else {
                    contextMenu.hide();
                }
            } catch (Exception e) {
                LOG.error(e.getMessage(), e);
            }
        });
        deleteMenuItem.setOnAction(event -> {
            try {
                Bible selectedItem = bibleListView.getSelectionModel().getSelectedItem();
                if (selectedItem == null) {
                    return;
                }
                ServiceManager.getBibleService().delete(selectedItem);
                if (selectedItem.equivalent(bible)) {
                    bible = null;
                }
                initializeBibles();
            } catch (Exception e) {
                LOG.error(e.getMessage(), e);
            }
        });
    }

    private void setAbbreviationButtonVisibility() {
        abbreviationToggleButton.setManaged(settings.getBibleShortName());
        if (bible != null) {
            abbreviationToggleButton.setSelected(bible.isShowAbbreviation());
        }
    }

    private void initializeDecreaseIncreaseButtons() {
        try {
            decreaseButton.setOnAction(event -> shrinkVerseListViewText());
            increaseButton.setOnAction(event -> enlargeVerseListViewText());
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
    }

    private void shrinkVerseListViewText() {
        try {
            verseFont = Font.font(verseFont.getSize() - 1);
            setVerseListViewTextFont(verseFont);
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
    }

    private void enlargeVerseListViewText() {
        try {
            verseFont = Font.font(verseFont.getSize() + 1);
            setVerseListViewTextFont(verseFont);
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
    }

    private void setVerseListViewTextFont(Font verseFont) {
        try {
            settings.setVerseListViewFontSize(verseFont.getSize());
            for (BibleVerseTextFlow textFlow : verseListView.getItems()) {
                for (Node node : textFlow.getChildren()) {
                    if (node instanceof final Text node1) {
                        node1.setFont(verseFont);
                    }
                }
            }
            addAllVerse();
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
    }

    private void addAllVerse() {
        try {
            verseListView.getItems().clear();
            if (selectedPart >= 0 && selectedBook >= 0) {
                List<Book> books = bible.getBooks();
                Book book = books.get(selectedBook);
                List<BibleVerse> bibleVerses = book.getChapters().get(selectedPart).getVerses();
                for (int i = 0; i < bibleVerses.size(); ++i) {
                    BibleVerse bibleVerse = bibleVerses.get(i);
                    final String s = (i + 1) + ".	" + bibleVerse.getText();
                    TextFlow textFlow = new TextFlow();
                    final Text text = new Text(s);
                    text.setFont(verseFont);
                    setGeneralTextColor(text);
                    textFlow.getChildren().add(text);
                    textFlow.setTextAlignment(TextAlignment.LEFT);
                    textFlow.setPrefWidth(verseListView.getWidth() - verseRightMargin);
                    BibleVerseTextFlow bibleVerseTextFlow = new BibleVerseTextFlow(textFlow, bibleVerse);
                    verseListView.getItems().add(bibleVerseTextFlow);
                }
            }
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
    }

    private void search(String text) {
        try {
            if (text.trim().isEmpty()) {
                Platform.runLater(() -> {
                    try {
                        addAllVerse();
                        foundLabel.setText("");
                    } catch (Exception e) {
                        LOG.error(e.getMessage(), e);
                    }
                });
                return;
            }
            Thread thread = new Thread(() -> {
                try {
                    if (selectedPart >= 0 && selectedBook >= 0) {
                        String text3 = text;
                        if (!Settings.getInstance().isWithAccents()) {
                            text3 = strip(text);
                        }
                        text3 = text3.replace("]", "").replace("[", "");
                        if (text3.isEmpty()) {
                            return;
                        }
                        List<BibleVerseTextFlow> tmpSearchListView = new ArrayList<>();
                        final Chapter chapter = bible.getBooks().get(selectedBook).getChapters().get(selectedPart);
                        int found = 0;
                        List<BibleVerse> bibleVerses = chapter.getVerses();
                        BibleVerse firstFoundedVerse = null;
                        BibleVerseTextFlow currentIterationFoundBibleVerseTextFlow = getCurrentIterationFoundBibleVerseTextFlow();
                        foundedBibleVerseTextFlows.clear();
                        for (int i = 0; i < bibleVerses.size(); ++i) {
                            BibleVerse bibleVerse = bibleVerses.get(i);
                            String verse = bibleVerse.getText();
                            TextFlow textFlow = new TextFlow();
                            Text reference = new Text((i + 1) + ".	");
                            reference.setFont(verseFont);
                            setReferenceTextColor(reference);
                            textFlow.getChildren().add(reference);
                            char[] chars = stripAccents(verse).toLowerCase().toCharArray();
                            char[] searchTextChars = text3.toCharArray();
                            int verseIndex = 0;
                            int fromIndex = 0;
                            int lastAddedIndex = 0;
                            boolean founded = false;
                            for (int j = 0; j < chars.length; ++j) {
                                if ('a' <= chars[j] && chars[j] <= 'z') {
                                    if (chars[j] == searchTextChars[verseIndex]) {
                                        if (verseIndex == 0) {
                                            fromIndex = j;
                                        }
                                        ++verseIndex;
                                        if (verseIndex == searchTextChars.length) {
                                            if (lastAddedIndex != fromIndex) {
                                                Text text1 = new Text(verse.substring(lastAddedIndex, fromIndex));
                                                setGeneralTextColor(text1);
                                                text1.setFont(verseFont);
                                                textFlow.getChildren().add(text1);
                                            }
                                            Text foundText = new Text(verse.substring(fromIndex, j + 1));
                                            setFoundTextColor(foundText);
                                            foundText.setFont(Font.font(verseFont.getFamily(), FontWeight.BOLD, verseFont.getSize()));
                                            addTextWithBackGround(textFlow, foundText);
                                            lastAddedIndex = j + 1;
                                            verseIndex = 0;
                                            founded = true;
                                            ++found;
                                            if (firstFoundedVerse == null) {
                                                firstFoundedVerse = bibleVerse;
                                            }
                                        }
                                    } else {
                                        if (verseIndex != 0) {
                                            --j;
                                            verseIndex = 0;
                                        }
                                    }
                                }
                            }
                            if (lastAddedIndex < verse.length()) {
                                Text text1 = new Text(verse.substring(lastAddedIndex));
                                setGeneralTextColor(text1);
                                text1.setFont(verseFont);
                                textFlow.getChildren().add(text1);
                            }
                            textFlow.setTextAlignment(TextAlignment.LEFT);
                            textFlow.setPrefWidth(verseListView.getWidth() - verseRightMargin);
                            BibleVerseTextFlow bibleVerseTextFlow = new BibleVerseTextFlow(textFlow, bibleVerse);
                            tmpSearchListView.add(bibleVerseTextFlow);
                            if (founded) {
                                foundedBibleVerseTextFlows.add(bibleVerseTextFlow);
                            }
                        }

                        int finalFound = found;
                        Platform.runLater(() -> {
                            try {
                                final String found1 = settings.getResourceBundle().getString("Found");
                                foundLabel.setText(found1 + ": " + finalFound);
                                //TODO try not to clear
                                ObservableList<BibleVerseTextFlow> items = verseListView.getItems();
                                items.clear();
                                items.addAll(tmpSearchListView);
                                int index = 0;
                                currentIterationFoundBibleVerseTextFlowIndex = 0;
                                if (currentIterationFoundBibleVerseTextFlow != null) {
                                    for (BibleVerseTextFlow bibleVerseTextFlow : foundedBibleVerseTextFlows) {
                                        if (isSameBibleVerse(bibleVerseTextFlow, currentIterationFoundBibleVerseTextFlow)) {
                                            currentIterationFoundBibleVerseTextFlowIndex = index;
                                        }
                                        ++index;
                                    }
                                }
                                scrollToCurrentIterationFoundBibleVerseTextFlow();
                            } catch (Exception e) {
                                LOG.error(e.getMessage(), e);
                            }
                        });
                    }
                } catch (Exception e) {
                    LOG.error(e.getMessage(), e);
                }
            });
            thread.start();
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
    }

    private boolean isSameBibleVerse(BibleVerseTextFlow bibleVerseTextFlow, BibleVerseTextFlow previousIterationFoundBibleVerseTextFlow) {
        try {
            return bibleVerseTextFlow.getBibleVerse().getId().equals(previousIterationFoundBibleVerseTextFlow.getBibleVerse().getId());
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            return false;
        }
    }

    void initializeBibles() {
        try {
            Bible previouslySelectedBible = this.bible;
            List<Bible> bibles = checkForBibles();
            if (bibles == null) {
                return;
            }
            if (bibleListView.getItems().size() == bibles.size()) {
                return;
            }
            clearAndAddBibles(previouslySelectedBible, bibles);
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
    }

    public void initializeBiblesWithoutSameSizeCheck() {
        try {
            Bible previouslySelectedBible = this.bible;
            List<Bible> bibles = checkForBibles();
            if (bibles == null) {
                return;
            }
            clearAndAddBibles(previouslySelectedBible, bibles);
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
    }

    private List<Bible> checkForBibles() {
        lazyInitialize();
        BibleService bibleService = ServiceManager.getBibleService();
        List<Bible> bibles = bibleService.findAll();
        bibleService.sort(bibles);
        bibles.sort((o1, o2) -> Integer.compare(o2.getUsage(), o1.getUsage()));
        if (bibles.isEmpty()) {
            bibleListView.getItems().clear();
            downloadBibles();
            return null;
        }
        return bibles;
    }

    private void clearAndAddBibles(Bible previouslySelectedBible, List<Bible> bibles) {
        ObservableList<Bible> items = bibleListView.getItems();
        items.clear();
        items.addAll(bibles);
        parallelBibles.clear();
        parallelBibles.addAll(bibles);
        selectBibleInListView(previouslySelectedBible);
        addAllBooks();
        historyController.setBible(bible);
        bibleSearchController.setBibles(bibles);
    }

    private void selectBibleInListView(Bible previouslySelectedBible) {
        MultipleSelectionModel<Bible> selectionModel = bibleListView.getSelectionModel();
        if (selectionModel.getSelectedItem() == null) {
            if (!tryToSelectPreviouslySelected(previouslySelectedBible, selectionModel)) {
                selectionModel.selectFirst();
            }
        }
    }

    private boolean tryToSelectPreviouslySelected(Bible previouslySelectedBible, MultipleSelectionModel<Bible> selectionModel) {
        if (previouslySelectedBible != null) {
            Bible bibleFromList = getBibleFromList(bibleListView.getItems(), previouslySelectedBible);
            if (bibleFromList != null) {
                selectionModel.select(bibleFromList);
                return selectionModel.getSelectedItem() != null;
            }
        }
        return false;
    }

    private Bible getBibleFromList(ObservableList<Bible> bibles, Bible bible) {
        for (Bible aBible : bibles) {
            if (aBible.equivalent(bible)) {
                return aBible;
            }
        }
        return null;
    }

    boolean isNotAllBooks() {
        return !isAllBooks;
    }

    void setMainController(MyController mainController) {
        try {
            this.mainController = mainController;
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
    }

    void setProjectionScreenController(ProjectionScreenController projectionScreenController) {
        try {
            this.projectionScreenController = projectionScreenController;
            projectionScreenController.setBibleController(this);
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
    }

    void setBibleSearchController(BibleSearchController bibleSearchController) {
        this.bibleSearchController = bibleSearchController;
    }

    void setRecentController(RecentController recentController) {
        this.recentController = recentController;
    }

    void setHistoryController(HistoryController historyController) {
        this.historyController = historyController;
    }

    public boolean contains(String a, String b) {
        try {
            a = StringUtils.stripAccents(a);
            b = StringUtils.stripAccents(b);
            return a.toLowerCase().contains(b.toLowerCase().trim());
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
        return false;
    }

    void addAllBooks() {
        try {
            isAllBooks = false;
            if (searchIBook == null) {
                searchIBook = new ArrayList<>();
            } else {
                searchIBook.clear();
            }
            bookListView.getItems().clear();
            for (int iBook = 0; iBook < bible.getBooks().size(); ++iBook) {
                Book book = bible.getBooks().get(iBook);
                bookListView.getItems().add(book.getShortOrTitle());
                searchIBook.add(iBook);
            }
            isAllBooks = true;
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
    }

    private void titleSearch(String text) {
        try {
            if (!text.trim().isEmpty()) {
                if (searchIBook == null) {
                    searchIBook = new ArrayList<>();
                } else {
                    searchIBook.clear();
                }
                bookListView.getItems().clear();
                for (int iBook = 0; iBook < bible.getBooks().size(); ++iBook) {
                    Book book = bible.getBooks().get(iBook);
                    addIfContains(text, iBook, book.getTitle());
                    addIfContains(text, iBook, book.getShortName());
                }
                if (bookListView.getItems().size() == 1) {
                    bookListView.getSelectionModel().select(0);
                    oldReplace = true;
                    partTextField.setText("1");
                    oldReplace = true;
                    partTextField.setText("");
                    partTextField.requestFocus();
                }
            } else {
                addAllBooks();
            }
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
    }

    private void addIfContains(String text, int iBook, String s) {
        if (s == null || s.trim().isEmpty()) {
            return;
        }
        if (contains(s, text)) {
            bookListView.getItems().add(s);
            searchIBook.add(iBook);
        }
    }

    private boolean setNextPart() {
        try {
            if (selectedVerse + 1 >= verseListView.getItems().size() && selectedPart + 1 < partListView.getItems().size()) {
                ++selectedPart;
                partListView.getSelectionModel().select(selectedPart);
                partListView.scrollTo(selectedPart);
                verseListView.getSelectionModel().select(0);
                verseListView.scrollTo(0);
                return true;
            }
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
        return false;
    }

    private boolean setPreviousPart() {
        try {
            if (selectedVerse == 0 && selectedPart - 1 >= 0) {
                --selectedPart;
                isLastVerse = true;
                partListView.scrollTo(selectedPart);
                partListView.getSelectionModel().select(selectedPart);
                return true;
            }
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
        return false;
    }

    void setNextVerse() {
        try {
            if (selectedVerse + 1 < verseListView.getItems().size()) {
                ++selectedVerse;
                verseListView.getSelectionModel().clearAndSelect(selectedVerse);
                verseListView.scrollTo(selectedVerse - 1);
            } else {
                setNextPart();
            }
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
    }

    void setPreviousVerse() {
        try {
            if (selectedVerse > 0) {
                --selectedVerse;
                verseListView.getSelectionModel().clearAndSelect(selectedVerse);
                verseListView.scrollTo(selectedVerse);
            } else {
                setPreviousPart();
            }
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
    }

    private void setSelection(ListCell<BibleVerseTextFlow> cell) {
        try {
            if (cell.isSelected()) {
                verseListView.getSelectionModel().clearSelection(cell.getIndex());
            } else {
                verseListView.getSelectionModel().select(cell.getIndex());
            }
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
    }

    ListView<String> getBookListView() {
        return bookListView;
    }

    ListView<Integer> getPartListView() {
        return partListView;
    }

    ListView<BibleVerseTextFlow> getVerseListView() {
        return verseListView;
    }

    private boolean isSelecting() {
        return isSelecting;
    }

    void setSelecting(boolean isSelecting) {
        try {
            if (this.isSelecting && !isSelecting && verseListView.isFocused() && wasSelectionChange) {
                verseSelected();
            }
            wasSelectionChange = false;
            this.isSelecting = isSelecting;
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
    }

    private void verseSelected() {
        try {
            if (bible == null) {
                return;
            }
            ObservableList<Integer> ob = verseListView.getSelectionModel().getSelectedIndices();
            StringBuilder string = new StringBuilder();
            int iVerse;
            iVerse = getFirstFromSelectedIndices(ob);
            String text = null;
            ProjectionData projectionData = new ProjectionData();
            ProjectionDTO projectionDTO = new ProjectionDTO();
            projectionData.setProjectionDTO(projectionDTO);
            if (selectedBook >= 0 && selectedPart >= 0 && iVerse >= 0) {
                List<VerseIndex> verseIndices = new ArrayList<>();
                List<BibleVerse> bibleVerses = new ArrayList<>(ob.size());
                for (int i : ob) {
                    BibleVerse bibleVerse = bible.getBooks().get(selectedBook).getChapters().get(selectedPart).getVerses().get(i);
                    bibleVerses.add(bibleVerse);
                    List<VerseIndex> verseIndexList = bibleVerse.getVerseIndices();
                    if (verseIndexList != null) {
                        verseIndices.addAll(verseIndexList);
                    }
                }
                ProjectionAssembler projectionAssembler = ProjectionAssembler.getInstance();
                projectionAssembler.setVerseIndices(projectionDTO, verseIndices);
                projectionAssembler.setSelectedBible(projectionDTO, bible);
                projectionAssembler.setSelectedVerses(projectionDTO, selectedBook, selectedPart, ob);
                string = new StringBuilder(getVersesAndReference(bible, bibleVerses));
                if (settings.isParallel()) {
                    for (Bible parallelBible : parallelBibles) {
                        if (parallelBible.isParallelSelected()) {
                            string.append("\n").append(getBibleVerseWithReferenceText(verseIndices, parallelBible, selectedBook, selectedPart, ob));
                        }
                    }
                }
                ArrayList<Integer> tmp = new ArrayList<>(ob);
                text = string.toString();
                String verseNumbers = text.substring(string.lastIndexOf(":") + 1, string.length()).replace("]", "").replace("</color>", "");
                recentController.addRecentBibleVerse(text, selectedBook, selectedPart, iVerse, verseNumbers, tmp);
            }
            if (!string.isEmpty()) {
                if (!settings.isShowReferenceOnly()) {
                    //noinspection ConstantConditions
                    projectionScreenController.setText(text, ProjectionType.BIBLE, projectionData);
                }
                for (int i : ob) {
                    if (i == -1) {
                        i = verseListView.getSelectionModel().getSelectedIndex();
                    }
                    ref.addVerse(selectedBook, selectedPart + 1, i + 1);
                    allReference.addVerse(selectedBook, selectedPart + 1, i + 1);
                }
                refreshReferenceTextArea();
                if (settings.isShowReferenceOnly()) {
                    projectionScreenController.setText(referenceTextArea.getText(), ProjectionType.REFERENCE, projectionData);
                }
            }
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
    }

    private void refreshReferenceTextArea() {
        try {
            ref.setBible(bible);
            referenceTextArea.setText(ref.getReference());
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
    }

    private EventHandler<KeyEvent> numeric_Validation() {
        return getKeyEventEventHandler(LOG);
    }

    private void setBookPartVerse(int book, int part, int verse) {
        try {
            addAllBooks();
            verseListView.getSelectionModel().clearSelection();
            verseListView.scrollTo(0);
            verseListView.refresh();
            if (book >= 0 && book < bookListView.getItems().size()) {
                if (bookListView.getSelectionModel().getSelectedIndex() != book) {
                    searchSelected = 1;
                } else {
                    searchSelected = 0;
                }
                bookListView.getSelectionModel().select(book);
                while (searchSelected == 1) {
                    try {
                        TimeUnit.MILLISECONDS.sleep(1);
                    } catch (InterruptedException ignored) {
                    }
                }
                bookListView.scrollTo(book);
                if (part >= 0 && part < partListView.getItems().size()) {
                    if (partListView.getSelectionModel().getSelectedIndex() != part) {
                        searchSelected = 2;
                    } else {
                        searchSelected = 0;
                    }
                    partListView.getSelectionModel().select(part);
                    while (searchSelected == 2) {
                        try {
                            TimeUnit.MILLISECONDS.sleep(1);
                        } catch (InterruptedException ignored) {
                        }
                    }
                    partListView.scrollTo(part);
                    if (verse >= 0 && verse < verseListView.getItems().size()) {
                        verseListView.scrollTo(verse);
                        verseListView.getFocusModel().focus(verse);
                    }
                }
            }
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
    }

    void onClose() {
        try {
            Settings settings = this.settings;
            if (splitDividersInitialized) {
                settings.setBibleTabHorizontalSplitPaneDividerPosition(horizontalSplitPane.getDividerPositions()[0]);
                settings.setBibleTabVerticalSplitPaneDividerPosition(verticalSplitPane.getDividerPositions()[0]);
            }
            settings.save();
            if (bible != null) {
                bible.setUsage(bible.getUsage() + 1);
                ServiceManager.getBibleService().update(bible);
            }
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
    }

    public void downloadBibles() {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(MainDesktop.class.getResource("/view/DownloadBibles.fxml"));
            loader.setResources(Settings.getInstance().getResourceBundle());
            Pane root = loader.load();
            DownloadBiblesController controller = loader.getController();
            Scene scene = new Scene(root);
            setStyleFile(scene);
            Stage stage = getAStage(getClass());
            stage.setScene(scene);
            stage.setTitle(Settings.getInstance().getResourceBundle().getString("Download bibles"));
            controller.setBibleController(this);
            controller.setStage(stage);
            stage.show();
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
    }

    private void setStyleFile(Scene scene) {
        URL resource = getClass().getResource("/view/" + settings.getSceneStyleFile());
        if (resource != null) {
            scene.getStylesheets().add(resource.toExternalForm());
        }
    }

    public void parallelBibles() {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(MainDesktop.class.getResource("/view/ParallelBibles.fxml"));
            loader.setResources(Settings.getInstance().getResourceBundle());
            Pane root = loader.load();
            ParallelBiblesController controller = loader.getController();
            Scene scene = new Scene(root);
            setStyleFile(scene);
            Stage stage = getCustomStage2(getClass(), scene, root.getPrefWidth(), root.getPrefHeight());
            stage.setTitle(Settings.getInstance().getResourceBundle().getString("Parallel"));
            controller.initialize(bibleListView.getItems());
            controller.setBibleController(this);
            controller.setStage(stage);
            stage.show();
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
    }

    void sortParallelBibles() {
        ParallelBiblesController.sortParallelBibles(parallelBibles);
    }

    public void toggleAbbreviation() {
        try {
            if (bible != null) {
                bible.setShowAbbreviation(!abbreviationToggleButton.isSelected());
                ServiceManager.getBibleService().update(bible);
            }
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
    }

    void setSettingsController(SettingsController settingsController) {
        settingsController.addOnSaveListener(this::setAbbreviationButtonVisibility);
    }

    public void onKeyEvent(KeyEvent event) {
        KeyCode keyCode = event.getCode();
        if (event.isControlDown()) {
            if (keyCode == KeyCode.B) {
                bookTextField.requestFocus();
            }
            if (keyCode == KeyCode.I) {
                verseTextField.requestFocus();
            }
            if (keyCode == KeyCode.P) {
                partTextField.requestFocus();
            }
            if (keyCode == KeyCode.R) {
                sendProjectionScreenText.fire();
            }
        }
    }

    public void selectBible(Bible bible) {
        bibleListView.getSelectionModel().select(bible);
    }
}
