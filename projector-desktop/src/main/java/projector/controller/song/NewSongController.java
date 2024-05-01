package projector.controller.song;

import com.bence.projector.common.model.SectionType;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.IndexRange;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseButton;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Callback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import projector.Credentials;
import projector.MainDesktop;
import projector.api.ApiException;
import projector.api.SongApiBean;
import projector.application.ProjectionType;
import projector.application.Settings;
import projector.controller.LoginController;
import projector.controller.ProjectionScreenController;
import projector.controller.song.util.OnChangeListener;
import projector.controller.song.util.SearchedSong;
import projector.model.Language;
import projector.model.Song;
import projector.model.SongVerse;
import projector.service.LanguageService;
import projector.service.ServiceException;
import projector.service.ServiceManager;
import projector.service.SongService;
import projector.service.SongVerseService;
import projector.utils.DraggableEntity;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import static projector.controller.song.VerseController.getRawTextFromVerseString;
import static projector.utils.ContextMenuUtil.initializeContextMenu;
import static projector.utils.SceneUtils.getAStage;

public class NewSongController {

    private static final Logger LOG = LoggerFactory.getLogger(NewSongController.class);
    private static Pane globalRoot;
    private final Settings settings = Settings.getInstance();
    private final String prefix = "verseOrderListView:move:";
    private final ArrayList<VerseController> verseControllers = new ArrayList<>();
    private final SongService songService = ServiceManager.getSongService();
    private final SongVerseService songVerseService = ServiceManager.getSongVerseService();
    @FXML
    private ListView<DraggableEntity<SongVerse>> verseOrderListView;
    @FXML
    private CheckBox uploadCheckBox;
    @FXML
    private ComboBox<Language> languageComboBoxForNewSong;
    @FXML
    private ToggleButton secondTextToggleButton;
    @FXML
    private Button uploadButton;
    @FXML
    private Button saveButton;
    @FXML
    private ColorPicker colorPicker;
    @FXML
    private TextField titleTextField;
    @FXML
    private TextArea textArea;
    @FXML
    private RadioButton verseEditorRadioButton;
    @FXML
    private RadioButton rawTextEditorRadioButton;
    @FXML
    private BorderPane rawTextBorderPane;
    @FXML
    private ScrollPane verseEditorScrollPane;
    @FXML
    private VBox textAreas;
    @FXML
    private Button newVerseButton;
    @FXML
    private BorderPane borderPane;
    private SongController songController;
    private Stage stage;
    private Boolean edit;
    @FXML
    private ProjectionScreenController previewProjectionScreenController;
    private Stage stage2;
    private SearchedSong selectedSong;
    private Song editingSong;
    private Song newSong;
    private VerseController lastFocusedVerse;
    private List<Language> languages;
    private boolean sameAsCalculatedOrder = false;

    public static Pane getGlobalRoot() {
        return globalRoot;
    }

    public void initialize() {
        edit = null;
        textArea.textProperty().addListener((observable, oldValue, newValue) -> toProjectionScreen());
        textArea.setWrapText(true);
        textArea.caretPositionProperty().addListener((observable, oldValue, newValue) -> toProjectionScreen());
        initializeRadioButtons();
        verseEditorScrollPane.setFitToWidth(true);
        initializeNewVerseButton();
        textAreas.getChildren().clear();
        colorPicker.setFocusTraversable(false);
        colorPicker.setOnAction(event -> {
            Color value = colorPicker.getValue();
            TextArea lastFocusedVerseTextArea = lastFocusedVerse.getTextArea();
            IndexRange selection = lastFocusedVerseTextArea.getSelection();
            System.out.println("selection = " + selection);
            String text = lastFocusedVerseTextArea.getText();
            String left = text.substring(0, selection.getStart());
            String selected = text.substring(selection.getStart(), selection.getEnd());
            selected = selected.replaceAll("<color=\"0x.{0,9}>", "");
            selected = selected.replaceAll("</color>", "");
            String right = text.substring(selection.getEnd());
            lastFocusedVerseTextArea.setText(left + "<color=\"" + value.toString() + "\">" + selected + "</color>" + right);
        });
        LanguageService languageService = ServiceManager.getLanguageService();
        languages = languageService.findAll();
        languageService.sortLanguages(languages);
        languageComboBoxForNewSong.getItems().addAll(languages);
        textAreas.getChildren().clear();
        verseControllers.clear();
        verseOrderListView.getItems().clear();
        initializeVerseOrderList();
    }

