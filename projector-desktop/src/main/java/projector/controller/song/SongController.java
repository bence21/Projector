package projector.controller.song;

import com.bence.projector.common.dto.ProjectionDTO;
import com.bence.projector.common.dto.SongDTO;
import com.bence.projector.common.dto.SongVerseProjectionDTO;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.MultipleSelectionModel;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.SingleSelectionModel;
import javafx.scene.control.Slider;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.Tooltip;
import javafx.scene.effect.BlendMode;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.text.TextFlow;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Callback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import projector.MainDesktop;
import projector.api.SongApiBean;
import projector.api.assembler.SongAssembler;
import projector.api.retrofit.ApiManager;
import projector.application.ProjectionType;
import projector.application.ProjectorState;
import projector.application.Settings;
import projector.application.SongVerseTime;
import projector.application.SongVerseTimeService;
import projector.controller.MyController;
import projector.controller.ProjectionScreenController;
import projector.controller.ProjectionTextChangeListener;
import projector.controller.RecentController;
import projector.controller.eventHandler.NextButtonEventHandler;
import projector.controller.language.DownloadLanguagesController;
import projector.controller.song.util.ContainsResult;
import projector.controller.song.util.LastSearching;
import projector.controller.song.util.OrderMethod;
import projector.controller.song.util.ScheduleSong;
import projector.controller.song.util.SearchedSong;
import projector.controller.song.util.SongTextFlow;
import projector.controller.util.UserService;
import projector.model.FavouriteSong;
import projector.model.Language;
import projector.model.Song;
import projector.model.SongCollection;
import projector.model.SongCollectionElement;
import projector.model.SongVerse;
import projector.remote.SongReadRemoteListener;
import projector.remote.SongRemoteListener;
import projector.service.FavouriteSongService;
import projector.service.LanguageService;
import projector.service.ServiceException;
import projector.service.ServiceManager;
import projector.service.SongCollectionService;
import projector.service.SongService;
import projector.utils.CustomProperties;
import projector.utils.IntegerFilter;
import projector.utils.scene.text.MyTextFlow;
import projector.utils.scene.text.SongVersePartTextFlow;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.reflect.Type;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static com.bence.projector.common.converter.OpenLPXmlConverter.getXmlSongs;
import static java.lang.Math.min;
import static projector.application.SongVerseTimeService.getSongVersTimesFilePath;
import static projector.controller.MessageDialogController.confirmDeletion;
import static projector.utils.ColorUtil.getCollectionNameColor;
import static projector.utils.ColorUtil.getSongTitleColor;
import static projector.utils.ContextMenuUtil.getDeleteMenuItem;
import static projector.utils.ContextMenuUtil.initializeContextMenu;
import static projector.utils.KeyEventUtil.getTextFromEvent;
import static projector.utils.SceneUtils.getAStage;
import static projector.utils.SceneUtils.getCustomStage2;
import static projector.utils.SceneUtils.getCustomStage3;
import static projector.utils.StringUtils.stripAccents;

public class SongController {

    private static final Logger LOG = LoggerFactory.getLogger(SongController.class);
    private static final double minOpacity = 0.4;
    private final SongService songService;
    private final Settings settings = Settings.getInstance();
    private final String BASE_URL = ApiManager.DOMAIN;
    @SuppressWarnings("FieldCanBeLocal")
    private final String link = "http://" + BASE_URL;
    @SuppressWarnings("FieldCanBeLocal")
    private final String link2 = "https://" + BASE_URL;
    @SuppressWarnings("FieldCanBeLocal")
    private final String link3 = "http://www." + BASE_URL;
    @SuppressWarnings("FieldCanBeLocal")
    private final String link4 = "https://www." + BASE_URL;
    private final String prefix = "id:";
    private final SongCollectionService songCollectionService = ServiceManager.getSongCollectionService();
    private final String vowels = CustomProperties.getInstance().vowels();
    private final SongController songController = this;
    public Spinner<Integer> maxLineSpinner;
    @FXML
    private Button openLPImportButton;
    @FXML
    private ListView<SongVerse> verseOrderListView;
    @FXML
    private HBox authorBox;
    @FXML
    private BorderPane rightBorderPane;
    @FXML
    private Button showVersionsButton;
    @FXML
    private ComboBox<Language> languageComboBox;
    @FXML
    private Button importButton;
    @FXML
    private Button exportButton;
    @FXML
    private ComboBox<OrderMethod> sortComboBox;
    @FXML
    private TextField verseTextField;
    @FXML
    private TextField searchTextField;
    @FXML
    private CheckBox searchInTextCheckBox;
    @FXML
    private CheckBox favoritesCheckBox;
    @FXML
    private ListView<SearchedSong> searchedSongListView;
    @FXML
    private ListView<SongVersePartTextFlow> songListView;
    @FXML
    private Button downloadButton;
    @FXML
    private Button uploadButton;
    @FXML
    private Button newSongButton;
    @FXML
    private ListView<ScheduleSong> scheduleListView;
    @FXML
    private ListView<SongCollection> songCollectionListView;
    @FXML
    private SplitPane horizontalSplitPane;
    @FXML
    private SplitPane verticalSplitPane;
    @FXML
    private BorderPane leftBorderPane;
    @FXML
    private Button nextButton;
    @FXML
    private Slider songHeightSlider;
    @FXML
    private ToggleButton progressLineToggleButton;
    @FXML
    private CheckBox aspectRatioCheckBox;
    @FXML
    private Button starButton;
    @FXML
    private TextField authorTextField;
    private ProjectionScreenController projectionScreenController;
    private ProjectionScreenController previewProjectionScreenController;
    private RecentController recentController;
    private ScheduleController scheduleController;
    private List<Song> songs = new ArrayList<>();
    private String lastSearchText = "";
    private SongVerseTime activeSongVerseTime;
    private long timeStart;
    private List<SongVerseTime> previousSongVerseTimeList;
    private int previousSelectedVerseIndex;
    private ScheduledExecutorService opacityScheduler;
    private SongVerseTimeService songVerseTimeService;
    private MyController mainController;
    private boolean isBlank = false;
    private LastSearching lastSearching = LastSearching.IN_TITLE;
    private SongCollection selectedSongCollection;
    private List<ProjectionTextChangeListener> projectionTextChangeListeners;
    private Song selectedSong;
    private List<SongVerse> selectedSongVerseList;
    private int successfullyCreated;
    private SongRemoteListener songRemoteListener;
    private SongReadRemoteListener songReadRemoteListener;
    private boolean initialized = false;
    private boolean synchronizingVerseOrderListSelection = false;
    private List<FavouriteSong> favouriteSongs;
    private boolean starButtonPreviousStateWithStar = false;
    private boolean pauseSortOrFilter = false;
    private boolean versionsButtonPreviousStateWithStar = false;
    private final List<SongVersePartTextFlow> inCalculationSongVersePartTextFlows = new ArrayList<>(); // it's important to be the same to the end!

    public SongController() {
        songService = ServiceManager.getSongService();
    }

