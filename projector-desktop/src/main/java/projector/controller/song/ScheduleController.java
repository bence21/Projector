package projector.controller.song;

import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.MultipleSelectionModel;
import javafx.scene.control.SelectionMode;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.MouseButton;
import javafx.scene.input.TransferMode;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import projector.application.ProjectorState;
import projector.application.ScheduleEntryState;
import projector.application.SessionAutosave;
import projector.application.Settings;
import projector.controller.song.util.ScheduleSong;
import projector.controller.util.ProjectionScreensUtil;
import projector.model.Song;
import projector.service.ServiceManager;
import projector.service.SongService;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static projector.controller.BibleController.setGeneralTextColor;
import static projector.controller.song.SongController.setSongCollections;
import static projector.controller.song.SongController.setTextFlowsText;
import static projector.utils.ColorUtil.getGeneralTextColor;
import static projector.utils.ColorUtil.getSubduedTextColor;
import static projector.utils.ColorUtil.getVisitedTextColor;
import static projector.utils.ContextMenuUtil.initializeContextMenu;

public class ScheduleController {
    private static final Logger LOG = LoggerFactory.getLogger(ScheduleController.class);
    private final String $id$_ = "$id$ ";
    private final String $uuid$_ = "$uuid$ ";
    private final String $section$_ = "$section$ ";
    private final String prefix = "scheduleListView:move:";
    private final KeyCombination keyAltUp = new KeyCodeCombination(KeyCode.UP, KeyCombination.ALT_DOWN);
    private final KeyCombination keyAltDown = new KeyCodeCombination(KeyCode.DOWN, KeyCombination.ALT_DOWN);
    @FXML
    private ListView<ScheduleSong> listView;
    private SongController songController;
    private int selectedIndex = -1;
    private boolean pauseSelectionListener = false;

    int getSelectedIndex() {
        return selectedIndex;
    }

    public ListView<ScheduleSong> getListView() {
        return listView;
    }

    public void setListView(ListView<ScheduleSong> listView) {
        this.listView = listView;
    }