    private void initializeNewVerseButton() {
        newVerseButton.setOnAction(event -> {
            List<SongVerse> songVerses = calculateOrder();
            List<SongVerse> songVersesByVerseOrder = editingSong.getSongVersesByVerseOrder();
            boolean sameOrder = songVersesListsSameOrder(songVerses, songVersesByVerseOrder);
            SongVerse songVerse = new SongVerse();
            editingSong.getVerses().add(songVerse);
            addNewSongVerse(songVerse);
            if (sameOrder) {
                fillVerseOrder(calculateOrder());
                sameAsCalculatedOrder = true;
            } else {
                addVerseToVerseOrder(songVerse);
            }
            setVerseOrderForSong(editingSong);
        });
    }

    private void addVerseToVerseOrder(SongVerse songVerse) {
        ObservableList<DraggableEntity<SongVerse>> verseOrderListViewItems = verseOrderListView.getItems();
        DraggableEntity<SongVerse> songVerseDraggableEntity = new DraggableEntity<>(songVerse);
        songVerseDraggableEntity.setListViewIndex(verseOrderListViewItems.size());
        verseOrderListViewItems.add(songVerseDraggableEntity);
    }

    private boolean songVersesListsSameOrder(List<SongVerse> songVerses1, List<SongVerse> songVerses2) {
        int size = songVerses1.size();
        if (size != songVerses2.size()) {
            return false;
        }
        for (int i = 0; i < size; ++i) {
            if (!songVerses1.get(i).getText().equals(songVerses2.get(i).getText())) {
                return false;
            }
        }
        return true;
    }

    private void initializeVerseOrderList() {
        verseOrderListView.orientationProperty().set(Orientation.HORIZONTAL);
        final ContextMenu cm = new ContextMenu();
        initializeContextMenu(cm, LOG);
        MenuItem deleteMenuItem = new MenuItem(Settings.getInstance().getResourceBundle().getString("Delete"));
        deleteMenuItem.setOnAction(event -> {
            try {
                DraggableEntity<SongVerse> selectedItem = verseOrderListView.getSelectionModel().getSelectedItem();
                verseOrderListView.getItems().remove(selectedItem);
            } catch (Exception e) {
                LOG.error(e.getMessage(), e);
            }
        });
        cm.getItems().addAll(deleteMenuItem);
        verseOrderListView.setOnMouseClicked(event -> {
            try {
                if (event.getButton() == MouseButton.SECONDARY && !aloneInList(verseOrderListView.getSelectionModel().getSelectedItem(), verseOrderListView.getItems())) {
                    cm.show(verseOrderListView, event.getScreenX(), event.getScreenY());
                } else {
                    cm.hide();
                }
            } catch (Exception e) {
                LOG.error(e.getMessage(), e);
            }
        });
        verseOrderListView.setCellFactory(new Callback<>() {
            @Override
            public ListCell<DraggableEntity<SongVerse>> call(ListView<DraggableEntity<SongVerse>> listView) {

                return new ListCell<>() {
                    final Tooltip tooltip = new Tooltip();

                    @Override
                    protected void updateItem(DraggableEntity<SongVerse> songVerse, boolean empty) {
                        try {
                            super.updateItem(songVerse, empty);
                            if (songVerse != null && !empty) {
                                setText(songVerse.getEntity().getSectionTypeStringWithCount());
                                ListCell<DraggableEntity<SongVerse>> thisCell = this;
                                setOnDragDetected(event -> {
                                    if (getItem() == null) {
                                        return;
                                    }
                                    Dragboard dragboard = startDragAndDrop(TransferMode.MOVE);
                                    ClipboardContent content = new ClipboardContent();
                                    content.putString(prefix + verseOrderListView.getSelectionModel().getSelectedIndex());
                                    dragboard.setContent(content);
                                });
                                setOnDragEntered(event -> {
                                    if (event.getGestureSource() != thisCell && event.getDragboard().hasString()) {
                                        setOpacity(0.3);
                                    }
                                });
                                setOnDragExited(event -> {
                                    if (event.getGestureSource() != thisCell &&
                                            event.getDragboard().hasString()) {
                                        setOpacity(1);
                                    }
                                });
                                setOnDragOver(dragEvent -> dragEvent.acceptTransferModes(TransferMode.MOVE, TransferMode.COPY, TransferMode.LINK));
                                setOnDragDropped(event -> {
                                    if (getItem() == null) {
                                        return;
                                    }
                                    Dragboard dragboard = event.getDragboard();
                                    if (dragboard.hasString()) {
                                        int index = getIndexFromDragBoard(dragboard);
                                        if (index != -1) {
                                            ObservableList<DraggableEntity<SongVerse>> items = verseOrderListView.getItems();
                                            DraggableEntity<SongVerse> songVerse1 = items.get(index);
                                            int otherIndex = songVerse.getListViewIndex();
                                            if (otherIndex < index) {
                                                for (int i = index; i > otherIndex; --i) {
                                                    DraggableEntity<SongVerse> element = items.get(i - 1);
                                                    element.setListViewIndex(i);
                                                    items.set(i, element);
                                                }
                                                songVerse1.setListViewIndex(otherIndex);
                                                items.set(otherIndex, songVerse1);
                                            } else if (otherIndex > index) {
                                                for (int i = index; i < otherIndex; ++i) {
                                                    DraggableEntity<SongVerse> element = items.get(i + 1);
                                                    element.setListViewIndex(i);
                                                    items.set(i, element);
                                                }
                                                songVerse1.setListViewIndex(otherIndex);
                                                items.set(otherIndex, songVerse1);
                                            }
                                            calculateSameOrder();
                                        }
                                        event.setDropCompleted(true);
                                    }
                                });
                                tooltip.setText(songVerse.getEntity().getText());
                                setTooltip(tooltip);
                            } else {
                                setText(null);
                                setTooltip(null);
                            }
                        } catch (Exception e) {
                            LOG.error(e.getMessage(), e);
                        }
                    }
                };
            }
        });
    }