    public static EventHandler<KeyEvent> getKeyEventEventHandler(Logger log) {
        return event -> {
            try {
                if (!getTextFromEvent(event).matches("[0-9]") && event.getCode() != KeyCode.F1) {
                    event.consume();
                }
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        };
    }

    static void setSongCollectionForSongsInHashMap(List<SongCollection> songCollections, HashMap<String, Song> hashMap) {
        for (SongCollection songCollection : songCollections) {
            for (SongCollectionElement songCollectionElement : songCollection.getSongCollectionElements()) {
                String songUuid = songCollectionElement.getSongUuid();
                if (songUuid != null && hashMap.containsKey(songUuid)) {
                    Song song = hashMap.get(songUuid);
                    song.addToSongCollections(songCollection);
                    song.addToSongCollectionElements(songCollectionElement);
                    songCollectionElement.setSong(song);
                }
            }
        }
    }

    static TextFlow setTextFlowsText(SongTextFlow item, TextFlow textFlow) {
        Text text = new Text("");
        if (textFlow == null) {
            textFlow = new TextFlow(text);
            item.setTextFlow(textFlow);
        } else {
            ObservableList<Node> children = textFlow.getChildren();
            children.clear();
            children.add(text);
        }
        return textFlow;
    }

    static void setSongCollections(List<Song> songs) {
        List<SongCollection> songCollections = ServiceManager.getSongCollectionService().findAll();
        HashMap<String, Song> hashMap = new HashMap<>(songs.size());
        for (Song song : songs) {
            hashMap.put(song.getUuid(), song);
            song.clearSongCollectionLists();
        }
        setSongCollectionForSongsInHashMap(songCollections, hashMap);
    }

    private static void addWordsInCollection(Song song, Collection<String> words) {
        for (SongVerse songVerse : song.getVerses()) {
            String[] split = songVerse.getText().split("[\\s\\t\\n\\r]");
            for (String word : split) {
                word = stripAccents(word.toLowerCase());
                words.add(word);
            }
        }
    }

    private static SongCollection getSongCollectionFromRepository(SongCollectionElement songCollectionElement) {
        SongCollection songCollection = songCollectionElement.getSongCollection();
        if (songCollection == null) {
            return null;
        }
        SongCollection byUuid = ServiceManager.getSongCollectionService().findByUuid(songCollection.getUuid());
        if (byUuid == null) {
            return songCollection;
        }
        return byUuid;
    }

    private static Comparator<Song> getSongComparatorByAscendingByTitle() {
        return Comparator.comparing(l -> l.getStrippedTitle().toLowerCase());
    }

    private static Comparator<Song> getSongComparatorByDescendingByTitle() {
        return (l, r) -> r.getStrippedTitle().toLowerCase().compareTo(l.getStrippedTitle().toLowerCase());
    }

    private static Comparator<Song> getSongComparatorByModifiedDate() {
        return (l, r) -> r.getModifiedDate().compareTo(l.getModifiedDate());
    }

    private static Comparator<Song> getSongComparatorByPublished() {
        return (l, r) -> {
            if (l.isPublished() && !r.isPublished()) {
                return 1;
            } else if (!l.isPublished() && r.isPublished()) {
                return -1;
            }
            return 0;
        };
    }

    private static Comparator<Song> getSongComparatorByRelevanceOrder() {
        return (lhs, rhs) -> {
            Long scoreL = lhs.getScore();
            Long scoreR = rhs.getScore();
            if (scoreL.equals(scoreR)) {
                return rhs.getModifiedDate().compareTo(lhs.getModifiedDate());
            }
            return scoreR.compareTo(scoreL);
        };
    }

    private static boolean hasDifferentFavourite(List<Song> versionGroupSongs, Song selectedSong) {
        if (selectedSong == null || versionGroupSongs == null) {
            return false;
        }
        if (selectedSong.isFavourite()) {
            return false;
        }
        for (Song song : versionGroupSongs) {
            if (!selectedSong.equivalent(song) && song.isFavourite()) {
                return true;
            }
        }
        return false;
    }

    public synchronized void lazyInitialize() {
        if (initialized) {
            return;
        }
        initialized = true;
        try {
            songVerseTimeService = SongVerseTimeService.getInstance();
            previousSongVerseTimeList = new LinkedList<>();
            songListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
            newSongButton.setFocusTraversable(false);
            searchTextField.textProperty().addListener((observable, oldValue, newValue) -> {
                try {
                    if (searchInTextCheckBox.isSelected()) {
                        search(newValue);
                    } else {
                        titleSearch(newValue);
                    }
                } catch (Exception e) {
                    LOG.error(e.getMessage(), e);
                }
            });
            searchTextField.setOnKeyPressed(event -> {
                try {
                    if (event.getCode() == KeyCode.ENTER) {
                        selectFirstSong();
                    } else if (event.getCode() == KeyCode.F1) {
                        mainController.setBlank();
                    }
                } catch (Exception e) {
                    LOG.error(e.getMessage(), e);
                }
            });
            searchInTextCheckBox.selectedProperty().addListener((observable, oldValue, newValue) -> searchAgainBySearchOption(newValue));
            favoritesCheckBox.selectedProperty().addListener((observable, oldValue, newValue) -> searchAgain());
            searchedSongListView.setCellFactory(param -> new ListCell<>() {
                @Override
                protected void updateItem(SearchedSong searchedSong, boolean empty) {
                    try {
                        super.updateItem(searchedSong, empty);
                        if (searchedSong == null) {
                            setGraphic(null);
                        } else if (empty || searchedSong.getSong().getTitle() == null) {
                            TextFlow textFlow = setTextFlowsText(searchedSong, searchedSong.getTextFlow());
                            setGraphic(textFlow);
                        } else {
                            Song song = searchedSong.getSong();
                            TextFlow textFlow = searchedSong.getTextFlow();
                            if (textFlow == null) {
                                textFlow = new TextFlow();
                            } else {
                                textFlow.getChildren().clear();
                            }
                            ObservableList<Node> children = textFlow.getChildren();
                            for (SongCollectionElement songCollectionElement : song.getSongCollectionElements()) {
                                SongCollection songCollection = getSongCollectionFromRepository(songCollectionElement);
                                if (songCollection == null) {
                                    continue;
                                }
                                Text collectionName = new Text(songCollection.getName() + " ");
                                collectionName.setFill(getCollectionNameColor());
                                children.add(collectionName);
                                Text ordinalNumber = new Text(songCollectionElement.getOrdinalNumber() + "\n");
                                ordinalNumber.setFill(getCollectionNameColor());
                                children.add(ordinalNumber);
                            }
                            Text title = new Text(song.getTitle());
                            title.setFill(getSongTitleColor());
                            addFavouriteStarImageForFavourite(song, children, title);
                            if (searchedSong.getFoundAtVerse() != null) {
                                Text text = new Text(searchedSong.getFoundAtVerse());
                                text.setFill(Color.rgb(17, 150, 0));
                                children.add(text);
                            }
                            setGraphic(textFlow);
                        }
                    } catch (Exception e) {
                        LOG.error(e.getMessage(), e);
                    }
                }
            });
            searchedSongListView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> onSongSelectFromListView());
            initialization2();
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
    }

    private void onSongSelectFromListView() {
        try {
            MultipleSelectionModel<SearchedSong> selectionModel = searchedSongListView.getSelectionModel();
            int index = selectionModel.selectedIndexProperty().get();
            if (index >= 0) {
                prepareSelectedSong(selectionModel.getSelectedItem().getSong());
            }
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
    }

    private void prepareSelectedSong(Song selectedSong1) {
        try {
            if (selectedSong1 == null) {
                return;
            }
            if (activeSongVerseTime != null && activeSongVerseTime.getVerseTimes() != null && activeSongVerseTime.getVerseTimes().length > previousSelectedVerseIndex && previousSelectedVerseIndex >= 0 && activeSongVerseTime.getVerseTimes()[previousSelectedVerseIndex] == 0.0) {
                double x = System.currentTimeMillis() - timeStart;
                x /= 1000;
                activeSongVerseTime.getVerseTimes()[previousSelectedVerseIndex] = x;
            }
            ObservableList<SongVersePartTextFlow> songListViewItems = songListView.getItems();
            songListViewItems.clear();
            selectedSong = selectedSong1;

            showVersionsButton.setVisible(false);
            String versionGroup = selectedSong.getVersionGroup();
            if (versionGroup == null) {
                versionGroup = selectedSong.getUuid();
            }
            List<Song> allByVersionGroup = null;
            if (versionGroup != null) {
                allByVersionGroup = songService.findAllByVersionGroup(versionGroup);
                if (allByVersionGroup.size() > 1) {
                    showVersionsButton.setVisible(true);
                }
            }
            checkForFavouriteInVersionGroup(allByVersionGroup, selectedSong);

            Scene scene = projectionScreenController.getScene();
            int width = 0;
            int height = 0;
            if (scene != null) {
                width = (int) scene.getWidth();
                height = (int) scene.getHeight();
            }
            if (width == 0) {
                width = 16;
                height = 9;
            }
            final int size = (int) songHeightSlider.getValue();
            selectedSongVerseList = selectedSong.getSongVersesByVerseOrder();
            SongVersePartTextFlow songVersePartTextFlow = new SongVersePartTextFlow();
            MyTextFlow myTextFlow = songVersePartTextFlow.getMyTextFlow();
            myTextFlow.setAutoHeight(true);
            myTextFlow.disableStrokeFont();
            int width1;
            boolean aspectRatioCheckBoxSelected = aspectRatioCheckBox.isSelected();
            if (height < 10) {
                height = 10;
            }
            width1 = getWidthForSongVersePartText(aspectRatioCheckBoxSelected, size, width, height);
            songVersePartTextFlow.setAWidth(width);
            myTextFlow.setPrefHeight(size);
            myTextFlow.setTextAlignment(TextAlignment.CENTER);
            myTextFlow.setBackGroundColor();
            songVersePartTextFlow.setOpacity(minOpacity);
            StringBuilder selectedSongTitle = new StringBuilder();
            for (SongCollectionElement songCollectionElement : selectedSong.getSongCollectionElements()) {
                SongCollection songCollection = getSongCollectionFromRepository(songCollectionElement);
                if (songCollection == null || !songCollection.isShowInTitle()) {
                    continue;
                }
                selectedSongTitle.append(songCollection.getName());
                selectedSongTitle.append(" ").append(songCollectionElement.getOrdinalNumber());
                selectedSongTitle.append("\n");
            }
            selectedSongTitle.append(selectedSong.getTitle());
            songVersePartTextFlow.setText2(selectedSongTitle.toString(), width1, size);
            songListViewItems.add(songVersePartTextFlow);
            int songVerseIndex = 0;
            for (SongVerse songVerse : selectedSongVerseList) {
                addSongVerseParts(songVerse, size, width, height, songListViewItems, songVerseIndex++);
            }
            songLastSlide(size, songListViewItems, width, height);
            onSongSelectedEnd(songListViewItems);
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
    }

    private int getWidthForSongVersePartText(boolean aspectRatioCheckBoxSelected, int size, int width, int height) {
        int width1;
        if (aspectRatioCheckBoxSelected) {
            width1 = (size * width - getConstantSpaceForSongVerseList()) / height;
        } else {
            width1 = (int) songListView.getWidth() - getConstantSpaceForSongVerseList();
        }
        return width1;
    }

    private int getWidthForSongVersePartText2(int size, int width, int height) {
        return getWidthForSongVersePartText(aspectRatioCheckBox.isSelected(), size, width, height);
    }

    private void addSongVerseParts(SongVerse songVerse, int size, int width, int height, ObservableList<SongVersePartTextFlow> songListViewItems, int songVerseIndex) {
        List<String> texts = getTextByMaxLine(songVerse);
        int k = 0;
        boolean textsSplit = texts.size() > 1;
        for (String text : texts) {
            SongVerseProjectionDTO songVerseProjectionDTO = new SongVerseProjectionDTO();
            songVerseProjectionDTO.setTextsSplit(textsSplit);
            songVerseProjectionDTO.setFocusedTextIndex(k);
            songVerseProjectionDTO.setTexts(texts);
            songVerseProjectionDTO.setLastOne(texts.size() == k + 1);
            songVerseProjectionDTO.setFocusedText(text);
            songVerseProjectionDTO.setSongVerseIndex(songVerseIndex);
            addSongVersePart(songVerse, size, width, height, songListViewItems, songVerseProjectionDTO);
            ++k;
        }
    }

    private List<String> getTextByMaxLine(SongVerse songVerse) {
        try {
            List<String> partTexts = new ArrayList<>();
            String text = songVerse.getText();
            int maxLine = maxLineSpinner.getValue();
            if (maxLine < 1) {
                return addAndReturn(partTexts, text);
            }
            String[] lines = text.split("\n");
            int n = lines.length;
            if (n <= maxLine) {
                return addAndReturn(partTexts, text);
            }
            int count = (int) Math.ceil((double) n / maxLine);
            int[] lineCounts = new int[count];
            Arrays.fill(lineCounts, maxLine);
            int remained = n % maxLine;
            if (remained == 0) {
                remained = maxLine;
            }
            lineCounts[count - 1] = remained;
            int diff = maxLine - remained;
            int j = count - 2;
            while (diff > 0 && j >= 0 && lineCounts[j] - 2 >= lineCounts[count - 1]) {
                --lineCounts[j--];
                ++lineCounts[count - 1];
                --diff;
            }
            int k = 0;
            StringBuilder s = new StringBuilder();
            for (int i = 0; i < n; ++i) {
                if (k > 0) {
                    s.append("\n");
                }
                ++k;
                s.append(lines[i]);
                if (k == lineCounts[partTexts.size()] || i + 1 == n) {
                    partTexts.add(s.toString());
                    s = new StringBuilder();
                    k = 0;
                }
            }
            return partTexts;
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            List<String> partTexts = new ArrayList<>();
            return getOneSongVerseTextAsList(songVerse, partTexts);
        }
    }

    private static List<String> getOneSongVerseTextAsList(SongVerse songVerse, List<String> partTexts) {
        try {
            return addAndReturn(partTexts, songVerse.getText());
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    private static List<String> addAndReturn(List<String> partTexts, String text) {
        partTexts.add(text);
        return partTexts;
    }

    private void addSongVersePart(SongVerse songVerse, int size, int width, int height, ObservableList<SongVersePartTextFlow> songListViewItems, SongVerseProjectionDTO songVerseProjectionDTO) {
        SongVersePartTextFlow songVersePartTextFlow = new SongVersePartTextFlow();
        songVersePartTextFlow.setSongVerseProjectionDTO(songVerseProjectionDTO);
        songVersePartTextFlow.setSongVerse(songVerse);
        MyTextFlow myTextFlow = songVersePartTextFlow.getMyTextFlow();
        myTextFlow.setAutoHeight(true);
        myTextFlow.disableStrokeFont();
        int width1 = getWidthForSongVersePartText2(size, width, height);
        songVersePartTextFlow.setAWidth(width);
        myTextFlow.setPrefHeight(size);
        myTextFlow.setTextAlignment(TextAlignment.CENTER);
        myTextFlow.setBackGroundColor();
        songVersePartTextFlow.setOpacity(minOpacity);
        String text = songVerseProjectionDTO.getFocusedText();
        songVersePartTextFlow.setText2(getColorizedStringByLastSearchedText(text), width1, size);
        myTextFlow.setSecondText(songVerse.getSecondText());
        myTextFlow.setRawText(text);
        songListViewItems.add(songVersePartTextFlow);
    }

    private static int getConstantSpaceForSongVerseList() {
        return 32 + SongVersePartTextFlow.DESCRIPTION_BORDER_PANE_WIDTH + SongVersePartTextFlow.SPACING;
    }

    public static String getWholeWithFocusedText(List<String> texts, HashMap<Integer, Boolean> focusedIndices) {
        StringBuilder s = new StringBuilder();
        for (int i = 0; i < texts.size(); ++i) {
            appendColor(texts, i, s, focusedIndices.containsKey(i));
        }
        return s.toString();
    }

    private static void appendColor(List<String> texts, int i, StringBuilder s, boolean isFocused) {
        Color color = getFocusedColorBy(isFocused);
        if (!s.isEmpty()) {
            s.append("\n");
        }
        s.append(getColoredText(texts.get(i), color));
    }

    public static Color getFocusedColorBy(boolean isFocused) {
        Color color = Settings.getInstance().getColor();
        if (isFocused) {
            return color;
        }
        return color.darker();
    }

    private void songLastSlide(int size, ObservableList<SongVersePartTextFlow> songListViewItems, int width, int height) {
        SongVersePartTextFlow songVersePartTextFlow;
        songVersePartTextFlow = new SongVersePartTextFlow();
        songVersePartTextFlow.setAWidth(width);
        MyTextFlow myTextFlow = songVersePartTextFlow.getMyTextFlow();
        myTextFlow.setAutoHeight(true);
        myTextFlow.disableStrokeFont();
        int widthForSongVersePartText = getWidthForSongVersePartText2(size, width, height);
        songVersePartTextFlow.setText2(" ", widthForSongVersePartText, size / 3);
        myTextFlow.setPrefHeight(100);
        myTextFlow.setBackGroundColor();
        songVersePartTextFlow.setOpacity(minOpacity);
        songListViewItems.add(songVersePartTextFlow);
        songListView.getFocusModel().focus(0);
        songListView.scrollTo(0);
    }

    private void onSongSelectedEnd(ObservableList<SongVersePartTextFlow> songListViewItems) {
        if (activeSongVerseTime != null) {
            previousSongVerseTimeList.add(activeSongVerseTime);
        }
        int LAST_EMPTY_SLIDE = 1;
        int n = songListViewItems.size() - LAST_EMPTY_SLIDE;
        activeSongVerseTime = new SongVerseTime(selectedSong.getTitle(), n);
        activeSongVerseTime.setSong(selectedSong);
        previousSelectedVerseIndex = -1;
        double[] savedTimes = songVerseTimeService.getAverageTimes(selectedSong, n);
        for (int j = 0; j < songListViewItems.size(); ++j) {
            SongVersePartTextFlow songVersePartTextFlow = songListViewItems.get(j);
            double estimatedSeconds = getEstimatedSecondsForSongVersePart(songVersePartTextFlow);
            double time;
            if (savedTimes != null && savedTimes.length > j) {
                time = savedTimes[j];
                if (2 * estimatedSeconds < time) {
                    time = 2 * estimatedSeconds;
                } else if (time < estimatedSeconds / 2) {
                    time = estimatedSeconds / 2;
                }
            } else {
                time = estimatedSeconds;
            }
            songVersePartTextFlow.setEstimatedSeconds(time);
        }
        if (songRemoteListener != null) {
            songRemoteListener.onSongVerseListViewChanged(songListViewItems);
        }
        settingTheAuthor(selectedSong);
        settingTheVerseOrder(selectedSong);
        settingTheStarButtonBySong(selectedSong);
    }

    private double getEstimatedSecondsForSongVersePart(SongVersePartTextFlow songVersePartTextFlow) {
        String text = songVersePartTextFlow.getMyTextFlow().getRawText();
        text = text.replaceAll("[^" + vowels + "]", "");
        return text.length() * 0.72782;
    }

    private void initialization2() {
        try {
            ObservableList<SongVersePartTextFlow> songListViewItems = songListView.getItems();
            initListViewMenuItem();
            initSongCollectionListViewMenuItem();

            searchedSongListView.setOnKeyPressed(event -> {
                try {
                    if (event.getCode() == KeyCode.ENTER || event.getCode() == KeyCode.SPACE) {
                        songListView.requestFocus();
                        songListView.getFocusModel().focus(0);
                    }
                } catch (Exception e) {
                    LOG.error(e.getMessage(), e);
                }
            });
            songListView.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
                try {
                    KeyCode keyCode = event.getCode();
                    if (keyCode == KeyCode.DOWN) {
                        if (selectNextSongFromScheduleIfLastIndex()) {
                            event.consume();
                            return;
                        }
                    }
                    if (keyCode == KeyCode.DOWN || keyCode == KeyCode.UP || keyCode == KeyCode.HOME || keyCode == KeyCode.END || keyCode == KeyCode.PAGE_DOWN || keyCode == KeyCode.PAGE_UP) {
                        double x = System.currentTimeMillis() - timeStart;
                        if (x < 70) {
                            event.consume();
                            return;
                        }
                    } else if (keyCode == KeyCode.ENTER) {
                        mainController.setBlank(false);
                    } else if (keyCode.isDigitKey()) {
                        verseTextField.setText(getTextFromEvent(event));
                        verseTextField.requestFocus();
                        event.consume();
                    }
                    if (keyCode == KeyCode.PAGE_DOWN) {
                        setNext();
                        event.consume();
                    } else if (keyCode == KeyCode.PAGE_UP) {
                        setPrevious();
                        event.consume();
                    }
                } catch (Exception e) {
                    LOG.error(e.getMessage(), e);
                }
            });
            songListViewInitialization(songListViewItems);
            initializationEnd();
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
    }

    private void songListViewInitialization(ObservableList<SongVersePartTextFlow> songListViewItems) {
        MultipleSelectionModel<SongVersePartTextFlow> songListViewSelectionModel = songListView.getSelectionModel();
        songListViewSelectionModel.getSelectedIndices().addListener((ListChangeListener<Integer>) c -> {
            try {
                ObservableList<Integer> ob = songListViewSelectionModel.getSelectedIndices();
                synchronizedSelectVerseOrderListView(ob);
                if (ob.size() == 1) {
                    int selectedIndex = ob.get(0);
                    if (selectedIndex < 0) {
                        return;
                    }
                    if ((settings.isShareOnNetwork() || settings.isAllowRemote()) && projectionTextChangeListeners != null && !projectionScreenController.isLock()) {
                        try {
                            String secondText = getSecondText(selectedIndex - 1);
                            if (secondText != null && !secondText.isEmpty()) {
                                for (ProjectionTextChangeListener projectionTextChangeListener : projectionTextChangeListeners) {
                                    projectionTextChangeListener.onSetText(secondText, ProjectionType.SONG, null);
                                }
                            }
                        } catch (Exception e) {
                            LOG.error(e.getMessage(), e);
                        }
                    }
                    if (timeStart != 0 && previousSelectedVerseIndex >= 0 && previousSelectedVerseIndex < activeSongVerseTime.getVerseTimes().length) {
                        double x = System.currentTimeMillis() - timeStart;
                        x /= 1000;
                        activeSongVerseTime.getVerseTimes()[previousSelectedVerseIndex] = x;
                    }
                    SongVersePartTextFlow songVersePartTextFlow = songListViewItems.get(selectedIndex);
                    String text = songVersePartTextFlow.getMyTextFlow().getRawText();
                    text = getWithSecondText(songVersePartTextFlow, text);
                    setSongVerseProjection1(songVersePartTextFlow, text);
                    previousSelectedVerseIndex = selectedIndex;
                    if (selectedIndex + 1 == songListViewItems.size()) {
                        projectionScreenController.progressLineSetVisible(false);
                        projectionScreenController.setLineSize(0);
                    } else {
                        projectionScreenController.setLineSize((double) selectedIndex / (songListViewItems.size() - 2));
                    }
                } else if (ob.size() > 1) {
                    int lastIndex = setSongVerseProjectionBySelectedParts();
                    projectionScreenController.setLineSize((double) lastIndex / (songListViewItems.size() - 2));
                }
                if (recentController != null && !recentController.getLastItemText().equals(activeSongVerseTime.getSongTitle()) && !ob.isEmpty()) {
                    recentController.addRecentSong(activeSongVerseTime.getSongTitle(), ProjectionType.SONG);
                }
                opacityForSongVerse(ob);
            } catch (Exception e) {
                LOG.error(e.getMessage(), e);
            }
        });
    }

    private int setSongVerseProjectionBySelectedParts() {
        ObservableList<SongVersePartTextFlow> songListViewItems = songListView.getItems();
        ObservableList<Integer> selectedIndices = songListView.getSelectionModel().getSelectedIndices();
        if (selectedIndices.isEmpty()) {
            return 0;
        }
        StringBuilder tmpTextBuffer = new StringBuilder();
        List<SongVersePartTextFlow> songVersePartTextFlows = new ArrayList<>();
        SongVersePartTextFlow songVersePartTextFlow = songListViewItems.get(selectedIndices.get(0));
        songVersePartTextFlows.add(songVersePartTextFlow);
        tmpTextBuffer.append(songVersePartTextFlow.getMyTextFlow().getRawText());
        int lastIndex = 0;
        for (int i = 1; i < selectedIndices.size(); ++i) {
            Integer index = selectedIndices.get(i);
            if (index != songListViewItems.size() - 1) {
                songVersePartTextFlow = songListViewItems.get(index);
                songVersePartTextFlows.add(songVersePartTextFlow);
                tmpTextBuffer.append("\n").append(songVersePartTextFlow.getMyTextFlow().getRawText());
                if (lastIndex < index) {
                    lastIndex = index;
                }
            }
        }
        setSongVerseProjection(songVersePartTextFlows, tmpTextBuffer.toString());
        return lastIndex;
    }

    private void setSongVerseProjection1(SongVersePartTextFlow songVersePartTextFlow, String text) {
        List<SongVersePartTextFlow> songVersePartTextFlows = new ArrayList<>(1);
        songVersePartTextFlows.add(songVersePartTextFlow);
        setSongVerseProjection(songVersePartTextFlows, text);
    }

    private void setSongVerseProjection(List<SongVersePartTextFlow> songVersePartTextFlows, String text) {
        ProjectionDTO projectionDTO = getProjectionDTOForSongVersePart(songVersePartTextFlows);
        projectionScreenController.setText(text, ProjectionType.SONG, projectionDTO);
    }

    private static ProjectionDTO getProjectionDTOForSongVersePart(List<SongVersePartTextFlow> songVersePartTextFlows) {
        ProjectionDTO projectionDTO = new ProjectionDTO();
        projectionDTO.setSongVerseProjectionDTOS(getSongVerseProjectionDTOS(songVersePartTextFlows));
        return projectionDTO;
    }

    private static List<SongVerseProjectionDTO> getSongVerseProjectionDTOS(List<SongVersePartTextFlow> songVersePartTextFlows) {
        List<SongVerseProjectionDTO> songVerseProjectionDTOS = new ArrayList<>();
        for (SongVersePartTextFlow songVersePartTextFlow : songVersePartTextFlows) {
            SongVerseProjectionDTO songVerseProjectionDTO = songVersePartTextFlow.getSongVerseProjectionDTO();
            if (songVerseProjectionDTO == null) {
                continue;
            }
            songVerseProjectionDTOS.add(songVerseProjectionDTO);
        }
        return songVerseProjectionDTOS;
    }

    private void opacityForSongVerse(ObservableList<Integer> selectedIndices) {
        timeStart = System.currentTimeMillis();
        for (SongVersePartTextFlow songListViewItem : inCalculationSongVersePartTextFlows) {
            songListViewItem.onSelectionRemoved();
        }
        inCalculationSongVersePartTextFlows.clear();
        ObservableList<SongVersePartTextFlow> songListViewItems = songListView.getItems();
        for (Integer selectedIndex : selectedIndices) {
            if (selectedIndex != null && selectedIndex >= 0 && selectedIndex < songListViewItems.size()) {
                inCalculationSongVersePartTextFlows.add(songListViewItems.get(selectedIndex));
            }
        }
        if (opacityScheduler == null) {
            opacityScheduler = Executors.newSingleThreadScheduledExecutor();
            AtomicInteger logCount = new AtomicInteger();
            opacityScheduler.scheduleAtFixedRate(() -> {
                try {
                    if (!isBlank && timeStart != 0 && previousSelectedVerseIndex >= 0 && previousSelectedVerseIndex < activeSongVerseTime.getVerseTimes().length) {
                        double elapsedSeconds = System.currentTimeMillis() - timeStart;
                        elapsedSeconds /= 1000;
                        double estimatedSeconds = 0.0;
                        for (SongVersePartTextFlow songListViewItem : inCalculationSongVersePartTextFlows) {
                            estimatedSeconds += songListViewItem.getEstimatedSeconds();
                        }
                        double z = 1.0 - minOpacity;
                        final double v = z * elapsedSeconds / estimatedSeconds;
                        for (SongVersePartTextFlow songListViewItem : inCalculationSongVersePartTextFlows) {
                            double opacity = minOpacity + v;
                            if (opacity > 1) {
                                opacity = 1;
                                songListViewItem.showTimer();
                            }
                            double previousOpacity = songListViewItem.getOpacity();
                            if (Math.abs(previousOpacity - opacity) > 0.0005) {
                                songListViewItem.setOpacity(opacity);
                            }
                        }
                    }
                } catch (Exception e) {
                    if (logCount.getAndIncrement() < 10) {
                        LOG.error(e.getMessage(), e);
                    }
                }
            }, 0, 39, TimeUnit.MILLISECONDS);
        }
    }

    private void initializationEnd() {
        songListView.setOnMouseClicked(event -> {
            try {
                if (event.getClickCount() == 2) {
                    slideReSelect();
                }
            } catch (Exception e) {
                LOG.error(e.getMessage(), e);
            }
        });
        songCollectionListView.orientationProperty().set(Orientation.HORIZONTAL);
        songCollectionListView.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(SongCollection item, boolean empty) {
                try {
                    super.updateItem(item, empty);
                    if (empty || item == null || item.getName() == null) {
                        setText(null);
                    } else {
                        setText(item.getName());
                    }
                } catch (Exception e) {
                    LOG.error(e.getMessage(), e);
                }
            }
        });
        songCollectionListView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            try {
                if (newValue != null) {
                    selectedSongCollection = newValue;
                    if (pauseSortOrFilter) {
                        return;
                    }
                    sortSongs(selectedSongCollection.getSongs());
                    switch (lastSearching) {
                        case IN_SONG -> search(lastSearchText);
                        case IN_TITLE -> titleSearch(lastSearchText);
                        case IN_TITLE_START_WITH -> titleSearchStartWith(lastSearchText);
                    }
                }
            } catch (Exception e) {
                LOG.error(e.getMessage(), e);
            }
        });
        Settings settings = Settings.getInstance();
        SplitPane.setResizableWithParent(leftBorderPane, false);
        horizontalSplitPane.getDividers().get(0).setPosition(settings.getSongTabHorizontalSplitPaneDividerPosition());
        verticalSplitPane.setDividerPositions(settings.getSongTabVerticalSplitPaneDividerPosition());
        songHeightSlider.setValue(settings.getSongHeightSliderValue());
        songHeightSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            try {
                final int size = newValue.intValue();
                resizeSongList(size);
            } catch (Exception e) {
                LOG.error(e.getMessage(), e);
            }
        });
        aspectRatioCheckBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
            try {
                final int size = (int) songHeightSlider.getValue();
                resizeSongList(size);
            } catch (Exception e) {
                LOG.error(e.getMessage(), e);
            }
        });
        initializeNextButton();
        initializeProgressLineButton();
        initializeDownloadButton();
        initializeUploadButton();
        initializeVerseTextField();
        initializeSortComboBox();
        initializeLanguageComboBox();
        exportButton.setOnAction(event -> exportButtonOnAction());
        importButton.setOnAction(event -> importButtonOnAction());
        initializeShowVersionsButton();
        initializeDragListeners();
        initializeSongs();
        initializeVerseOrderList();
        hideOpenLPImportButton();
        initializeStarButton();
        initializeMaxLineSpinner();
    }

    private void initializeMaxLineSpinner() {
        try {
            SpinnerValueFactory.IntegerSpinnerValueFactory valueFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(0, Integer.MAX_VALUE);
            valueFactory.setValue(settings.getMaxLine());
            maxLineSpinner.setValueFactory(valueFactory);
            maxLineSpinner.valueProperty().addListener((observable, oldValue, newValue) -> {
                settings.setMaxLine(newValue);
                reAddSelectedSong();
            });
            TextFormatter<Integer> textFormatter = new TextFormatter<>(new IntegerFilter(valueFactory));
            TextField editor = maxLineSpinner.getEditor();
            editor.setTextFormatter(textFormatter);
            editor.textProperty().addListener((observable, oldValue, newValue) -> valueFactory.setValue(Integer.parseInt(newValue)));
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
    }

    private void reAddSelectedSong() {
        prepareSelectedSong(selectedSong);
    }

    private void checkForFavouriteInVersionGroup(List<Song> versionGroupSongs, Song selectedSong) {
        boolean hasDifferentFavourite = hasDifferentFavourite(versionGroupSongs, selectedSong);
        if (hasDifferentFavourite == versionsButtonPreviousStateWithStar) {
            return;
        }
        String path;
        versionsButtonPreviousStateWithStar = hasDifferentFavourite;
        if (versionsButtonPreviousStateWithStar) {
            path = "versions_star.png";
        } else {
            path = "versions.png";
        }
        InputStream resourceAsStream = getClass().getResourceAsStream("/icons/" + path);
        if (resourceAsStream != null) {
            ImageView imageView = new ImageView(new Image(resourceAsStream));
            imageView.setFitHeight(40.0);
            imageView.setFitWidth(27.0);
            imageView.setPickOnBounds(true);
            imageView.setPreserveRatio(true);
            showVersionsButton.setGraphic(imageView);
        }
    }

    private void settingTheStarButtonBySong(Song song) {
        if (!starButton.isVisible()) {
            return;
        }
        if (song.isFavourite() == starButtonPreviousStateWithStar) {
            return;
        }
        changeStarButtonImage(song);
    }

    private void changeStarButtonImage(Song song) {
        String startFile;
        starButtonPreviousStateWithStar = song.isFavourite();
        if (starButtonPreviousStateWithStar) {
            startFile = "star.png";
        } else {
            startFile = "star_border_black.png";
        }
        InputStream resourceAsStream = getClass().getResourceAsStream("/icons/" + startFile);
        if (resourceAsStream != null) {
            ImageView imageView = new ImageView(new Image(resourceAsStream));
            imageView.setFitHeight(20.0);
            imageView.setFitWidth(13.5);
            imageView.setPickOnBounds(true);
            imageView.setPreserveRatio(true);
            starButton.setGraphic(imageView);
        }
    }

    private void initializeStarButton() {
        starButton.setOnAction(event -> {
            if (selectedSong == null) {
                return;
            }
            selectedSong.setFavourite(!selectedSong.isFavourite());
            FavouriteSong favourite = selectedSong.getFavourite();
            favourite.setModifiedDate(new Date());
            favourite.setUploadedToServer(false);
            favourite.setFavouritePublished(favourite.isFavouriteNotPublished());
            FavouriteSongService favouriteSongService = ServiceManager.getFavouriteSongService();
            favouriteSongService.create(favourite);
            favouriteSongService.syncFavourites();
            changeStarButtonImage(selectedSong);
            searchedSongListView.refresh();
        });
        checkStarButtonVisibilityByLoggedIn();
    }

    private void checkStarButtonVisibilityByLoggedIn() {
        setStarButtonVisibility(UserService.getInstance().isLoggedIn());
    }

    private void setStarButtonVisibility(boolean b) {
        starButton.setManaged(b);
        starButton.setVisible(b);
    }

    private void addFavouriteStarImageForFavourite(Song song, ObservableList<Node> children, Text title) {
        if (!song.isFavourite()) {
            children.add(title);
            return;
        }
        InputStream resourceAsStream = getClass().getResourceAsStream("/icons/star.png");
        if (resourceAsStream != null) {
            ImageView imageView = new ImageView(new Image(resourceAsStream));
            double fitSize = 16.0;
            imageView.setFitHeight(fitSize);
            imageView.setFitWidth(fitSize);
            imageView.setPickOnBounds(true);
            imageView.setPreserveRatio(true);
            BorderPane borderPane = new BorderPane();
            borderPane.setLeft(title);
            borderPane.setRight(imageView);
            borderPane.setPrefWidth(Math.max(searchedSongListView.getWidth() - 25, 10));
            children.add(borderPane);
        }
    }

    private void searchAgain() {
        searchAgainBySearchOption(searchInTextCheckBox.isSelected());
    }

    private void searchAgainBySearchOption(Boolean newValue) {
        try {
            if (newValue) {
                search(lastSearchText);
            } else {
                titleSearch(lastSearchText);
            }
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
    }

    private void slideReSelect() {
        try {
            setSongVerseProjectionBySelectedParts();
            timeStart = System.currentTimeMillis();
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
    }

    private void hideOpenLPImportButton() {
        openLPImportButton.setVisible(false);
        openLPImportButton.setManaged(false);
    }

    private String getWithSecondText(SongVersePartTextFlow SongVersePartTextFlow, String text) {
        if (settings.isShowSongSecondText()) {
            String secondText = SongVersePartTextFlow.getMyTextFlow().getSecondText();
            if (secondText != null) {
                text += "\n" + getColoredText(secondText, settings.getSongSecondTextColor());
            }
        }
        return text;
    }

    public static String getColoredText(String text, Color color) {
        if (color == null) {
            return text;
        }
        String coloredText = "<color=\"" + color + "\">";
        coloredText += text;
        coloredText += "</color>";
        return coloredText;
    }

    private void synchronizedSelectVerseOrderListView(ObservableList<Integer> ob) {
        if (synchronizingVerseOrderListSelection) {
            return;
        }
        synchronizingVerseOrderListSelection = true;
        MultipleSelectionModel<SongVerse> selectionModel = verseOrderListView.getSelectionModel();
        int size = verseOrderListView.getItems().size();
        selectionModel.clearSelection();
        ObservableList<SongVersePartTextFlow> songListViewItems = songListView.getItems();
        for (int index : ob) {
            SongVersePartTextFlow songVersePartTextFlow = songListViewItems.get(index);
            if (songVersePartTextFlow == null) {
                continue;
            }
            SongVerseProjectionDTO songVerseProjectionDTO = songVersePartTextFlow.getSongVerseProjectionDTO();
            if (songVerseProjectionDTO == null) {
                continue;
            }
            Integer songVerseIndex = songVerseProjectionDTO.getSongVerseIndex();
            if (songVerseIndex == null) {
                continue;
            }
            int index1 = songVerseIndex;
            if (index1 >= 0 && index1 < size) {
                selectionModel.select(index1);
            }
        }
        synchronizingVerseOrderListSelection = false;
    }

    private void synchronizedSelectSongVerseListView(ObservableList<Integer> ob) {
        if (synchronizingVerseOrderListSelection) {
            return;
        }
        synchronizingVerseOrderListSelection = true;
        MultipleSelectionModel<SongVersePartTextFlow> selectionModel = songListView.getSelectionModel();
        selectionModel.clearSelection();
        boolean first = true;
        for (int index : ob) {
            List<SongVersePartTextFlow> songVersePartTextFlows = getSongVersePartTextFlowByIndex(index);
            for (SongVersePartTextFlow songVersePartTextFlow : songVersePartTextFlows) {
                if (first) {
                    // TODO: scroll if not visible
                    songListView.scrollTo(songVersePartTextFlow);
                    first = false;
                }
                selectionModel.select(songVersePartTextFlow);
            }
        }
        synchronizingVerseOrderListSelection = false;
    }

    private List<SongVersePartTextFlow> getSongVersePartTextFlowByIndex(int verseIndex) {
        List<SongVersePartTextFlow> songVersePartTextFlows = new ArrayList<>();
        for (SongVersePartTextFlow songVersePartTextFlow : songListView.getItems()) {
            SongVerseProjectionDTO songVerseProjectionDTO = songVersePartTextFlow.getSongVerseProjectionDTO();
            if (songVerseProjectionDTO == null) {
                continue;
            }
            Integer songVerseIndex = songVerseProjectionDTO.getSongVerseIndex();
            if (songVerseIndex == null) {
                continue;
            }
            if (songVerseIndex == verseIndex) {
                songVersePartTextFlows.add(songVersePartTextFlow);
            }
        }
        return songVersePartTextFlows;
    }

    private void initializeVerseOrderList() {
        verseOrderListView.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            try {
                final KeyCode keyCode = event.getCode();
                if (keyCode == KeyCode.PAGE_DOWN) {
                    setNext();
                    event.consume();
                } else if (keyCode == KeyCode.PAGE_UP) {
                    setPrevious();
                    event.consume();
                }
            } catch (Exception e) {
                LOG.error(e.getMessage(), e);
            }
        });
        verseOrderListView.orientationProperty().set(Orientation.HORIZONTAL);
        verseOrderListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        final ObservableList<Integer> selectedIndices = verseOrderListView.getSelectionModel().getSelectedIndices();
        selectedIndices.addListener((ListChangeListener<Integer>) c -> synchronizedSelectSongVerseListView(selectedIndices));
        verseOrderListView.setCellFactory(new Callback<>() {
            @Override
            public ListCell<SongVerse> call(ListView<SongVerse> listView) {

                return new ListCell<>() {
                    final Tooltip tooltip = new Tooltip();

                    @Override
                    protected void updateItem(SongVerse songVerse, boolean empty) {
                        try {
                            super.updateItem(songVerse, empty);
                            if (songVerse != null && !empty) {
                                setText(songVerse.getSectionTypeStringWithCount());
                                tooltip.setText(songVerse.getText());
                                setTooltip(tooltip);
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
    }

    private void settingTheVerseOrder(Song selectedSong) {
        ObservableList<SongVerse> verseOrderListViewItems = verseOrderListView.getItems();
        verseOrderListViewItems.clear();
        List<SongVerse> songVersesByVerseOrder = selectedSong.getSongVersesByVerseOrder();
        verseOrderListViewItems.addAll(songVersesByVerseOrder);
    }

    private void settingTheAuthor(Song selectedSong) {
        String authorName = selectedSong.getAuthor();
        boolean visibleAuthor = authorName != null && !authorName.trim().isEmpty();
        authorBox.setVisible(visibleAuthor);
        authorBox.setManaged(visibleAuthor);
        if (visibleAuthor) {
            authorTextField.setText(authorName);
        }
    }

    private void initializeDragListeners() {
        searchedSongListView.setOnDragDetected(event -> {
            Dragboard dragboard = searchedSongListView.startDragAndDrop(TransferMode.LINK, TransferMode.COPY);
            ClipboardContent content = new ClipboardContent();
            Song song = searchedSongListView.getSelectionModel().getSelectedItem().getSong();
            String uuid = song.getUuid();
            String s;
            if (uuid == null) {
                s = prefix + song.getId();
                content.putString(s);
            } else {
                s = link4 + "/song/" + uuid;
                content.putUrl(s);
            }
            dragboard.setContent(content);
            rightBorderPane.setOpacity(0.8);
        });
        searchedSongListView.setOnDragDone(event -> rightBorderPane.setOpacity(1.0));
        scheduleListView.setOnDragEntered(dragEvent -> {
            if (getSongsFromDragBoard(dragEvent) != null) {
                scheduleListView.setBlendMode(BlendMode.DARKEN);
            }
        });

        scheduleListView.setOnDragExited(dragEvent -> {
            if (getSongsFromDragBoard(dragEvent) != null) {
                scheduleListView.setBlendMode(null);
            }
        });

        scheduleListView.setOnDragOver(dragEvent -> {
            if (getSongsFromDragBoard(dragEvent) != null) {
                dragEvent.acceptTransferModes(TransferMode.COPY, TransferMode.LINK);
            }
        });

        scheduleListView.setOnDragDropped(dragEvent -> {
            List<Song> songsFromDragBoard = getSongsFromDragBoard(dragEvent);
            if (songsFromDragBoard != null) {
                try {
                    for (Song songFromDragBoard : songsFromDragBoard) {
                        try {
                            setSongCollection(songFromDragBoard);
                            scheduleController.addSong(songFromDragBoard);
                        } catch (Exception e) {
                            LOG.error(e.getMessage(), e);
                        }
                    }
                } catch (Exception e) {
                    LOG.error(e.getMessage(), e);
                }
                dragEvent.setDropCompleted(true);
            }
        });
    }

    private void setSongCollection(Song song) {
        List<SongCollectionElement> songCollectionElements = ServiceManager.getSongCollectionElementService().findBySong(song);
        if (songCollectionElements != null && !songCollectionElements.isEmpty()) {
            song.setSongCollectionElements(songCollectionElements);
            for (SongCollectionElement songCollectionElement : songCollectionElements) {
                song.addToSongCollections(songCollectionElement.getSongCollection());
            }
        }
    }

    private List<Song> getSongsFromDragBoard(DragEvent dragEvent) {
        Dragboard dragboard = dragEvent.getDragboard();
        String url = dragboard.getUrl();
        if (url != null) {
            ArrayList<Song> songs = new ArrayList<>();
            String queuePrefix = "queue?ids=";
            if (url.contains(queuePrefix)) {
                String prefix = "/";
                url = url.replace(link + prefix, "");
                url = url.replace(link2 + prefix, "");
                url = url.replace(link3 + prefix, "");
                url = url.replace(link4 + prefix, "");
                url = url.replaceFirst("queue\\?ids=", "");
                url = getFirstBy(url);
                String[] uuids = url.split(",");
                for (String uuid : uuids) {
                    addToSongByUuid(songs, uuid);
                }
            } else {
                String songPrefix = "/song/";
                url = url.replace(link + songPrefix, "");
                url = url.replace(link2 + songPrefix, "");
                url = url.replace(link3 + songPrefix, "");
                url = url.replace(link4 + songPrefix, "");
                url = getFirstBy(url);
                addToSongByUuid(songs, url);
            }
            if (songs.isEmpty()) {
                return null;
            }
            return songs;
        }
        String string = dragboard.getString();
        if (string != null && string.startsWith(prefix)) {
            Song byId = songService.findById(Long.parseLong(string.replace(prefix, "")));
            ArrayList<Song> songs = new ArrayList<>();
            if (byId != null) {
                songs.add(byId);
                return songs;
            }
        }
        return null;
    }

    private String getFirstBy(String url) {
        String[] split = url.split("\\?");
        if (split.length > 0) {
            return split[0];
        }
        return "";
    }

    private void addToSongByUuid(ArrayList<Song> songs, String uuid) {
        Song byUuid = songService.findByUuid(uuid);
        if (byUuid != null) {
            songs.add(byUuid);
        } else {
            final Song[] song = {null};
            Thread thread = getThreadForSongFromApi(uuid, song);
            try {
                thread.join(500);
            } catch (InterruptedException ignored) {
            }
            if (song[0] != null) {
                songs.add(song[0]);
            }
        }
    }

    private Thread getThreadForSongFromApi(String uuid, Song[] song) {
        Thread thread = new Thread(() -> {
            try {
                SongApiBean songApiBean = new SongApiBean();
                song[0] = songApiBean.getSongByUuid(uuid);
                if (song[0] != null) {
                    song[0].setDownloadedSeparately(true);
                    ServiceManager.getSongService().create(song[0]);
                    addSongToLanguagesComboBox(song[0]);
                }
            } catch (Exception e) {
                LOG.error(e.getMessage(), e);
            }
        });
        thread.start();
        return thread;
    }

    private void addSongToLanguagesComboBox(Song song) {
        Language language = song.getLanguage();
        if (language != null) {
            Language languageInComboBox = findLanguageInComboBox(language);
            if (languageInComboBox != null) {
                List<Song> comboBoxSongs = languageInComboBox.getSongs();
                comboBoxSongs.add(song);
                sortSongs(comboBoxSongs);
            }
        }
    }

    private Language findLanguageInComboBox(Language language) {
        for (Language aLanguage : languageComboBox.getItems()) {
            if (aLanguage.getId().equals(language.getId())) {
                return aLanguage;
            }
        }
        return null;
    }

    private void initializeShowVersionsButton() {
        showVersionsButton.setVisible(false);
        showVersionsButton.setOnAction(event -> {
            String versionGroup = selectedSong.getVersionGroup();
            String uuid = selectedSong.getUuid();
            if (versionGroup == null) {
                versionGroup = uuid;
            }
            List<Song> allByVersionGroup = songService.findAllByVersionGroup(versionGroup);
            int initialCapacity = allByVersionGroup.size();
            final List<Song> songs = new ArrayList<>(initialCapacity);
            HashMap<String, Song> hashMap = new HashMap<>(initialCapacity);
            HashMap<String, Song> songHashMap = new HashMap<>(initialCapacity);
            for (Song song : allByVersionGroup) {
                hashMap.put(song.getUuid(), song);
                songHashMap.put(song.getUuid(), song);
            }
            List<SongCollection> songCollections = ServiceManager.getSongCollectionService().findAll();
            for (SongCollection songCollection : songCollections) {
                for (SongCollectionElement songCollectionElement : songCollection.getSongCollectionElements()) {
                    String songUuid = songCollectionElement.getSongUuid();
                    if (songHashMap.containsKey(songUuid)) {
                        Song song = songHashMap.get(songUuid);
                        song.addToSongCollections(songCollection);
                        song.addToSongCollectionElements(songCollectionElement);
                        songCollectionElement.setSong(song);
                        if (hashMap.containsKey(songUuid)) {
                            songs.add(song);
                            hashMap.remove(songUuid);
                        }
                    }
                }
            }
            songs.addAll(hashMap.values());
            sortSongsByRelevanceOrder(songs);
            ObservableList<SearchedSong> items = searchedSongListView.getItems();
            items.clear();
            for (Song song : songs) {
                SearchedSong searchedSong = new SearchedSong(song);
                items.add(searchedSong);
            }
        });
    }

    public boolean selectNextSongFromScheduleIfLastIndex() {
        if (songListView.getSelectionModel().getSelectedIndex() == songListView.getItems().size() - 1) {
            int nextIndex = scheduleController.getSelectedIndex() + 1;
            scheduleListView.getSelectionModel().select(nextIndex);
            return true;
        }
        return false;
    }

    private String getColorizedStringByLastSearchedText(String text) {
        if (!searchInTextCheckBox.isSelected()) {
            return text;
        }
        StringBuilder s = new StringBuilder();
        char[] lastSearch = stripAccents(lastSearchText.toLowerCase()).toCharArray();
        if (lastSearch.length == 0) {
            return text;
        }
        int matchCount = 0;
        char[] chars = text.toCharArray();
        StringBuilder tmp = new StringBuilder();
        int whitespaceCount = 0;
        for (int i = 0; i < chars.length; ++i) {
            char c = chars[i];
            String s1 = stripAccents((c + "").toLowerCase());
            if (!s1.isEmpty()) {
                if (s1.charAt(0) == lastSearch[matchCount]) {
                    if (matchCount == 0) {
                        whitespaceCount = 0;
                    }
                    ++matchCount;
                    if (matchCount == lastSearch.length) {
                        matchCount = 0;
                        s.append("<color=\"0xFFC600FF\">").append(tmp).append(c).append("</color>");
                        tmp = new StringBuilder();
                        continue;
                    }
                } else {
                    if (matchCount > 0) {
                        i -= matchCount + whitespaceCount;
                        s.append(chars[i]);
                        matchCount = 0;
                        tmp = new StringBuilder();
                        continue;
                    }
                }
            } else {
                ++whitespaceCount;
            }
            if (matchCount == 0) {
                s.append(c);
            } else {
                tmp.append(c);
            }
        }
        return s.append(tmp).toString();
    }

    private void initializeSortComboBox() {
        try {
            sortComboBox.getItems().addAll(OrderMethod.RELEVANCE, OrderMethod.ASCENDING_BY_TITLE, OrderMethod.DESCENDING_BY_TITLE, OrderMethod.BY_MODIFIED_DATE, OrderMethod.BY_PUBLISHED, OrderMethod.BY_COLLECTION);
            SingleSelectionModel<OrderMethod> selectionModel = sortComboBox.getSelectionModel();
            selectionModel.selectFirst();
            selectionModel.select(settings.getSongOrderMethod());
            selectionModel.selectedItemProperty().addListener((observable, oldValue, newValue) -> {
                addSongCollections();
                sortSongs(songs);
                addAllSongs();
                settings.setSongOrderMethod(newValue);
            });
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
    }

    public void initializeLanguageComboBox() {
        try {
            LanguageService languageService = ServiceManager.getLanguageService();
            List<Language> languages = languageService.findAll();
            languageService.setSongsSize(languages);
            if (countSelectedLanguages(languages) < 2) {
                languageComboBox.setVisible(false);
                languageComboBox.setManaged(false);
            } else {
                languageComboBox.setVisible(true);
                languageComboBox.setManaged(true);
            }
            languages.sort((o1, o2) -> Long.compare(o2.getSongsSize(songService), o1.getSongsSize(songService)));
            languageComboBox.getItems().clear();
            for (Language language : languages) {
                if (language.getCountedSongsSize() > 0) {
                    languageComboBox.getItems().add(language);
                } else {
                    break;
                }
            }
            if (settings.isCheckLanguages() && languageComboBox.getItems().size() > 1) {
                Language all = new Language();
                all.setEnglishName("All");
                all.setNativeName("All");
                List<Song> songs = ServiceManager.getSongService().findAll();
                List<Song> noLanguageSongs = new ArrayList<>();
                for (Song song : songs) {
                    if (song.getLanguage() == null) {
                        System.out.println("song = " + song.getTitle());
                        noLanguageSongs.add(song);
                    }
                }
                all.setSongs(songs);
                if (!noLanguageSongs.isEmpty()) {
                    setLanguagesForSongs(noLanguageSongs);
                }
                languageComboBox.getItems().add(0, all);
            }
            SingleSelectionModel<Language> selectionModel = languageComboBox.getSelectionModel();
            Language songSelectedLanguage = settings.getSongSelectedLanguage();
            for (Language language : languages) {
                if (songSelectedLanguage.getUuid().equals(language.getUuid())) {
                    selectionModel.select(language);
                    settings.setSongSelectedLanguage(language);
                    break;
                }
            }
            selectionModel.selectedItemProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue != null) {
                    settings.setSongSelectedLanguage(newValue);
                }
                readSongs();
                addAllSongs();
                addSongCollections();
            });
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
    }

    private void setLanguagesForSongs(List<Song> songs) {
        List<Language> languages = ServiceManager.getLanguageService().findAll();
        List<Song> allWithLanguage = songService.findAll();
        HashMap<String, Song> songHashMap = new HashMap<>();
        for (Song song : allWithLanguage) {
            songHashMap.put(song.getUuid(), song);
        }

        Map<Language, Collection<String>> languageMap = new HashMap<>();
        for (Language language : languages) {
            TreeSet<String> value = new TreeSet<>();
            languageMap.put(language, value);
            for (Song song : language.getSongs()) {
                addWordsInCollection(song, value);
            }
        }
        for (Song song : songs) {
            Song song1 = songHashMap.get(song.getUuid());
            if (song1 != null && song1.getLanguage() != null) {
                Language language = song1.getLanguage();
                Collection<String> words = languageMap.get(language);
                addWordsInCollection(song1, words);
            } else {
                if (song.isDeleted()) {
                    continue;
                }
                List<String> words = new ArrayList<>();
                addWordsInCollection(song, words);
                Map<Language, ContainsResult> countMap = new HashMap<>(languages.size());
                for (Language language1 : languages) {
                    Collection<String> wordsByLanguage = languageMap.get(language1);
                    int count = 0;
                    Integer wordCount = 0;
                    for (String word : words) {
                        if (wordsByLanguage.contains(word)) {
                            ++count;
                        }
                        ++wordCount;
                    }
                    ContainsResult containsResult = new ContainsResult();
                    containsResult.setCount(count);
                    containsResult.setWordCount(wordCount);
                    countMap.put(language1, containsResult);
                }
                Set<Map.Entry<Language, ContainsResult>> entries = countMap.entrySet();
                Map.Entry<Language, ContainsResult> max = new AbstractMap.SimpleEntry<>(null, new ContainsResult());
                for (Map.Entry<Language, ContainsResult> entry : entries) {
                    if (entry.getValue().getRatio() > max.getValue().getRatio()) {
                        max = entry;
                    }
                }
                if (max.getKey() != null) {
                    song.setLanguage(max.getKey());
                    songService.create(song);
                    addWordsInCollection(song, languageMap.get(song.getLanguage()));
                } else {
                    System.out.println(song.getTitle());
                }
            }
        }
    }

    private int countSelectedLanguages(List<Language> languages) {
        int count = 0;
        SongService songService = ServiceManager.getSongService();
        for (Language language : languages) {
            if (language.getSongsSize(songService) > 0) {
                ++count;
            }
        }
        return count;
    }

    private void initializeVerseTextField() {
        verseTextField.addEventFilter(KeyEvent.KEY_TYPED, numeric_Validation());
        verseTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            try {
                int x = Integer.parseInt(newValue.trim());
                int size = songListView.getItems().size();
                if (x >= 0 && x < size && x * 10 > size - 1 || x == 0) {
                    songListView.getSelectionModel().clearAndSelect(x);
                    songListView.scrollTo(x);
                    verseTextField.setText("");
                } else if (x >= size) {
                    verseTextField.setText("");
                }
            } catch (NumberFormatException ignored) {
            }
        });
        verseTextField.setOnKeyPressed(event -> {
            mainController.globalKeyEventHandler().handle(event);
            if (event.isConsumed()) {
                return;
            }
            KeyCode keyCode = event.getCode();
            if (keyCode.equals(KeyCode.ENTER)) {
                selectByVerseTextFieldNumber();
            } else if (keyCode.isArrowKey()) {
                songListView.requestFocus();
                songListView.fireEvent(event);
            }
        });
    }

    private void selectByVerseTextFieldNumber() {
        try {
            int x = Integer.parseInt(verseTextField.getText().trim());
            int size = songListView.getItems().size();
            if (x >= 0 && x < size) {
                songListView.getSelectionModel().clearAndSelect(x);
                songListView.scrollTo(x);
                verseTextField.setText("");
            }
        } catch (NumberFormatException ignored) {
        }
    }

    private EventHandler<KeyEvent> numeric_Validation() {
        return getKeyEventEventHandler(LOG);
    }

    private String getSecondText(int selectedIndex) {
        try {
            if (selectedIndex < 0) {
                return null;
            }
            if (selectedIndex >= selectedSongVerseList.size()) {
                return null;
            }
            SongVerse songVerse = selectedSongVerseList.get(selectedIndex);
            return songVerse.getSecondText();
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            return null;
        }
    }

    private void initializeDownloadButton() {
        try {
            downloadButton.setOnAction(event -> {
                try {
                    FXMLLoader loader = new FXMLLoader();
                    loader.setLocation(MainDesktop.class.getResource("/view/language/DownloadLanguages.fxml"));
                    loader.setResources(Settings.getInstance().getResourceBundle());
                    Pane root = loader.load();
                    DownloadLanguagesController downloadLanguagesController = loader.getController();
                    Scene scene = new Scene(root);
                    URL resource = getClass().getResource("/view/" + settings.getSceneStyleFile());
                    if (resource != null) {
                        scene.getStylesheets().add(resource.toExternalForm());
                    }
                    Stage stage = getAStage(getClass());
                    stage.setScene(scene);
                    stage.setTitle(Settings.getInstance().getResourceBundle().getString("Download languages"));
                    stage.show();
                    downloadLanguagesController.setSongController(this);
                    downloadLanguagesController.setStage(stage);
                    downloadOldVersionGroups();
                } catch (Exception e) {
                    LOG.error(e.getMessage(), e);
                }
            });
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
    }

    private void downloadOldVersionGroups() {
        // This will be deleted
        if (getOldVersion() == 0) {
            Thread thread = new Thread(() -> {
                SongApiBean songApi = new SongApiBean();
                List<Language> languages = ServiceManager.getLanguageService().findAll();
                for (Language language : languages) {
                    if (!language.getSongs().isEmpty()) {
                        final List<Song> songApiSongs = songApi.getSongsByLanguageAndAfterModifiedDate(language, 1524234911591L);
                        for (Song song : songApiSongs) {
                            if (song.getVersionGroup() != null) {
                                Song byUuid = songService.findByUuid(song.getUuid());
                                if (byUuid != null) {
                                    byUuid.setVersionGroup(song.getVersionGroup());
                                    byUuid.setVerses(byUuid.getVerses());
                                    songService.update(byUuid);
                                }
                            }
                        }
                    }
                }
            });
            thread.start();
            try (FileOutputStream stream = new FileOutputStream("data/songs.version"); BufferedWriter br = new BufferedWriter(new OutputStreamWriter(stream, StandardCharsets.UTF_8))) {
                br.write("1\n");
            } catch (IOException e) {
                LOG.error(e.getMessage(), e);
            }
        }
    }

    private int getOldVersion() {
        try (FileInputStream stream = new FileInputStream("data/songs.version"); BufferedReader br = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            return Integer.parseInt(br.readLine());
        } catch (FileNotFoundException | NumberFormatException ignored) {
            List<Song> all = songService.findAll();
            if (all.isEmpty()) {
                return 1;
            }
            for (Song song : all) {
                if (song.getVersionGroup() != null) {
                    return 1;
                }
            }
            return 0;
        } catch (IOException e) {
            LOG.error(e.getMessage(), e);
        }
        return 0;
    }

    private void initializeUploadButton() {
        try {
            uploadButton.setOnAction(event -> {
                try {
                    FXMLLoader loader = new FXMLLoader();
                    loader.setLocation(MainDesktop.class.getResource("/view/song/UploadSongs.fxml"));
                    loader.setResources(Settings.getInstance().getResourceBundle());
                    Pane root = loader.load();
                    Scene scene = new Scene(root);
                    URL resource = getClass().getResource("/view/" + settings.getSceneStyleFile());
                    if (resource != null) {
                        scene.getStylesheets().add(resource.toExternalForm());
                    }
                    Stage stage = getAStage(getClass());
                    stage.setScene(scene);
                    stage.setTitle(Settings.getInstance().getResourceBundle().getString("Upload songs"));
                    stage.show();
                } catch (Exception e) {
                    LOG.error(e.getMessage(), e);
                }
            });
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
    }

    private void initializeProgressLineButton() {
        try {
            progressLineToggleButton.setOnAction(event -> settings.setShowProgressLine(progressLineToggleButton.isSelected()));
            progressLineToggleButton.setSelected(true);
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
    }

    private void initializeNextButton() {
        try {
            nextButton.setOnAction(event -> {
                try {
                    final MultipleSelectionModel<SongVersePartTextFlow> selectionModel = songListView.getSelectionModel();
                    final int selectedIndex = selectionModel.getSelectedIndex();
                    if (selectedIndex >= 0) {
                        final int index = selectedIndex + 1;
                        if (songListView.getItems().size() > index) {
                            selectionModel.clearAndSelect(index);
                            songListView.scrollTo(index);
                        }
                    } else {
                        if (!songListView.getItems().isEmpty()) {
                            selectionModel.clearAndSelect(0);
                            songListView.scrollTo(0);
                        }
                    }
                } catch (Exception e) {
                    LOG.error(e.getMessage(), e);
                }
            });
            nextButton.addEventHandler(KeyEvent.KEY_PRESSED, new NextButtonEventHandler(nextButton, LOG) {
            });
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
    }

    private void resizeSongList(int size) {
        try {
            Scene scene = projectionScreenController.getScene();
            int width;
            int height;
            if (scene != null) {
                width = (int) scene.getWidth();
                height = (int) scene.getHeight();
            } else {
                width = 16;
                height = 9;
            }
            if (height < 10) {
                height = 10;
            }
            int width1 = getWidthForSongVersePartText2(size, width, height);
            for (SongVersePartTextFlow SongVersePartTextFlow : songListView.getItems()) {
                SongVersePartTextFlow.getMyTextFlow().setSize(width1, size);
            }
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
    }

    public void setProjectionScreenController(ProjectionScreenController projectionScreenController) {
        try {
            this.projectionScreenController = projectionScreenController;
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
    }

    public void setRecentController(RecentController recentController) {
        try {
            this.recentController = recentController;
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
    }

    public void setScheduleController(ScheduleController scheduleController) {
        try {
            this.scheduleController = scheduleController;
            scheduleController.setListView(scheduleListView);
            scheduleController.initialize();
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
    }

    private boolean contains(String a, String b) {
        try {
            a = stripAccents(a);
            b = stripAccents(b);
            a = a.replace("[", "");
            a = a.replace("]", "");
            b = b.replace("[", "");
            b = b.replace("]", "");
            return a.toLowerCase().contains(b.toLowerCase().trim());
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
        return false;
    }

    private void search(String text) {
        try {
            lastSearching = LastSearching.IN_SONG;
            List<Song> songs = getFilteredSongs();
            if (text.trim().isEmpty()) {
                ObservableList<SearchedSong> items = searchedSongListView.getItems();
                items.clear();
                for (Song song : songs) {
                    SearchedSong searchedSong = new SearchedSong(song);
                    items.add(searchedSong);
                }
                lastSearchText = text;
            } else {
                Thread thread = new Thread(() -> {
                    try {
                        String searchText = text;
                        String[] split = searchText.split(" ");
                        String firstWord = split[0];
                        String remainingText = "";
                        try {
                            remainingText = searchText.substring(firstWord.length() + 1);
                        } catch (Exception ignored) {
                        }
                        remainingText = stripAccents(remainingText);
                        searchText = stripAccents(searchText);
                        searchText = searchText.toLowerCase();
                        ArrayList<Integer> tmpSearchISong = new ArrayList<>();
                        ArrayList<String> tmpSearchIFoundAtLine = new ArrayList<>();
                        for (int i = 0; i < songs.size(); ++i) {
                            boolean contains = false;
                            String line = "";
                            Song song = songs.get(i);
                            for (SongCollectionElement songCollectionElement : song.getSongCollectionElements()) {
                                if (songCollectionElement.getOrdinalNumberLowerCase().contains(firstWord) && !(remainingText.isEmpty() || song.getStrippedTitle().contains(remainingText))) {
                                    System.out.println("remainingText = " + remainingText);
                                }
                                if (songCollectionElement.getOrdinalNumberLowerCase().contains(firstWord) && (remainingText.isEmpty() || song.getStrippedTitle().contains(remainingText))) {
                                    contains = true;
                                }
                            }
                            if (song.getStrippedTitle().contains(searchText)) {
                                contains = true;
                            } else {
                                final List<SongVerse> verses = song.getVerses();
                                for (SongVerse verse : verses) {
                                    if (verse.getStrippedText().contains(searchText)) {
                                        contains = true;
                                        line = "\n";
                                        final int k = 35;
                                        final String text1 = verse.getText();
                                        if (text1.length() > k) {
                                            line += text1.substring(0, k).replaceAll("\\n", " ") + "...";
                                        } else {
                                            line += text1;
                                        }
                                        break;
                                    }
                                }
                            }
                            if (contains) {
                                tmpSearchISong.add(i);
                                tmpSearchIFoundAtLine.add(line);
                            }
                        }
                        Platform.runLater(() -> {
                            try {
                                ObservableList<SearchedSong> items = searchedSongListView.getItems();
                                items.clear();
                                for (int i = 0; i < tmpSearchISong.size(); ++i) {
                                    SearchedSong searchedSong = new SearchedSong(songs.get(tmpSearchISong.get(i)));
                                    searchedSong.setFoundAtVerse(tmpSearchIFoundAtLine.get(i));
                                    items.add(searchedSong);
                                }
                                selectIfJustOne();
                            } catch (Exception e) {
                                LOG.error(e.getMessage(), e);
                            } finally {
                                lastSearchText = text;
                            }
                        });
                    } catch (Exception e) {
                        LOG.error(e.getMessage(), e);
                    }
                });
                thread.start();
            }
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
    }

    private List<Song> getFilteredSongs() {
        List<Song> songs = selectedSongCollection.getSongs();
        setFavouriteSongs(songs);
        songs = filterSongsByFavourites(songs);
        return songs;
    }

    private List<FavouriteSong> getFavouriteSongs() {
        if (favouriteSongs == null) {
            favouriteSongs = ServiceManager.getFavouriteSongService().findAll();
        }
        return favouriteSongs;
    }

    private void setFavouriteSongs(List<Song> songs) {
        List<FavouriteSong> favouriteSongs = getFavouriteSongs();
        setFavouritesForSongs(songs, favouriteSongs);
    }

    private List<Song> filterSongsByFavourites(List<Song> songs) {
        if (!favoritesCheckBox.isSelected()) {
            return songs;
        }
        List<Song> filtered = new ArrayList<>();
        for (Song song : songs) {
            if (song.isFavourite()) {
                filtered.add(song);
            }
        }
        return filtered;
    }

    private HashMap<String, Song> getStringSongHashMap(List<Song> songs) {
        HashMap<String, Song> hashMap = new HashMap<>(songs.size());
        for (Song song : songs) {
            hashMap.put(song.getUuid(), song);
        }
        return hashMap;
    }

    private void setFavouritesForSongs(List<Song> songs, List<FavouriteSong> favouriteSongs) {
        HashMap<String, Song> hashMap = getStringSongHashMap(songs);
        for (FavouriteSong favouriteSong : favouriteSongs) {
            if (favouriteSong.getSong() != null) {
                String songUuid = favouriteSong.getSong().getUuid();
                if (hashMap.containsKey(songUuid)) {
                    Song song = hashMap.get(songUuid);
                    song.setFavourite(favouriteSong);
                }
            }
        }
    }

    private void titleSearch(String text) {
        try {
            text = text.trim();
            lastSearching = LastSearching.IN_TITLE;
            lastSearchText = text;
            ObservableList<SearchedSong> searchedSongListViewItems = searchedSongListView.getItems();
            searchedSongListViewItems.clear();
            String[] split = text.split(" ");
            String firstWord = split[0];
            String ordinalNumber = firstWord;
            String collectionName = "";
            if (!firstWord.matches("^[0-9]+.*")) {
                char[] chars = firstWord.toCharArray();
                int i;
                for (i = 0; i < chars.length; ++i) {
                    if (chars[i] >= '0' && chars[i] <= '9') {
                        break;
                    }
                }
                collectionName = stripAccents(firstWord.substring(0, i).toLowerCase());
                ordinalNumber = firstWord.substring(i);
            }
            int ordinalNumberInt = Integer.MIN_VALUE;
            try {
                ordinalNumberInt = Integer.parseInt(ordinalNumber);
            } catch (Exception ignored) {
            }
            String remainingText = "";
            try {
                remainingText = text.substring(firstWord.length() + 1);
            } catch (Exception ignored) {
            }
            remainingText = stripAccents(remainingText);
            text = stripAccents(text);
            List<Song> songs = getFilteredSongs();
            boolean wasOrdinalNumber = false;
            for (Song song : songs) {
                boolean contains = false;
                for (SongCollectionElement songCollectionElement : song.getSongCollectionElements()) {
                    boolean containsInCollectionName = isContainsInCollectionName(collectionName, songCollectionElement) || collectionName.isEmpty();
                    String number = songCollectionElement.getOrdinalNumberLowerCase();
                    boolean equals = number.equals(ordinalNumber) && !number.isEmpty();
                    boolean contains2 = number.contains(ordinalNumber) || ordinalNumberInt == songCollectionElement.getOrdinalNumberInt();
                    boolean b = remainingText.isEmpty() || song.getStrippedTitle().contains(remainingText);
                    if (containsInCollectionName && contains2 && b) {
                        contains = true;
                        if (equals) {
                            wasOrdinalNumber = true;
                            break;
                        }
                    }
                }
                if (contains || contains(song.getStrippedTitle(), text)) {
                    SearchedSong searchedSong = new SearchedSong(song);
                    searchedSongListViewItems.add(searchedSong);
                }
            }
            if (wasOrdinalNumber) {
                String finalCollectionName = collectionName;
                String finalOrdinalNumber = ordinalNumber;
                int finalOrdinalNumberInt = ordinalNumberInt;
                sortSongCollectionElementsForSongs(ordinalNumber, collectionName, ordinalNumberInt, searchedSongListViewItems);
                if (Thread.interrupted()) {
                    return;
                }
                searchedSongListViewItems.sort((l, r) -> {
                    List<SongCollectionElement> lSongCollectionElements = l.getSong().getSongCollectionElements();
                    List<SongCollectionElement> rSongCollectionElements = r.getSong().getSongCollectionElements();
                    return compareSongCollectionElementsFirstByOrdinalNumber(lSongCollectionElements, rSongCollectionElements, finalCollectionName, finalOrdinalNumber, finalOrdinalNumberInt);
                });
            }
            selectIfJustOne();
            if (songRemoteListener != null) {
                songRemoteListener.onSongListViewChanged(searchedSongListViewItems);
            }
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
    }

    private void sortSongCollectionElementsForSongs(String ordinalNumber, String collectionName, int ordinalNumberInt, ObservableList<SearchedSong> tempSongList) {
        Comparator<SongCollectionElement> sortBySongCollection = getSongCollectionElementComparator(collectionName, ordinalNumber, ordinalNumberInt);
        for (SearchedSong searchedSong : tempSongList) {
            Song song = searchedSong.getSong();
            List<SongCollectionElement> songCollectionElements = song.getSongCollectionElements();
            if (songCollectionElements.size() > 1) {
                List<SongCollectionElement> synchronizedList = Collections.synchronizedList(songCollectionElements);
                synchronizedList.sort(sortBySongCollection);
                song.setSongCollectionElements(synchronizedList);
                if (Thread.interrupted()) {
                    return;
                }
            }
        }
    }

    private boolean isContainsInCollectionName(String collectionName, SongCollectionElement songCollectionElement) {
        SongCollection songCollection = songCollectionElement.getSongCollection();
        String name = songCollection.getStrippedName();
        return containsInCollectionName(songCollection, collectionName, name);
    }

    public void titleSearchStartWith(String text) {
        try {
            lastSearching = LastSearching.IN_TITLE_START_WITH;
            lastSearchText = text;
            ObservableList<SearchedSong> items = searchedSongListView.getItems();
            items.clear();
            List<Song> songs = getFilteredSongs();
            for (Song song : songs) {
                if (song.getTitle().equals(text)) {
                    SearchedSong searchedSong = new SearchedSong(song);
                    items.add(searchedSong);
                    searchedSongListView.getSelectionModel().select(0);
                    return;
                }
            }
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
    }

    private void selectIfJustOne() {
        try {
            if (searchedSongListView.getItems().size() == 1) {
                searchedSongListView.getSelectionModel().clearAndSelect(0);
                searchedSongListView.requestFocus();
            }
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
    }

    public void initializeSongs() {
        try {
            readSongs();
            addAllSongs();
            addSongCollections();
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
    }

    private void readSongs() {
        try {
            Language songSelectedLanguage = settings.getSongSelectedLanguage();
            if (songSelectedLanguage != null) {
                songs = songSelectedLanguage.getSongs();
                setFavouriteSongs(songs);
                setSongCollections(songs);
                sortSongs(songs);
            }
        } catch (ServiceException e) {
            LOG.error(e.getMessage(), e);
        }
    }

    private void addSongsToSongListView(List<Song> songs) {
        try {
            ObservableList<SearchedSong> items = searchedSongListView.getItems();
            items.clear();
            for (Song song : songs) {
                SearchedSong searchedSong = new SearchedSong(song);
                items.add(searchedSong);
            }
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
    }

    private void addAllSongs() {
        addSongsToSongListView(songs);
    }

    private void sortSongs(List<Song> songs) {
        try {
            Comparator<Song> songComparator = getSongComparator();
            if (songComparator != null) {
                songs.sort(songComparator);
            }
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
    }

    private Comparator<Song> getSongComparator() {
        OrderMethod selectedItem = sortComboBox.getSelectionModel().getSelectedItem();
        Comparator<Song> songComparator = null;
        if (selectedItem == null || selectedItem.equals(OrderMethod.RELEVANCE)) {
            songComparator = getSongComparatorByRelevanceOrder();
        } else if (selectedItem.equals(OrderMethod.ASCENDING_BY_TITLE)) {
            songComparator = getSongComparatorByAscendingByTitle();
        } else if (selectedItem.equals(OrderMethod.DESCENDING_BY_TITLE)) {
            songComparator = getSongComparatorByDescendingByTitle();
        } else if (selectedItem.equals(OrderMethod.BY_MODIFIED_DATE)) {
            songComparator = getSongComparatorByModifiedDate();
        } else if (selectedItem.equals(OrderMethod.BY_PUBLISHED)) {
            songComparator = getSongComparatorByPublished();
        } else if (selectedItem.equals(OrderMethod.BY_COLLECTION)) {
            songComparator = getSongComparatorByCollection();
        }
        return songComparator;
    }

    private Comparator<Song> getSongComparatorByCollection() {
        return (l, r) -> {
            List<SongCollectionElement> lSongCollectionElements = l.getSongCollectionElements();
            List<SongCollectionElement> rSongCollectionElements = r.getSongCollectionElements();
            return compareSongCollectionElements(lSongCollectionElements, rSongCollectionElements);
        };
    }

    private int compareSongCollectionElements(List<SongCollectionElement> lSongCollectionElements, List<SongCollectionElement> rSongCollectionElements) {
        int lSize = lSongCollectionElements.size();
        int rSize = rSongCollectionElements.size();
        Comparator<SongCollectionElement> sortBySongCollection = Comparator.comparing(o -> o.getSongCollection().getName());
        lSongCollectionElements.sort(sortBySongCollection);
        rSongCollectionElements.sort(sortBySongCollection);
        int minSize = min(lSize, rSize);
        for (int i = 0; i < minSize; ++i) {
            SongCollectionElement lSongCollectionElement = lSongCollectionElements.get(i);
            SongCollectionElement rSongCollectionElement = rSongCollectionElements.get(i);
            int compare = compareSongCollectionElement(lSongCollectionElement, rSongCollectionElement);
            if (compare != 0) {
                return compare;
            }
        }
        if (lSize > rSize) {
            return -1;
        } else if (lSize < rSize) {
            return 1;
        }
        return 0;
    }

    private int compareSongCollectionElementsFirstByOrdinalNumber(List<SongCollectionElement> lSongCollectionElements, List<SongCollectionElement> rSongCollectionElements, String collectionName, String ordinalNumber, int ordinalNumberInt) {
        int lSize = lSongCollectionElements.size();
        int rSize = rSongCollectionElements.size();
        if (lSize != rSize) {
            if (lSize == 0) {
                return 1;
            }
            if (rSize == 0) {
                return -1;
            }
        }
        return compareSongCollectionElementsFirstByOrdinalNumberEnd(lSongCollectionElements, rSongCollectionElements, collectionName, ordinalNumber, ordinalNumberInt, lSize, rSize);
    }

    private Comparator<SongCollectionElement> getSongCollectionElementComparator(String collectionName, String ordinalNumber, int ordinalNumberInt) {
        return (songCollectionElement1, songCollectionElement2) -> {
            boolean containsInCollection1 = isContainsInCollectionName(collectionName, songCollectionElement1);
            boolean containsInCollection2 = isContainsInCollectionName(collectionName, songCollectionElement2);
            if (containsInCollection1 == containsInCollection2) {
                return compareSongCollectionElementByMatch(songCollectionElement1, songCollectionElement2, ordinalNumber, ordinalNumberInt);
            } else if (containsInCollection1) {
                return -1;
            } else {
                return 1;
            }
        };
    }

    private int compareSongCollectionElementsFirstByOrdinalNumberEnd(List<SongCollectionElement> lSongCollectionElements, List<SongCollectionElement> rSongCollectionElements, String collectionName, String ordinalNumber, int ordinalNumberInt, int lSize, int rSize) {
        int minSize = min(lSize, rSize);
        for (int i = 0; i < minSize; ++i) {
            SongCollectionElement lSongCollectionElement = lSongCollectionElements.get(i);
            SongCollectionElement rSongCollectionElement = rSongCollectionElements.get(i);
            boolean containsInCollectionL = isContainsInCollectionName(collectionName, lSongCollectionElement);
            boolean containsInCollectionR = isContainsInCollectionName(collectionName, rSongCollectionElement);
            if (containsInCollectionL == containsInCollectionR) {
                int compare = compareSongCollectionElementByMatch(lSongCollectionElement, rSongCollectionElement, ordinalNumber, ordinalNumberInt);
                if (compare != 0) {
                    return compare;
                }
                compare = compareSongCollectionElementFirstByOrdinalNumber(lSongCollectionElement, rSongCollectionElement);
                if (compare != 0) {
                    return compare;
                }
            } else if (containsInCollectionL) {
                return -1;
            } else {
                return 1;
            }
        }
        return 0;
    }

    private int compareSongCollectionElementByMatch(SongCollectionElement songCollectionElement1, SongCollectionElement songCollectionElement2, String ordinalNumber, int ordinalNumberInt) {
        boolean ordinalNumberMatch1 = songCollectionElement1.getOrdinalNumberLowerCase().equals(ordinalNumber);
        boolean ordinalNumberMatch2 = songCollectionElement2.getOrdinalNumberLowerCase().equals(ordinalNumber);
        if (ordinalNumberMatch1 == ordinalNumberMatch2) {
            if (ordinalNumberMatch1) {
                return 0;
            }
            int compare = compareSongCollectionElementByIntMatch(songCollectionElement1, songCollectionElement2, ordinalNumberInt);
            if (compare == 0) {
                return compareSongCollectionElementByPartialMatch(songCollectionElement1, songCollectionElement2, ordinalNumber);
            }
            return compare;
        } else if (ordinalNumberMatch1) {
            return -1;
        } else {
            return 1;
        }
    }

    private int compareSongCollectionElementByPartialMatch(SongCollectionElement songCollectionElement1, SongCollectionElement songCollectionElement2, String ordinalNumber) {
        boolean ordinalNumberMatch1 = songCollectionElement1.getOrdinalNumberLowerCase().contains(ordinalNumber);
        boolean ordinalNumberMatch2 = songCollectionElement2.getOrdinalNumberLowerCase().contains(ordinalNumber);
        if (ordinalNumberMatch1 == ordinalNumberMatch2) {
            if (ordinalNumberMatch1) {
                return Integer.compare(songCollectionElement1.getOrdinalNumberInt(), songCollectionElement2.getOrdinalNumberInt());
            }
            return 0;
        } else if (ordinalNumberMatch1) {
            return -1;
        } else {
            return 1;
        }
    }

    private int compareSongCollectionElementByIntMatch(SongCollectionElement songCollectionElement1, SongCollectionElement songCollectionElement2, int ordinalNumberInt) {
        boolean ordinalNumberMatch1 = songCollectionElement1.getOrdinalNumberInt() == ordinalNumberInt;
        boolean ordinalNumberMatch2 = songCollectionElement2.getOrdinalNumberInt() == ordinalNumberInt;
        if (ordinalNumberMatch1 == ordinalNumberMatch2) {
            return 0;
        } else if (ordinalNumberMatch1) {
            return -1;
        } else {
            return 1;
        }
    }

    private boolean containsInCollectionName(SongCollection songCollection, String collectionName, String name) {
        if (collectionName.trim().isEmpty()) {
            return false;
        }
        return name.contains(collectionName) || songCollection.getStrippedShortName().contains(collectionName);
    }

    private int compareSongCollectionElement(SongCollectionElement lSongCollectionElement, SongCollectionElement rSongCollectionElement) {
        SongCollection lSongCollection = lSongCollectionElement.getSongCollection();
        SongCollection rSongCollection = rSongCollectionElement.getSongCollection();
        if (lSongCollection.getName().equals(rSongCollection.getName())) {
            return Integer.compare(lSongCollectionElement.getOrdinalNumberInt(), rSongCollectionElement.getOrdinalNumberInt());
        }
        return lSongCollection.getStrippedName().compareTo(rSongCollection.getStrippedName());
    }

    private int compareSongCollectionElementFirstByOrdinalNumber(SongCollectionElement lSongCollectionElement, SongCollectionElement rSongCollectionElement) {
        int compareOrdinalNumber = Integer.compare(lSongCollectionElement.getOrdinalNumberInt(), rSongCollectionElement.getOrdinalNumberInt());
        if (compareOrdinalNumber != 0) {
            return compareOrdinalNumber;
        }
        return getSongComparator().compare(lSongCollectionElement.getSong(), rSongCollectionElement.getSong());
    }

    private void sortSongsByRelevanceOrder(List<Song> songs) {
        songs.sort(getSongComparatorByRelevanceOrder());
    }
    //        }
    //            }
    //                }
    ////                    }
    //                    // + " " + songs.get(j).getTitle());
    //                    // s2.length() + " " + songs.get(i).getTitle()
    //                    // System.out.println(s.length() + " " + x + " " +
    ////                    if (((double) x) / (double) s.length() > 0.9 || ((double) x) / (double) s2.length() > 0.9) {
    ////                    int x = StringUtils.highestCommonSubStringInt(s.toString(), s2.toString());
    //                    }
    //                        s2.append(k);
    //                    for (String k : songs.get(j).getVerses()) {
    //                    StringBuilder s2 = new StringBuilder();
    //                if (!songs.get(j).getTitle().equals(tmp.getTitle())) {
    //            for (int j = i + 1; j < songs.size(); ++j) {
    //            }
    //                s.append(k);
    //            for (String k : tmp.getVerses()) {
    //            Song tmp = songs.get(i);
    //            StringBuilder s = new StringBuilder();
    //        for (int i = 900; i < songs.size(); ++i) {
//    public void similars() {

//    }

    private void initListViewMenuItem() {
        try {
            final ContextMenu cm = new ContextMenu();
            initializeContextMenu(cm, LOG);
            MenuItem editMenuItem = new MenuItem(Settings.getInstance().getResourceBundle().getString("Edit"));
            MenuItem addToCollectionMenuItem = new MenuItem(Settings.getInstance().getResourceBundle().getString("Add to collection"));
            MenuItem removeFromCollectionMenuItem = new MenuItem(Settings.getInstance().getResourceBundle().getString("Remove from collection"));
            MenuItem deleteMenuItem = getDeleteMenuItem();
            MenuItem addScheduleMenuItem = new MenuItem(Settings.getInstance().getResourceBundle().getString("Add to schedule"));
            cm.getItems().addAll(editMenuItem, addToCollectionMenuItem, deleteMenuItem, addScheduleMenuItem);
            editMenuItem.setOnAction(new EventHandler<>() {

                @Override
                public void handle(ActionEvent event) {
                    try {
                        SearchedSong selectedItem = searchedSongListView.getSelectionModel().getSelectedItem();
                        Song selectedSong = selectedItem.getSong();
                        FXMLLoader loader = new FXMLLoader();
                        loader.setLocation(MainDesktop.class.getResource("/view/song/NewSong.fxml"));
                        loader.setResources(Settings.getInstance().getResourceBundle());
                        Pane root = loader.load();
                        NewSongController newSongController = loader.getController();
                        newSongController.setSongController(songController);
                        newSongController.setSelectedSong(selectedItem);
                        newSongController.setTitleTextFieldText(selectedSong.getTitle());
                        Stage stage = getCustomStage3(getClass(), root);
                        stage.setTitle(Settings.getInstance().getResourceBundle().getString("Song Edit"));
                        stage.show();

                        FXMLLoader loader2 = new FXMLLoader();
                        loader2.setLocation(MainDesktop.class.getResource("/view/ProjectionScreen.fxml"));
                        loader2.setResources(Settings.getInstance().getResourceBundle());
                        Pane root2 = loader2.load();
                        previewProjectionScreenController = loader2.getController();
                        newSongController.setPreviewProjectionScreenController(previewProjectionScreenController);
                        Scene scene2 = new Scene(root2, 400, 300);
                        URL resource1 = getClass().getResource("/view/" + settings.getSceneStyleFile());
                        if (resource1 != null) {
                            scene2.getStylesheets().add(resource1.toExternalForm());
                        }
                        Stage stage2 = getCustomStage2(getClass(), scene2, root2.getWidth(), root2.getHeight());
                        scene2 = stage2.getScene();
                        scene2.widthProperty().addListener((observable, oldValue, newValue) -> previewProjectionScreenController.repaint());
                        scene2.heightProperty().addListener((observable, oldValue, newValue) -> previewProjectionScreenController.repaint());

                        stage2.setX(0);
                        stage2.setY(0);
                        stage2.setTitle(Settings.getInstance().getResourceBundle().getString("Preview"));
                        stage2.show();
                        previewProjectionScreenController.setStage(stage2);

                        stage.setOnCloseRequest(we -> stage2.close());
                        newSongController.setEditingSong(selectedSong);
                        newSongController.setStage(stage, stage2);
                    } catch (Exception e) {
                        LOG.error(e.getMessage(), e);
                    }
                }
            });
            addToCollectionMenuItem.setOnAction(new EventHandler<>() {

                @Override
                public void handle(ActionEvent event) {
                    try {
                        Song selectedSong = searchedSongListView.getSelectionModel().getSelectedItem().getSong();
                        FXMLLoader loader = new FXMLLoader();
                        loader.setLocation(MainDesktop.class.getResource("/view/song/AddToCollection.fxml"));
                        loader.setResources(Settings.getInstance().getResourceBundle());
                        Pane root = loader.load();
                        AddToCollectionController addToCollectionController = loader.getController();
                        addToCollectionController.setSongController(songController);
                        addToCollectionController.setSelectedSong(selectedSong);
                        Scene scene = new Scene(root);
                        URL resource = getClass().getResource("/view/" + settings.getSceneStyleFile());
                        if (resource != null) {
                            scene.getStylesheets().add(resource.toExternalForm());
                        }
                        Stage stage = getAStage(getClass());
                        stage.setScene(scene);
                        stage.setTitle(Settings.getInstance().getResourceBundle().getString("Add to collection"));
                        addToCollectionController.setStage(stage);
                        stage.show();
                    } catch (Exception e) {
                        LOG.error(e.getMessage(), e);
                    }
                }
            });
            removeFromCollectionMenuItem.setOnAction(event -> {
                try {
                    Song selectedSong = searchedSongListView.getSelectionModel().getSelectedItem().getSong();
                    List<SongCollectionElement> songCollectionElements = selectedSong.getSongCollectionElements();
                    ServiceManager.getSongCollectionElementService().delete(songCollectionElements);
                    selectedSong.setSongCollections(null);
                    selectedSong.setSongCollectionElements(null);
                    addSongCollections();
                } catch (Exception e) {
                    LOG.error(e.getMessage(), e);
                }
            });
            deleteMenuItem.setOnAction(event -> {
                try {
                    confirmDeletion(() -> deleteSong(searchedSongListView.getSelectionModel().getSelectedItem()), LOG, getClass());
                } catch (Exception e) {
                    LOG.error(e.getMessage(), e);
                }
            });
            addScheduleMenuItem.setOnAction(event -> {
                try {
                    Song tmp = searchedSongListView.getSelectionModel().getSelectedItem().getSong();
                    scheduleController.addSong(tmp);
                } catch (Exception e) {
                    LOG.error(e.getMessage(), e);
                }
            });
            searchedSongListView.setOnMouseClicked(event -> {
                try {
                    if (event.getButton() == MouseButton.SECONDARY) {
                        SearchedSong selectedItem = searchedSongListView.getSelectionModel().getSelectedItem();
                        if (selectedItem == null) {
                            return;
                        }
                        Song selectedSong = selectedItem.getSong();
                        boolean hasSongCollection = selectedSong.hasSongCollection();
                        if (hasSongCollection) {
                            cm.getItems().remove(addToCollectionMenuItem);
                            cm.getItems().add(1, removeFromCollectionMenuItem);
                        } else {
                            cm.getItems().remove(removeFromCollectionMenuItem);
                            cm.getItems().add(1, addToCollectionMenuItem);
                        }
                        cm.show(searchedSongListView, event.getScreenX(), event.getScreenY());
                    } else {
                        cm.hide();
                    }
                } catch (Exception e) {
                    LOG.error(e.getMessage(), e);
                }
            });
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
    }

    private void initSongCollectionListViewMenuItem() {
        try {
            final ContextMenu cm = new ContextMenu();
            initializeContextMenu(cm, LOG);
            ResourceBundle resourceBundle = Settings.getInstance().getResourceBundle();
            MenuItem editMenuItem = new MenuItem(resourceBundle.getString("Edit"));
            CheckMenuItem showInTitleMenuItem = new CheckMenuItem(resourceBundle.getString("Show in title"));
            MenuItem deleteMenuItem = new MenuItem(resourceBundle.getString("Delete"));
            cm.getItems().addAll(showInTitleMenuItem, deleteMenuItem);
            showInTitleMenuItem.setOnAction(event -> {
                SongCollection songCollection = getSelectedSongCollection();
                songCollection.setShowInTitle(!songCollection.isShowInTitle());
                ServiceManager.getSongCollectionService().update(songCollection);
            });
            editMenuItem.setOnAction(new EventHandler<>() {

                @Override
                public void handle(ActionEvent event) {
                    try {
                        FXMLLoader loader = new FXMLLoader();
                        loader.setLocation(MainDesktop.class.getResource("/view/song/SongBook.fxml"));
                        loader.setResources(Settings.getInstance().getResourceBundle());
                        Pane root = loader.load();
                        NewSongCollectionController newSongCollectionController = loader.getController();
                        newSongCollectionController.setSongController(songController);
                        newSongCollectionController.setEditing(true, getSelectedSongCollection());
                        newSongCollectionController.setSongs(songs);
                        Scene scene = new Scene(root);
                        setSceneStyleFile(scene);
                        Stage stage = getAStage(getClass());
                        stage.setScene(scene);
                        stage.setTitle(Settings.getInstance().getResourceBundle().getString("SongBook Edit"));
                        stage.show();
                        newSongCollectionController.setStage(stage);
                    } catch (Exception e) {
                        LOG.error(e.getMessage(), e);
                    }
                }

                private void setSceneStyleFile(Scene scene) {
                    URL resource = getClass().getResource("/view/" + settings.getSceneStyleFile());
                    if (resource != null) {
                        scene.getStylesheets().add(resource.toExternalForm());
                    }
                }
            });
            deleteMenuItem.setOnAction(event -> {
                try {
                    confirmDeletion(() -> {
                        SongCollectionService SongCollectionService = ServiceManager.getSongCollectionService();
                        SongCollection selectedItem = getSelectedSongCollection();
                        SongCollectionService.delete(selectedItem);
                        songCollectionListView.getItems().remove(selectedItem);
                    }, LOG, getClass());
                } catch (Exception e) {
                    LOG.error(e.getMessage(), e);
                }
            });

            songCollectionListView.setOnMouseClicked(event -> {
                try {
                    if (event.getButton() == MouseButton.SECONDARY && !songCollectionListView.getSelectionModel().getSelectedIndices().get(0).equals(0)) {
                        SongCollection songCollection = getSelectedSongCollection();
                        showInTitleMenuItem.setSelected(songCollection.isShowInTitle());
                        cm.show(songCollectionListView, event.getScreenX(), event.getScreenY());
                    } else {
                        cm.hide();
                    }
                } catch (Exception e) {
                    LOG.error(e.getMessage(), e);
                }
            });
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
    }

    private SongCollection getSelectedSongCollection() {
        return songCollectionListView.getSelectionModel().getSelectedItem();
    }

    void addSongCollections() {
        try {
            pauseSortOrFilter = true;
            try {
                addSongCollections_();
            } finally {
                pauseSortOrFilter = false;
            }
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
    }

    private void addSongCollections_() {
        ObservableList<SongCollection> items = songCollectionListView.getItems();
        items.clear();
        String all = Settings.getInstance().getResourceBundle().getString("All");
        SongCollection allSongCollections = new SongCollection(all);
        allSongCollections.setSongs(songs);
        selectedSongCollection = allSongCollections;
        items.add(allSongCollections);
        songCollectionListView.getSelectionModel().selectFirst();
        try {
            Date date = new Date();
            List<SongCollection> songCollections = songCollectionService.findAll();
            Date date2 = new Date();
            System.out.println(date2.getTime() - date.getTime());
            ServiceManager.getSongCollectionElementService().findSongsSize(songCollections);
            songCollections.sort((l, r) -> {
                long lSongsSize = l.getSongsSize();
                long rSongsSize = r.getSongsSize();
                if (lSongsSize < rSongsSize) {
                    return 1;
                } else if (lSongsSize > rSongsSize) {
                    return -1;
                }
                return 0;
            });
            Language songSelectedLanguage = settings.getSongSelectedLanguage();
            if (songSelectedLanguage != null) {
                Long id = songSelectedLanguage.getId();
                for (SongCollection songCollection : songCollections) {
                    checkSongCollectionLanguage(songCollection);
                    Language language = songCollection.getLanguage();
                    if (language != null && language.getId().equals(id)) {
                        items.add(songCollection);
                    }
                }
            }
            boolean value = items.size() != 1;
            songCollectionListView.setVisible(value);
            songCollectionListView.setManaged(value);
        } catch (ServiceException e) {
            LOG.error(e.getMessage(), e);
        }
    }

    private void checkSongCollectionLanguage(SongCollection songCollection) {
        Language language = songCollection.getLanguage();
        if (language == null) {
            for (Song song : songCollection.getSongs()) {
                if (song.getLanguage() != null) {
                    language = song.getLanguage();
                    songCollection.setLanguage(language);
                    songCollectionService.create(songCollection);
                    break;
                }
            }
        }
    }

    public ListView<SongVersePartTextFlow> getSongListView() {
        return songListView;
    }

    private void deleteSong(SearchedSong selectedSong) {
        try {
            final Song song = removeSongFromList(selectedSong);
            searchedSongListView.getItems().remove(selectedSong);
            try {
                songService.delete(song);
            } catch (ServiceException e) {
                LOG.error(e.getMessage(), e);
            }
            addAllSongs();
            addSongCollections();
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
    }

    Song removeSongFromList(SearchedSong searchedSong) {
        try {
            searchedSongListView.getItems().remove(searchedSong);
            final Song song = searchedSong.getSong();
            for (int i = 0; i < songs.size(); ++i) {
                final Song song1 = songs.get(i);
                if (song.getId().equals(song1.getId())) {
                    songs.remove(i);
                    return song1;
                }
            }
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
        return null;
    }

    public void newSongButtonOnAction() {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(MainDesktop.class.getResource("/view/song/NewSong.fxml"));
            loader.setResources(Settings.getInstance().getResourceBundle());
            Pane root = loader.load();
            NewSongController newSongController = loader.getController();
            newSongController.setSongController(songController);
            newSongController.setRoot(root); // only for testing
            Stage stage = getCustomStage3(getClass(), root);
            stage.setTitle(Settings.getInstance().getResourceBundle().getString("Song Edit"));
            stage.show();

            FXMLLoader loader2 = new FXMLLoader();
            loader2.setLocation(MainDesktop.class.getResource("/view/ProjectionScreen.fxml"));
            loader2.setResources(Settings.getInstance().getResourceBundle());
            Pane root2 = loader2.load();
            previewProjectionScreenController = loader2.getController();
            newSongController.setPreviewProjectionScreenController(previewProjectionScreenController);
            Scene scene2 = new Scene(root2, 400, 300);
            setSceneStyleFile2(scene2);
            Stage stage2 = getCustomStage2(getClass(), scene2, scene2.getWidth(), scene2.getHeight());
            scene2 = stage2.getScene();
            scene2.widthProperty().addListener((observable, oldValue, newValue) -> {
                try {
                    previewProjectionScreenController.repaint();
                } catch (Exception e) {
                    LOG.error(e.getMessage(), e);
                }
            });
            scene2.heightProperty().addListener((observable, oldValue, newValue) -> {
                try {
                    previewProjectionScreenController.repaint();
                } catch (Exception e) {
                    LOG.error(e.getMessage(), e);
                }
            });

            stage2.setX(0);
            stage2.setY(0);
            stage2.show();
            previewProjectionScreenController.setStage(stage2);

            stage.setOnCloseRequest(we -> stage2.close());
            newSongController.setNewSong();
            newSongController.setStage(stage, stage2);
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
    }

    private void setSceneStyleFile2(Scene scene) {
        URL resource = getClass().getResource("/view/" + settings.getSceneStyleFile());
        if (resource != null) {
            scene.getStylesheets().add(resource.toExternalForm());
        }
    }

    @SuppressWarnings("unused")
    public void newSongCollectionButtonOnAction() {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(MainDesktop.class.getResource("/view/song/SongBook.fxml"));
            loader.setResources(Settings.getInstance().getResourceBundle());
            Pane root = loader.load();
            NewSongCollectionController newSongCollectionController = loader.getController();
            newSongCollectionController.setSongs(songs);
            Scene scene = new Scene(root);
            setSceneStyleFile2(scene);
            Stage stage = getAStage(getClass());
            stage.setScene(scene);
            stage.setTitle(Settings.getInstance().getResourceBundle().getString("New song book"));
            newSongCollectionController.setStage(stage);
            newSongCollectionController.setSongController(this);
            stage.show();
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
    }

    public void onClose() {
        try {
            if (opacityScheduler != null) {
                opacityScheduler.shutdown();
            }
            saveSongVerseTimes();
            saveSomethingsInSettings();
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
    }

    private void saveSongVerseTimes() {
        if (previousSongVerseTimeList == null) {
            return;
        }
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(getSongVersTimesFilePath(), true);
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fileOutputStream, StandardCharsets.UTF_8));
            Date date = new Date();
            boolean wasSong = false;
            if (activeSongVerseTime != null) {
                previousSongVerseTimeList.add(activeSongVerseTime);
            }
            int minSec = 10;
            for (SongVerseTime songVerseTime : previousSongVerseTimeList) {
                double totalTime = songVerseTime.getTotalTime();
                if (totalTime > minSec) {
                    wasSong = true;
                    break;
                }
            }
            if (wasSong) {
                bw.write(date + System.lineSeparator());
                SongVerseTimeService songVerseTimeService = SongVerseTimeService.getInstance();
                if (!songVerseTimeService.isLastVersionWasSaved()) {
                    bw.write(songVerseTimeService.getLastVersionLine() + System.lineSeparator());
                }
                StringBuilder s = new StringBuilder();
                for (SongVerseTime songVerseTime : previousSongVerseTimeList) {
                    double totalTime = songVerseTime.getTotalTime();
                    if (totalTime > minSec) {
                        s.append(getSongVerseTimeStringForFile(songVerseTime));
                    }
                }
                bw.write(s.toString());
                bw.write(System.lineSeparator());
                bw.write(System.lineSeparator());
            }
            bw.close();
        } catch (IOException e) {
            LOG.error(e.getMessage(), e);
        }
    }

    private static String getSongVerseTimeStringForFile(SongVerseTime songVerseTime) throws IOException {
        StringBuilder songVerseTimeText = new StringBuilder(songVerseTime.getSongTitle() + System.lineSeparator());
        for (double verseTime : songVerseTime.getVerseTimes()) {
            songVerseTimeText.append(verseTime).append(" ");
        }
        songVerseTimeText.append(System.lineSeparator());
        songVerseTimeText.append(songVerseTime.getSongUuid()).append(System.lineSeparator());
        songVerseTimeText.append(songVerseTime.getSongId()).append(System.lineSeparator());
        songVerseTimeText.append(songVerseTime.getSongTextLength()).append(System.lineSeparator());
        return songVerseTimeText.toString();
    }

    private void saveSomethingsInSettings() {
        Settings settings = Settings.getInstance();
        settings.setSongTabHorizontalSplitPaneDividerPosition(horizontalSplitPane.getDividerPositions()[0]);
        settings.setSongTabVerticalSplitPaneDividerPosition(verticalSplitPane.getDividerPositions()[0]);
        settings.setSongHeightSliderValue(songHeightSlider.getValue());
        settings.save();
    }

    public void setNext() {
        try {
            if (songListView.getSelectionModel().getSelectedIndex() + 1 < songListView.getItems().size()) {
                songListView.getSelectionModel().clearAndSelect(songListView.getSelectionModel().getSelectedIndex() + 1);
            }
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
    }

    public void setPrevious() {
        try {
            if (songListView.getSelectionModel().getSelectedIndex() - 1 >= 0) {
                songListView.getSelectionModel().clearAndSelect(songListView.getSelectionModel().getSelectedIndex() - 1);
            }
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
    }

    public void onBlankButtonSelected(boolean isSelected) {
        try {
            if (isSelected) {
                if (activeSongVerseTime != null && activeSongVerseTime.getVerseTimes() != null && activeSongVerseTime.getVerseTimes().length > previousSelectedVerseIndex && previousSelectedVerseIndex >= 0 && activeSongVerseTime.getVerseTimes()[previousSelectedVerseIndex] == 0.0) {
                    double x = System.currentTimeMillis() - timeStart;
                    x /= 1000;
                    activeSongVerseTime.getVerseTimes()[previousSelectedVerseIndex] = x;
                }
            } else {
                timeStart = System.currentTimeMillis();
            }
            isBlank = isSelected;
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
    }

    public void setMainController(MyController mainController) {
        try {
            this.mainController = mainController;
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
    }

    private void selectFirstSong() {
        try {
            if (!searchedSongListView.getItems().isEmpty()) {
                searchedSongListView.getSelectionModel().clearAndSelect(0);
                searchedSongListView.requestFocus();
            }
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
    }

    public ListView<SearchedSong> getListView() {
        return searchedSongListView;
    }

    void addSong(Song song) {
        try {
            songs.add(song);
            sortSongs(songs);
            addAllSongs();
            int scrollToIndex = 0;
            for (Song song1 : songs) {
                if (song1.equals(song)) {
                    break;
                }
                ++scrollToIndex;
            }
            addSongCollections();
            songController.getListView().scrollTo(scrollToIndex);
            songController.getListView().getSelectionModel().select(scrollToIndex);
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
    }

    public synchronized void addProjectionTextChangeListener(ProjectionTextChangeListener projectionTextChangeListener) {
        if (projectionTextChangeListeners == null) {
            projectionTextChangeListeners = new ArrayList<>();
        }
        projectionTextChangeListeners.add(projectionTextChangeListener);
    }

    private void exportButtonOnAction() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(Settings.getInstance().getResourceBundle().getString("Choose a file"));
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("text", "*.txt"));
        fileChooser.setInitialDirectory(new File(new File(".").getAbsolutePath()));
        File selectedFile = fileChooser.showSaveDialog(null);
        Thread thread = new Thread(() -> {
            if (selectedFile != null) {
                FileOutputStream ofStream;
                try {
                    ofStream = new FileOutputStream(selectedFile);
                    BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(ofStream, StandardCharsets.UTF_8));

                    Gson gson = new GsonBuilder().serializeNulls().excludeFieldsWithoutExposeAnnotation().create();
                    for (Song song : songs) {
                        song.getVerses();
                    }
                    String json = gson.toJson(songs);
                    bw.write(json);
                    bw.close();
                    Platform.runLater(() -> {
                        Alert alert = new Alert(Alert.AlertType.INFORMATION);
                        alert.setTitle("Completed");
                        alert.setContentText("Successfully exported!");
                        alert.showAndWait();
                    });
                } catch (IOException e) {
                    LOG.error(e.getMessage(), e);
                }
            }
        });
        thread.start();
    }

    private void importButtonOnAction() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(Settings.getInstance().getResourceBundle().getString("Choose a file"));
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("text", "*.txt"));
        fileChooser.setInitialDirectory(new File(new File(".").getAbsolutePath()));
        File selectedFile = fileChooser.showOpenDialog(null);
        Thread thread = new Thread(() -> {
            if (selectedFile != null) {
                FileInputStream inputStream;
                try {
                    inputStream = new FileInputStream(selectedFile);
                    BufferedReader br = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
                    StringBuilder s = new StringBuilder();
                    String readLine = br.readLine();
                    while (readLine != null) {
                        s.append(readLine);
                        readLine = br.readLine();
                    }
                    Gson gson = new GsonBuilder().serializeNulls().excludeFieldsWithoutExposeAnnotation().create();
                    ArrayList<Song> songArrayList;
                    Type listType = new TypeToken<ArrayList<Song>>() {
                    }.getType();
                    songArrayList = gson.fromJson(s.toString(), listType);
                    successfullyCreated = 0;
                    for (Song song : songArrayList) {
                        Song byUuid = null;
                        String uuid = song.getUuid();
                        if (uuid != null) {
                            byUuid = songService.findByUuid(uuid);
                        }
                        if (byUuid == null) {
                            try {
                                song.stripTitle();
                                song.setVerses(song.getVerses());
                                songService.create(song);
                                ++successfullyCreated;
                            } catch (ServiceException ignored) {
                            }
                        }
                    }
                    br.close();
                    Platform.runLater(() -> {
                        initializeSongs();
                        Alert alert = new Alert(Alert.AlertType.INFORMATION);
                        alert.setTitle("Completed");
                        String contentText = "Successfully imported: " + successfullyCreated;
                        int conflicts = songArrayList.size() - successfullyCreated;
                        if (conflicts > 0) {
                            contentText += "\nConflicts: " + conflicts + "\nIf you have trouble please contact us!";
                        }
                        alert.setContentText(contentText);
                        alert.showAndWait();
                    });
                } catch (IOException e) {
                    LOG.error(e.getMessage(), e);
                }
            }
        });
        thread.start();
    }

    public void onKeyPressed(KeyEvent event) {
        KeyCode keyCode = event.getCode();
        if (keyCode.isDigitKey()) {
            verseTextField.setText(verseTextField.getText() + getTextFromEvent(event));
            verseTextField.requestFocus();
            event.consume();
        } else if (keyCode.equals(KeyCode.ENTER)) {
            selectByVerseTextFieldNumber();
        }
    }

    void selectSong(Song song) {
        ObservableList<SearchedSong> listViewItems = searchedSongListView.getItems();
        listViewItems.clear();
        listViewItems.add(new SearchedSong(song));
        searchedSongListView.getSelectionModel().selectFirst();
    }

    public void setSongRemoteListener(SongRemoteListener songRemoteListener) {
        this.songRemoteListener = songRemoteListener;
    }

    public SongReadRemoteListener getSongReadRemoteListener() {
        if (songReadRemoteListener == null) {
            songReadRemoteListener = new SongReadRemoteListener() {
                @Override
                public void onSongVerseListViewItemClick(int index) {
                    Platform.runLater(() -> {
                        if (songListView.getItems().size() > index) {
                            songListView.getSelectionModel().clearAndSelect(index);
                        }
                    });
                }

                @Override
                public void onSongListViewItemClick(int index) {
                    Platform.runLater(() -> {
                        if (searchedSongListView.getItems().size() > index) {
                            searchedSongListView.getSelectionModel().clearAndSelect(index);
                        }
                    });
                }

                @Override
                public void onSearch(String text) {
                    Platform.runLater(() -> titleSearch(text));
                }

                @Override
                public void onSongPrev() {
                    Platform.runLater(() -> setPrevious());
                }

                @Override
                public void onSongNext() {
                    Platform.runLater(() -> {
                        selectNextSongFromScheduleIfLastIndex();
                        setNext();
                    });
                }
            };
        }
        return songReadRemoteListener;
    }

    public void onKeyEvent(KeyEvent event) {
        KeyCode keyCode = event.getCode();
        if (event.isControlDown()) {
            if (keyCode == KeyCode.S) {
                searchTextField.requestFocus();
            }
        }
    }

    public void refreshScheduleListView() {
        scheduleListView.refresh();
    }

    public void importOpenLPFolderButtonOnAction() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Select OpenLP exported folder");
        directoryChooser.setInitialDirectory(new File(new File(".").getAbsolutePath()));
        File selectedDirectory = directoryChooser.showDialog(null);
        Thread thread = new Thread(() -> {
            if (selectedDirectory != null && selectedDirectory.isDirectory()) {
                File[] files = selectedDirectory.listFiles();
                if (files != null) {
                    List<SongDTO> songDTOS = getXmlSongs(files);
                    List<Song> xmlSongs = SongAssembler.getInstance().createModelList(songDTOS);
                    Platform.runLater(() -> addSongsToSongListView(xmlSongs));
                }
            }
        });
        thread.start();
    }

    public void selectSongTitle() {
        try {
            MultipleSelectionModel<SongVersePartTextFlow> selectionModel = songListView.getSelectionModel();
            if (selectionModel.getSelectedIndex() == 0) {
                slideReSelect();
            } else {
                selectionModel.clearAndSelect(0);
            }
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
    }

    public void onFavouritesUpdated() {
        Platform.runLater(() -> {
            initializeWithNullSomeLazzyFields();
            searchAgain();
            checkStarButtonVisibilityByLoggedIn();
        });
    }

    private void initializeWithNullSomeLazzyFields() {
        favouriteSongs = null;
    }

    public void reloadInitialSongs() {
        Platform.runLater(() -> {
            initializeWithNullSomeLazzyFields();
            settings.getSongSelectedLanguage().setSongs(null);
            initializeSongs();
            initializeLanguageComboBox();
            checkStarButtonVisibilityByLoggedIn();
        });
    }

    public void updateProjectorState(ProjectorState projectorState) {
        projectorState.setSelectedSong(selectedSong);
        projectorState.setSelectedLanguage(languageComboBox.getSelectionModel().getSelectedItem());
    }

    public void setByProjectorState(ProjectorState projectorState) {
        try {
            Language selectedLanguage = projectorState.getSelectedLanguage();
            if (selectedLanguage != null) {
                Language languageInCombo = getLanguageFromList(selectedLanguage, languageComboBox.getItems());
                languageComboBox.getSelectionModel().select(languageInCombo);
            }
            Song selectedSong = projectorState.getSelectedSong();
            SearchedSong searchedSong = getSearchedSongBySong(selectedSong, searchedSongListView.getItems());
            if (searchedSong != null) {
                searchedSongListView.getSelectionModel().select(searchedSong);
            }
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
    }

    private Language getLanguageFromList(Language language, List<Language> languages) {
        if (language == null) {
            return null;
        }
        for (Language languageInList : languages) {
            if (languageInList.equivalent(language)) {
                return languageInList;
            }
        }
        return null;
    }

    private SearchedSong getSearchedSongBySong(Song song, List<SearchedSong> searchedSongs) {
        for (SearchedSong searchedSong : searchedSongs) {
            if (searchedSong.getSong().equivalent(song)) {
                return searchedSong;
            }
        }
        return null;
    }

    public void onSignInUpdated(boolean signedIn) {
        uploadButton.setManaged(signedIn);
        uploadButton.setVisible(signedIn);
    }
}