    public void initialize() {
        listView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        final ObservableList<ScheduleSong> items = listView.getItems();
        listView.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(ScheduleSong item, boolean empty) {
                try {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setGraphic(null);
                    } else if (item.isSection()) {
                        applySectionRowPresentation(item);
                        setGraphic(item.getTextFlow());
                        configureDragAndDropHandlers(this, item, items);
                    } else if (item.getSong() == null || item.getSong().getTitle() == null) {
                        TextFlow textFlow = setTextFlowsText(item, item.getTextFlow());
                        setGraphic(textFlow);
                        item.setTextFlow(textFlow);
                    } else {
                        Song song = item.getSong();
                        configureDragAndDropHandlers(this, item, items);

                        TextFlow textFlow = item.getTextFlow();
                        if (textFlow == null) {
                            textFlow = new TextFlow();
                            ObservableList<Node> children = textFlow.getChildren();
                            Text text = new Text(song.getTitle());
                            setGeneralTextColor(text);
                            children.add(text);
                            item.setText(text);
                        } else {
                            Text text = item.getText();
                            if (text != null) {
                                text.setText(song.getTitle());
                            }
                        }
                        setGraphic(textFlow);
                        item.setTextFlow(textFlow);
                    }
                } catch (Exception e) {
                    LOG.error(e.getMessage(), e);
                }
            }
        });
        listView.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue) {
                listView.getSelectionModel().clearSelection();
            }
        });
        listView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (pauseSelectionListener) {
                return;
            }
            if (listView.getSelectionModel().getSelectedIndex() != -1) {
                selectedIndex = listView.getSelectionModel().getSelectedIndex();
            }
            ScheduleSong selectedItem = listView.getSelectionModel().getSelectedItem();
            if (newValue != null) {
                if (newValue.isSection()) {
                    onSectionScheduleRowSelected(selectedItem);
                } else {
                    onSongScheduleRowSelected(selectedItem, newValue);
                }
            }
            SessionAutosave.getInstance().notifySessionChanged();
        });
        items.addListener((javafx.collections.ListChangeListener.Change<? extends ScheduleSong> change) -> {
            while (change.next()) {
                if (change.wasAdded() || change.wasRemoved() || change.wasPermutated() || change.wasUpdated()) {
                    SessionAutosave.getInstance().notifySessionChanged();
                    break;
                }
            }
        });
        final ContextMenu cm = new ContextMenu();
        initializeContextMenu(cm, LOG);
        MenuItem moveUpMenuItem = new MenuItem(Settings.getInstance().getResourceBundle().getString("Move up"));
        MenuItem moveDownMenuItem = new MenuItem(Settings.getInstance().getResourceBundle().getString("Move down"));
        MenuItem removeMenuItem = new MenuItem(Settings.getInstance().getResourceBundle().getString("Remove"));
        MenuItem saveMenuItem = new MenuItem(Settings.getInstance().getResourceBundle().getString("Save"));
        MenuItem loadMenuItem = new MenuItem(Settings.getInstance().getResourceBundle().getString("Load"));
        MenuItem importPasteMenuItem = new MenuItem(Settings.getInstance().getResourceBundle().getString("Import schedule from clipboard"));
        MenuItem resetHighlightsMenuItem = new MenuItem(Settings.getInstance().getResourceBundle().getString("Reset highlights"));
        cm.getItems().addAll(moveUpMenuItem, moveDownMenuItem, removeMenuItem, saveMenuItem, loadMenuItem, importPasteMenuItem, resetHighlightsMenuItem);
        listView.setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.SECONDARY) {
                cm.show(listView, event.getScreenX(), event.getScreenY());
            } else {
                cm.hide();
            }
        });
        moveUpMenuItem.setOnAction(event -> moveUp());
        moveDownMenuItem.setOnAction(event -> moveDown());
        removeMenuItem.setOnAction(event -> {
            int selectedIndex = listView.getSelectionModel().getSelectedIndex();
            if (selectedIndex >= 0) {
                listView.getSelectionModel().clearSelection();
                items.remove(selectedIndex);
            }
        });
        saveMenuItem.setOnAction(event -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle(Settings.getInstance().getResourceBundle().getString("Choose a file"));
            fileChooser.getExtensionFilters().add(new ExtensionFilter("text", "*.txt"));
            fileChooser.setInitialDirectory(new File(new File(".").getAbsolutePath()));
            File selectedFile = fileChooser.showSaveDialog(null);
            if (selectedFile != null) {
                FileOutputStream ofStream;
                try {
                    ofStream = new FileOutputStream(selectedFile);
                    BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(ofStream, StandardCharsets.UTF_8));
                    for (ScheduleSong i : items) {
                        if (i.isSection()) {
                            bw.write($section$_ + i.getSectionLabel() + System.lineSeparator());
                            continue;
                        }
                        Song song = i.getSong();
                        if (song.getUuid() == null) {
                            bw.write($id$_ + song.getId() + System.lineSeparator());
                        } else {
                            bw.write($uuid$_ + song.getUuid() + System.lineSeparator());
                        }
                        bw.write(song.getTitle() + System.lineSeparator());
                    }
                    bw.close();
                } catch (IOException e) {
                    LOG.error(e.getMessage(), e);
                }
            }
        });
        loadMenuItem.setOnAction(event -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle(Settings.getInstance().getResourceBundle().getString("Choose a file"));
            fileChooser.getExtensionFilters().add(new ExtensionFilter("text", "*.txt"));
            fileChooser.setInitialDirectory(new File(new File(".").getAbsolutePath()));
            File selectedFile = fileChooser.showOpenDialog(null);
            if (selectedFile != null) {
                FileInputStream ifStream;
                try {
                    ifStream = new FileInputStream(selectedFile);
                    BufferedReader br = new BufferedReader(new InputStreamReader(ifStream, StandardCharsets.UTF_8));
                    items.clear();
                    SongService songService = ServiceManager.getSongService();
                    String tmp = br.readLine();
                    List<Song> readSongs = new ArrayList<>();
                    while (tmp != null) {
                        if (tmp.startsWith($section$_)) {
                            String label = tmp.substring($section$_.length());
                            addSectionRow(label);
                            tmp = br.readLine();
                            continue;
                        }
                        Song byId;
                        if (tmp.startsWith($id$_)) {
                            byId = songService.findById(Long.parseLong(tmp.substring($id$_.length())));
                        } else if (tmp.startsWith($uuid$_)) {
                            byId = songService.findByUuid(tmp.substring($uuid$_.length()));
                        } else {
                            Song byTitle = songService.findByTitle(tmp);
                            if (byTitle != null) {
                                addLoadedSong(readSongs, byTitle);
                            }
                            tmp = br.readLine();
                            continue;
                        }
                        if (byId == null) {
                            Song byTitle = songService.findByTitle(br.readLine());
                            if (byTitle != null) {
                                addLoadedSong(readSongs, byTitle);
                            }
                        } else {
                            br.readLine();
                            addLoadedSong(readSongs, byId);
                        }
                        tmp = br.readLine();
                    }
                    br.close();
                    setSongCollections(readSongs);
                } catch (IOException e) {
                    LOG.error(e.getMessage(), e);
                }
            }
        });
        importPasteMenuItem.setOnAction(event -> SchedulePasteImportDialog.show(this, songController));
        resetHighlightsMenuItem.setOnAction(event -> resetHighlights());
        listView.setOnKeyPressed(event -> {
            if (keyAltUp.match(event)) {
                moveUp();
            } else if (keyAltDown.match(event)) {
                moveDown();
            }
        });

    }

    private void applySectionRowPresentation(ScheduleSong item) {
        TextFlow textFlow = item.getTextFlow();
        if (textFlow == null) {
            textFlow = new TextFlow();
            Text text = new Text(item.getSectionLabel());
            text.setFill(getSubduedTextColor());
            text.setStyle("-fx-font-style: italic;");
            textFlow.getChildren().add(text);
            item.setText(text);
            item.setTextFlow(textFlow);
        } else {
            Text text = item.getText();
            if (text != null) {
                text.setText(item.getSectionLabel());
                text.setFill(getSubduedTextColor());
                text.setStyle("-fx-font-style: italic;");
            }
        }
    }

    private void configureDragAndDropHandlers(ListCell<ScheduleSong> cell, ScheduleSong item, ObservableList<ScheduleSong> items) {
        cell.setOnDragDetected(event -> {
            if (cell.getItem() == null) {
                return;
            }
            Dragboard dragboard = cell.startDragAndDrop(TransferMode.MOVE);
            ClipboardContent content = new ClipboardContent();
            content.putString(prefix + listView.getSelectionModel().getSelectedIndex());
            dragboard.setContent(content);
        });
        cell.setOnDragEntered(event -> {
            if (event.getGestureSource() != cell && event.getDragboard().hasString()) {
                cell.setOpacity(0.3);
            }
        });
        cell.setOnDragExited(event -> {
            if (event.getGestureSource() != cell && event.getDragboard().hasString()) {
                cell.setOpacity(1);
            }
        });
        cell.setOnDragOver(dragEvent -> dragEvent.acceptTransferModes(TransferMode.MOVE, TransferMode.COPY, TransferMode.LINK));
        cell.setOnDragDropped(event -> {
            if (cell.getItem() == null) {
                return;
            }
            Dragboard dragboard = event.getDragboard();
            if (dragboard.hasString()) {
                int index = getIndexFromDragBoard(dragboard);
                if (index != -1) {
                    reorderScheduleItems(items, item, index);
                }
                event.setDropCompleted(true);
            }
        });
    }

    private void reorderScheduleItems(ObservableList<ScheduleSong> items, ScheduleSong targetItem, int draggedIndex) {
        ScheduleSong draggedItem = items.get(draggedIndex);
        int targetIndex = targetItem.getListViewIndex();
        pauseSelectionListener = true;
        if (targetIndex < draggedIndex) {
            for (int i = draggedIndex; i > targetIndex; --i) {
                ScheduleSong element = items.get(i - 1);
                element.setListViewIndex(i);
                items.set(i, element);
            }
            draggedItem.setListViewIndex(targetIndex);
            items.set(targetIndex, draggedItem);
        } else if (targetIndex > draggedIndex) {
            for (int i = draggedIndex; i < targetIndex; ++i) {
                ScheduleSong element = items.get(i + 1);
                element.setListViewIndex(i);
                items.set(i, element);
            }
            draggedItem.setListViewIndex(targetIndex);
            items.set(targetIndex, draggedItem);
        }
        listView.getSelectionModel().clearAndSelect(targetIndex);
        pauseSelectionListener = false;
    }

    private void onSectionScheduleRowSelected(ScheduleSong selectedItem) {
        setTextColor(selectedItem, getVisitedTextColor());
        handleNextScheduled();
    }

    private void onSongScheduleRowSelected(ScheduleSong selectedItem, ScheduleSong row) {
        Song song = row.getSong();
        String title = song != null ? song.getTitle() : "";
        if (title != null && !title.isEmpty()) {
            setTextColor(selectedItem, getVisitedTextColor());
            songController.selectSong(song);
            handleNextScheduled();
        }
    }

    private void handleNextScheduled() {
        Song nextScheduledSong = getNextScheduledSong();
        ProjectionScreensUtil.getInstance().setNextScheduledSong(nextScheduledSong);
    }

    private Song getNextScheduledSong() {
        int selectedIndex = listView.getSelectionModel().getSelectedIndex();
        ObservableList<ScheduleSong> items = listView.getItems();
        int nextIndex = selectedIndex + 1;
        while (nextIndex >= 0 && nextIndex < items.size()) {
            ScheduleSong row = items.get(nextIndex);
            if (!row.isSection() && row.getSong() != null) {
                return row.getSong();
            }
            nextIndex++;
        }
        return null;
    }

    private void resetHighlights() {
        for (ScheduleSong scheduleSong : listView.getItems()) {
            setTextColor(scheduleSong, getGeneralTextColor());
        }
    }

    private int getIndexFromDragBoard(Dragboard dragboard) {
        String string = dragboard.getString();
        if (string.startsWith(prefix)) {
            return Integer.parseInt(string.replace(prefix, ""));
        }
        return -1;
    }

    private void setTextColor(ScheduleSong selectedItem, Color color) {
        TextFlow textFlow = selectedItem.getTextFlow();
        if (textFlow != null) {
            for (Node node : textFlow.getChildren()) {
                Text text1 = (Text) node;
                text1.setFill(color);
            }
        }
    }

    private void moveUp() {
        int selectedIndex = listView.getSelectionModel().getSelectedIndex();
        if (selectedIndex > 0) {
            moveByIndex(selectedIndex, selectedIndex - 1);
        }
    }

    private void moveDown() {
        int selectedIndex = listView.getSelectionModel().getSelectedIndex();
        if (0 <= selectedIndex && selectedIndex < listView.getItems().size() - 1) {
            moveByIndex(selectedIndex, selectedIndex + 1);
        }
    }

    private void moveByIndex(int selectedIndex, int newIndex) {
        pauseSelectionListener = true;
        MultipleSelectionModel<ScheduleSong> selectionModel = listView.getSelectionModel();
        ObservableList<ScheduleSong> items = listView.getItems();
        ScheduleSong scheduleSongOnNewIndex = items.get(newIndex);
        scheduleSongOnNewIndex.setListViewIndex(selectedIndex);
        ScheduleSong scheduleSong = items.get(selectedIndex);
        items.remove(selectedIndex);
        items.add(newIndex, scheduleSong);
        scheduleSong.setListViewIndex(newIndex);
        selectionModel.clearAndSelect(newIndex);
        pauseSelectionListener = false;
    }

    void addSong(Song song) {
        if (song != null) {
            ScheduleSong scheduleSong = new ScheduleSong(song);
            scheduleSong.setListViewIndex(listView.getItems().size());
            listView.getItems().add(scheduleSong);
        }
    }

    private void addLoadedSong(List<Song> readSongs, Song song) {
        if (song != null) {
            readSongs.add(song);
            addSong(song);
        }
    }

    void addSectionRow(String sectionLabel) {
        if (sectionLabel == null || sectionLabel.isEmpty()) {
            return;
        }
        ScheduleSong row = new ScheduleSong(sectionLabel);
        row.setListViewIndex(listView.getItems().size());
        listView.getItems().add(row);
    }

    public void setSongController(SongController songController) {
        this.songController = songController;
    }

    public void updateProjectorState(ProjectorState projectorState) {
        if (listView == null) {
            return;
        }
        List<ScheduleEntryState> entries = new ArrayList<>();
        for (ScheduleSong item : listView.getItems()) {
            if (item.isSection()) {
                entries.add(ScheduleEntryState.section(item.getSectionLabel()));
            } else if (item.getSong() != null) {
                entries.add(ScheduleEntryState.song(item.getSong()));
            }
        }
        projectorState.setScheduleEntries(entries);
        projectorState.setScheduleSelectedIndex(selectedIndex);
    }

    public void restoreFromProjectorState(ProjectorState projectorState) {
        if (listView == null || projectorState.getScheduleEntries() == null) {
            return;
        }
        pauseSelectionListener = true;
        try {
            ObservableList<ScheduleSong> items = listView.getItems();
            items.clear();
            SongService songService = ServiceManager.getSongService();
            List<Song> readSongs = new ArrayList<>();
            for (ScheduleEntryState entry : projectorState.getScheduleEntries()) {
                if (ScheduleEntryState.TYPE_SECTION.equals(entry.getType())) {
                    addSectionRow(entry.getSectionLabel());
                } else if (ScheduleEntryState.TYPE_SONG.equals(entry.getType())) {
                    Song song = null;
                    if (entry.getSongUuid() != null) {
                        song = songService.findByUuid(entry.getSongUuid());
                    } else if (entry.getSongId() != null) {
                        song = songService.findById(entry.getSongId());
                    }
                    if (song != null) {
                        addLoadedSong(readSongs, song);
                    }
                }
            }
            if (!readSongs.isEmpty()) {
                setSongCollections(readSongs);
            }
            int index = projectorState.getScheduleSelectedIndex();
            if (index >= 0 && index < items.size()) {
                selectedIndex = index;
                listView.getSelectionModel().clearAndSelect(index);
            }
            handleNextScheduled();
        } finally {
            pauseSelectionListener = false;
        }
    }

}