    private boolean aloneInList(DraggableEntity<SongVerse> selectedItem, ObservableList<DraggableEntity<SongVerse>> items) {
        int listViewIndex = selectedItem.getListViewIndex();
        SongVerse selectedVerse = selectedItem.getEntity();
        for (DraggableEntity<SongVerse> verseDraggableEntity : items) {
            if (verseDraggableEntity.getListViewIndex() != listViewIndex && verseDraggableEntity.getEntity().equals(selectedVerse)) {
                return false;
            }
        }
        return true;
    }

    private int getIndexFromDragBoard(Dragboard dragboard) {
        String string = dragboard.getString();
        if (string.startsWith(prefix)) {
            return Integer.parseInt(string.replace(prefix, ""));
        }
        return -1;
    }

    private void toProjectionScreen() {
        int caretPosition = textArea.getCaretPosition();

        System.out.println(caretPosition);
        String[] split = textArea.getText().split("\n\n");
        for (String i : split) {
            if (i.length() >= caretPosition) {
                StringBuilder result = new StringBuilder();
                for (String line : i.split("\n")) {
                    if (SectionType.getValueFromString(line).equals(SectionType.VERSE)) {
                        result.append(line).append("\n");
                    }
                }
                int end = result.length() - 1;
                if (end > 0) {
                    previewProjectionScreenController.setText2(result.substring(0, end),
                            ProjectionType.SONG);
                }
                break;
            } else {
                caretPosition -= (i.length() + 2);
                System.out.println(caretPosition);
            }
        }
    }

