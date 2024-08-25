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
import projector.application.Settings;
import projector.controller.song.util.ScheduleSong;
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
import static projector.utils.ColorUtil.getVisitedTextColor;
import static projector.utils.ContextMenuUtil.initializeContextMenu;

public class ScheduleController {
    private static final Logger LOG = LoggerFactory.getLogger(ScheduleController.class);
    private final String $id$_ = "$id$ ";
    private final String $uuid$_ = "$uuid$ ";
    private final String prefix = "scheduleListView:move:";
    private final KeyCombination keyAltUp = new KeyCodeCombination(KeyCode.UP, KeyCombination.ALT_DOWN);
    private final KeyCombination keyAltDown = new KeyCodeCombination(KeyCode.DOWN, KeyCombination.ALT_DOWN);
    @FXML
    private ListView<ScheduleSong> listView;
    private SongController songController;
    private int selectedIndex;
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
                    if (item == null) {
                        setGraphic(null);
                    } else if (empty || item.getSong().getTitle() == null) {
                        TextFlow textFlow = setTextFlowsText(item, item.getTextFlow());
                        setGraphic(textFlow);
                        item.setTextFlow(textFlow);
                    } else {
                        Song song = item.getSong();
                        ListCell<ScheduleSong> thisCell = this;
                        setOnDragDetected(event -> {
                            if (getItem() == null) {
                                return;
                            }
                            Dragboard dragboard = startDragAndDrop(TransferMode.MOVE);
                            ClipboardContent content = new ClipboardContent();
                            content.putString(prefix + listView.getSelectionModel().getSelectedIndex());
                            dragboard.setContent(content);
                        });
                        setOnDragEntered(event -> {
                            if (event.getGestureSource() != thisCell &&
                                    event.getDragboard().hasString()) {
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
                                    ScheduleSong scheduleSong = items.get(index);
                                    int otherIndex = item.getListViewIndex();
                                    pauseSelectionListener = true;
                                    if (otherIndex < index) {
                                        for (int i = index; i > otherIndex; --i) {
                                            ScheduleSong element = items.get(i - 1);
                                            element.setListViewIndex(i);
                                            items.set(i, element);
                                        }
                                        scheduleSong.setListViewIndex(otherIndex);
                                        items.set(otherIndex, scheduleSong);
                                    } else if (otherIndex > index) {
                                        for (int i = index; i < otherIndex; ++i) {
                                            ScheduleSong element = items.get(i + 1);
                                            element.setListViewIndex(i);
                                            items.set(i, element);
                                        }
                                        scheduleSong.setListViewIndex(otherIndex);
                                        items.set(otherIndex, scheduleSong);
                                    }
                                    listView.getSelectionModel().clearAndSelect(otherIndex);
                                    pauseSelectionListener = false;
                                }
                                event.setDropCompleted(true);
                            }

                        });

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
                String text = newValue.getSong().getTitle();
                if (!text.isEmpty()) {
                    setTextColor(selectedItem, getVisitedTextColor());
                    songController.selectSong(newValue.getSong());
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
        MenuItem resetHighlightsMenuItem = new MenuItem(Settings.getInstance().getResourceBundle().getString("Reset highlights"));
        cm.getItems().addAll(moveUpMenuItem, moveDownMenuItem, removeMenuItem, saveMenuItem, loadMenuItem, resetHighlightsMenuItem);
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
                        Song byId;
                        if (tmp.startsWith($id$_)) {
                            byId = songService.findById(Long.parseLong(tmp.substring($id$_.length())));
                        } else if (tmp.startsWith($uuid$_)) {
                            byId = songService.findByUuid(tmp.substring($uuid$_.length()));
                        } else {
                            Song byTitle = songService.findByTitle(tmp);
                            if (byTitle != null) {
                                readSongs.add(byTitle);
                            }
                            tmp = br.readLine();
                            continue;
                        }
                        if (byId == null) {
                            Song byTitle = songService.findByTitle(br.readLine());
                            if (byTitle != null) {
                                readSongs.add(byTitle);
                            }
                        } else {
                            br.readLine();
                            readSongs.add(byId);
                        }
                        tmp = br.readLine();
                    }
                    br.close();
                    setSongCollections(readSongs);
                    for (Song song : readSongs) {
                        addSong(song);
                    }
                } catch (IOException e) {
                    LOG.error(e.getMessage(), e);
                }
            }
        });
        resetHighlightsMenuItem.setOnAction(event -> resetHighlights());
        listView.setOnKeyPressed(event -> {
            if (keyAltUp.match(event)) {
                moveUp();
            } else if (keyAltDown.match(event)) {
                moveDown();
            }
        });

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

    public void setSongController(SongController songController) {
        this.songController = songController;
    }

}