    private void initializeRadioButtons() {
        final ToggleGroup toggleGroup = new ToggleGroup();
        verseEditorRadioButton.setToggleGroup(toggleGroup);
        rawTextEditorRadioButton.setToggleGroup(toggleGroup);
        verseEditorRadioButton.setSelected(true);
        verseEditorRadioButton.selectedProperty().addListener((observable, oldValue, newValue) -> {
            borderPane.getChildren().clear();
            if (edit == null) {
                return;
            }
            if (newValue) {
                borderPane.setCenter(verseEditorScrollPane);
                verseControllers.clear();
                verseOrderListView.getItems().clear();
                textAreas.getChildren().clear();
                String s = textArea.getText();
                while (s.contains("\n\n\n")) {
                    s = s.replaceAll("\n\n\n", "\n\n");
                }
                String textAreaText = s.replaceAll("]\n\n", "]\n");
                String[] split = textAreaText.split("\n\n");
                Map<String, Short> versesMap = new HashMap<>(split.length);
                short verseCount = 0;
                List<Short> verseOrderList = new ArrayList<>(split.length);
                for (String verseText : split) {
                    String key = verseText.trim();
                    Short verseIndex = versesMap.get(key);
                    if (verseIndex == null) {
                        verseIndex = verseCount;
                        versesMap.put(key, verseCount++);
                        SongVerse songVerse = new SongVerse();
                        String trimmedText = key;
                        String[] lines = trimmedText.split("\n");
                        if (lines.length > 0) {
                            String firstLine = lines[0];
                            SectionType sectionType = SectionType.getValueFromString(firstLine);
                            if (!sectionType.equals(SectionType.VERSE)) {
                                songVerse.setSectionType(sectionType);
                                if (trimmedText.length() == firstLine.length()) {
                                    continue;
                                }
                                trimmedText = trimmedText.substring(firstLine.length() + 1);
                            } else {
                                songVerse.setSectionType(SectionType.VERSE);
                            }
                        } else {
                            songVerse.setSectionType(SectionType.VERSE);
                        }
                        songVerse.setText(trimmedText);
                        editingSong.getVerses().add(songVerse);
                        addNewSongVerse(songVerse);
                    }
                    verseOrderList.add(verseIndex);
                }
                editingSong.setVerseOrderList(verseOrderList);
                fillVerseOrder(editingSong.getSongVersesByVerseOrder());
                colorPicker.setDisable(false);
                saveButton.setDisable(false);
                uploadButton.setDisable(false);
                verseOrderListView.setVisible(true);
            } else {
                colorPicker.setDisable(true);
                saveButton.setDisable(true);
                uploadButton.setDisable(true);
                String text = getRawTextFromVerses();
                textArea.setText(text);
                borderPane.setCenter(rawTextBorderPane);
                verseOrderListView.setVisible(false);
                editingSong.getVerses().clear();
            }
        });
        borderPane.getChildren().clear();
        borderPane.setCenter(verseEditorScrollPane);
    }

    private String getRawTextFromVerses() {
        StringBuilder text = new StringBuilder();
        List<SongVerse> songVersesByVerseOrder = editingSong.getSongVersesByVerseOrder();
        for (SongVerse songVerse : songVersesByVerseOrder) {
            final String rawText = getRawTextFromVerseString(songVerse.getText());
            if (!rawText.isEmpty()) {
                if (!songVerse.getSectionType().equals(SectionType.VERSE)) {
                    text.append(songVerse.getSectionType().getStringValue()).append("\n");
                }
                text.append(rawText).append("\n\n");
            }
        }
        return text.toString().trim();
    }

    private List<SongVerse> getVerses() {
        List<SongVerse> songVerses = new ArrayList<>(verseControllers.size());
        for (VerseController verseController : verseControllers) {
            final SongVerse songVerse = verseController.getSongVerse();
            songVerses.add(songVerse);
        }
        return songVerses;
    }

    void setPreviewProjectionScreenController(ProjectionScreenController previewProjectionScreenController) {
        this.previewProjectionScreenController = previewProjectionScreenController;
    }

    private boolean isEdit() {
        return edit != null && edit;
    }

    void setSongController(SongController songController) {
        this.songController = songController;
    }

    void setStage(Stage stage, Stage stage2) {
        this.stage = stage;
        this.stage2 = stage2;
    }

    public void saveButtonOnAction() {
        if (saveSong()) {
            songController.addSong(newSong);
            songController.refreshScheduleListView();
        }
    }

    private boolean saveSong() {
        Language selectedLanguage = languageComboBoxForNewSong.getSelectionModel().getSelectedItem();
        if (selectedLanguage == null) {
            Alert alert = new Alert(AlertType.WARNING);
            alert.setTitle("Warning");
            alert.setHeaderText("No language!");
            alert.setContentText("Please select a language! Or create a new one at " + Credentials.BASE_URL);
            alert.showAndWait();
            return false;
        }
        if (titleTextField.getText().trim().isEmpty()) {
            Alert alert = new Alert(AlertType.WARNING);
            alert.setTitle("Warning");
            alert.setHeaderText("No Title!");
            alert.setContentText("Please type a title");
            alert.showAndWait();
            return false;
        }
        final Date createdDate = new Date();
        if (isEdit()) {
            songController.removeSongFromList(selectedSong);
            editingSong = songService.getFromMemoryOrSong(editingSong);
            songVerseService.delete(editingSong.getVerses());
            newSong = editingSong;
        } else {
            newSong = new Song();
            newSong.setCreatedDate(createdDate);
            selectedLanguage.getSongs().add(newSong);
        }
        newSong.setLanguage(selectedLanguage);
        newSong.setTitle(titleTextField.getText().trim());
        setVerseOrderForSong(newSong);
        newSong.setModifiedDate(createdDate);
        newSong.setPublished(false);
        newSong.setPublish(uploadCheckBox.isSelected());
        if (verseEditorRadioButton.isSelected()) {
            newSong.setVerses(getVerses());
        }
        try {
            songService.create(newSong);
        } catch (ServiceException e) {
            Alert alert = new Alert(AlertType.WARNING);
            alert.setTitle("Warning");
            alert.setHeaderText("Could not save song!");
            alert.setContentText("Please try again later");
            alert.showAndWait();
            return false;
        }
        stage.close();
        stage2.close();
        return true;
    }

    private void setVerseOrderForSong(Song song) {
        ObservableList<DraggableEntity<SongVerse>> verseOrderListViewItems = verseOrderListView.getItems();
        List<Short> verseOrderList = new ArrayList<>(verseOrderListViewItems.size());
        for (DraggableEntity<SongVerse> songVerse : verseOrderListViewItems) {
            verseOrderList.add(songVerse.getEntity().getVerseOrderIndex());
        }
        song.setVerseOrderList(verseOrderList);
    }

    void setTitleTextFieldText(String text) {
        titleTextField.setText(text);
    }

    void setEditingSong(Song selectedSong) {
        editingSong = new Song(selectedSong);
        textAreas.getChildren().clear();
        verseControllers.clear();
        verseOrderListView.getItems().clear();
        for (SongVerse songVerse : editingSong.getVerses()) {
            if (!songVerse.isRepeated()) {
                addNewSongVerse(songVerse);
            }
        }
        fillVerseOrder(editingSong.getSongVersesByVerseOrder());
        calculateSameOrder();
        edit = true;
    }

    private void calculateSameOrder() {
        List<SongVerse> songVerses = calculateOrder();
        setVerseOrderForSong(editingSong);
        List<SongVerse> songVersesByVerseOrder = editingSong.getSongVersesByVerseOrder();
        sameAsCalculatedOrder = songVersesListsSameOrder(songVerses, songVersesByVerseOrder);
    }

    private void addNewSongVerse(SongVerse songVerse) {
        songVerse.setMainSong(editingSong);
        ObservableList<Node> textAreasChildren = textAreas.getChildren();
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(MainDesktop.class.getResource("/view/song/Verse.fxml"));
            loader.setResources(settings.getResourceBundle());
            Pane root = loader.load();
            VerseController verseController = loader.getController();
            verseController.setSongVerse(songVerse);
            verseController.setOnChangeListener(new OnChangeListener() {
                @Override
                public void onChange() {
                    if (sameAsCalculatedOrder) {
                        fillVerseOrder(calculateOrder());
                    } else {
                        fillVerseOrder(editingSong.getSongVersesByVerseOrder());
                    }
                    setVerseOrderForSong(editingSong);
                    invalidateVerseControllers();
                }

                @Override
                public void onAddToVerseButton() {
                    addVerseToVerseOrder(songVerse);
                    setVerseOrderForSong(editingSong);
                }
            });
            verseControllers.add(verseController);
            final TextArea textArea = verseController.getTextArea();
            textArea.focusedProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue) {
                    previewProjectionScreenController.setText2(verseController.getRawText(), ProjectionType.SONG);
                    lastFocusedVerse = verseController;
                }
            });
            textArea.textProperty().addListener((observable, oldValue, newValue) -> previewProjectionScreenController.setText2(verseController.getRawText(), ProjectionType.SONG));
            TextArea secondTextArea = verseController.getSecondTextArea();
            secondTextArea.focusedProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue) {
                    previewProjectionScreenController.setText2(secondTextArea.getText(), ProjectionType.SONG);
                }
            });
            secondTextArea.textProperty().addListener((observable, oldValue, newValue) -> previewProjectionScreenController.setText2(secondTextArea.getText(), ProjectionType.SONG));
            textAreasChildren.add(root);
        } catch (IOException e) {
            LOG.error(e.getMessage(), e);
        }
    }

    private void invalidateVerseControllers() {
        for (VerseController verseController : verseControllers) {
            verseController.reFillSectionType();
        }
    }

    private List<SongVerse> calculateOrder() {
        List<SongVerse> songVersesByCalculatedOrder = new ArrayList<>();
        SongVerse chorus = null;
        int delta = 1;
        for (VerseController verseController : verseControllers) {
            SongVerse songVerse = verseController.getSongVerse();
            if (songVerse.getSectionType() == SectionType.CHORUS) {
                chorus = songVerse;
                delta = 0;
            } else {
                if (chorus != null && delta > 0) {
                    songVersesByCalculatedOrder.add(chorus);
                }
                ++delta;
            }
            songVersesByCalculatedOrder.add(songVerse);
        }
        int size = songVersesByCalculatedOrder.size();
        SectionType lastAddedVerseType;
        if (size > 0) {
            lastAddedVerseType = songVersesByCalculatedOrder.get(size - 1).getSectionType();
        } else {
            lastAddedVerseType = null;
        }
        if (chorus != null && lastAddedVerseType != SectionType.CHORUS && lastAddedVerseType != SectionType.CODA && delta > 0) {
            songVersesByCalculatedOrder.add(chorus);
        }
        return songVersesByCalculatedOrder;
    }

    private void fillVerseOrder(List<SongVerse> verses) {
        ObservableList<DraggableEntity<SongVerse>> verseOrderListViewItems = verseOrderListView.getItems();
        verseOrderListViewItems.clear();
        int i = 0;
        for (SongVerse songVerse : verses) {
            DraggableEntity<SongVerse> songVerseDraggableEntity = new DraggableEntity<>(songVerse);
            songVerseDraggableEntity.setListViewIndex(i++);
            verseOrderListViewItems.add(songVerseDraggableEntity);
        }
    }

    void setSelectedSong(SearchedSong selectedSong) {
        this.selectedSong = selectedSong;
        Language language = selectedSong.getSong().getLanguage();
        if (language != null) {
            for (Language language1 : languages) {
                if (language1.getId().equals(language.getId())) {
                    languageComboBoxForNewSong.getSelectionModel().select(language1);
                    break;
                }
            }
        }
        uploadCheckBox.setSelected(selectedSong.getSong().isPublish());
    }

    public void uploadButtonOnAction() {
        if (saveSong()) {
            try {
                FXMLLoader loader = new FXMLLoader();
                loader.setLocation(MainDesktop.class.getResource("/view/Login.fxml"));
                loader.setResources(settings.getResourceBundle());
                Pane root = loader.load();
                LoginController loginController = loader.getController();
                Scene scene = new Scene(root);
                URL resource = getClass().getResource("/view/" + settings.getSceneStyleFile());
                if (resource != null) {
                    scene.getStylesheets().add(resource.toExternalForm());
                }
                Stage stage = getAStage(getClass());
                stage.setScene(scene);
                stage.show();
                loginController.addListener(user -> {
                    SongApiBean songApiBean = new SongApiBean();
                    Language byId = ServiceManager.getLanguageService().findById(newSong.getLanguage().getId());
                    newSong.setLanguage(byId);
                    try {
                        final Song song = songApiBean.updateSong(newSong, user);
                        if (song == null) {
                            LOG.info("Cannot update");
                        } else {
                            newSong.setUuid(song.getUuid());
                            newSong.setModifiedDate(song.getModifiedDate());
                            newSong.setPublished(true);
                            songService.create(newSong);
                            songController.addSong(newSong);
                            stage.close();
                        }
                    } catch (ApiException e) {
                        Alert alert = new Alert(AlertType.INFORMATION);
                        final ResourceBundle resourceBundle = settings.getResourceBundle();
                        alert.setTitle("Error");
                        alert.setHeaderText(resourceBundle.getString(e.getMessage()));
                        alert.showAndWait();
                    }
                });
            } catch (Exception e) {
                LOG.error(e.getMessage(), e);
            }
        }
    }

    public void secondTextOnAction() {
        boolean selected = secondTextToggleButton.isSelected();
        for (VerseController verseController : verseControllers) {
            verseController.showSecondText(selected);
        }
    }

    void setNewSong() {
        edit = false;
        editingSong = new Song();
    }

    public void setRoot(Pane root) {
        NewSongController.globalRoot = root;
    }
}
