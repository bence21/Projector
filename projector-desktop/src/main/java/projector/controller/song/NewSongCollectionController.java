package projector.controller.song;

import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.MultipleSelectionModel;
import javafx.scene.control.RadioButton;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.stage.Stage;
import projector.model.Song;
import projector.model.SongCollection;
import projector.service.ServiceException;
import projector.service.ServiceManager;
import projector.service.SongCollectionService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class NewSongCollectionController {

    private SongCollection oldSongCollection;
    @FXML
    private TextField songCollectionNameTextField;
    @FXML
    private Button applyButton;
    @FXML
    private Button addButton;
    @FXML
    private Button removeButton;
    @FXML
    private ListView<Song> allSongsListView;
    @FXML
    private ListView<Song> currentListView;
    @SuppressWarnings("FieldCanBeLocal")
    @FXML
    private ToggleGroup group;
    @FXML
    private RadioButton allRadioButton;
    @FXML
    private RadioButton notUsedRadioButton;
    @FXML
    private RadioButton usedRadioButton;
    private List<Song> allSongs;
    private List<SongCollection> songCollections;
    private Stage stage;
    private SongController songController;
    private boolean editing;

    public void initialize() {
        try {
            SongCollectionService songCollectionService = ServiceManager.getSongCollectionService();
            songCollections = songCollectionService.findAll();
        } catch (ServiceException ignored) {
        }
        group = new ToggleGroup();
        allRadioButton.setToggleGroup(group);
        notUsedRadioButton.setToggleGroup(group);
        usedRadioButton.setToggleGroup(group);
        allSongsListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        currentListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        allSongsListView.setCellFactory(param -> new ListCell<Song>() {
            @Override
            protected void updateItem(Song item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null || item.getTitle() == null) {
                    setText(null);
                } else {
                    setText(item.getTitle());
                }
            }
        });
        currentListView.setCellFactory(allSongsListView.getCellFactory());
        ObservableList<Song> currentListViewItems = currentListView.getItems();
        ObservableList<Song> allSongsListViewItems = allSongsListView.getItems();
        addButton.setOnAction(event -> swapSelected(allSongsListView.getSelectionModel(), currentListViewItems, allSongsListViewItems));
        removeButton.setOnAction(event -> swapSelected(currentListView.getSelectionModel(), allSongsListViewItems, currentListViewItems));
        applyButton.setOnAction(event -> {
            String title = songCollectionNameTextField.getText();
            SongCollectionService songBookService = ServiceManager.getSongCollectionService();
            if (editing) {
                oldSongCollection.setSongs(currentListViewItems);
                oldSongCollection.setName(title);
                try {
                    songBookService.update(oldSongCollection);
                    System.out.println("SongBook update");
                } catch (ServiceException e) {
                    System.out.println("SongBook not update");
                }
            } else {
                SongCollection songBook = new SongCollection(title);
                songBook.setSongs(currentListViewItems);
                try {
                    songBookService.create(songBook);
                    System.out.println("SongBook created");
                } catch (ServiceException e) {
                    System.out.println("SongBook not created");
                }
            }
            songController.addSongCollections();
            stage.close();
        });
        allRadioButton.setOnAction(event -> {
            allSongsListViewItems.clear();
            allSongsListViewItems.addAll(allSongs);
        });
        notUsedRadioButton.setOnAction(event -> {
            allSongsListViewItems.clear();
            for (Song song : allSongs) {
                if (!currentListViewItems.contains(song) && song.getSongCollections().size() == 0) {
                    allSongsListViewItems.add(song);
                }
            }
        });
        usedRadioButton.setOnAction(event -> {
            allSongsListViewItems.clear();
            for (Song song : allSongs) {
                if (!currentListViewItems.contains(song) && song.getSongCollections().size() == 0) {
                    allSongsListViewItems.add(song);
                }
            }
        });
    }

    private void swapSelected(MultipleSelectionModel<Song> selectionModel, ObservableList<Song> items, ObservableList<Song> items2) {
        ObservableList<Integer> selectedIndices = selectionModel.getSelectedIndices();
        for (Integer selectedIndex : selectedIndices) {
            items.add(items2.get(selectedIndex));
        }
        for (int i = selectedIndices.size() - 1; i >= 0; --i) {
            items2.remove(selectedIndices.get(i).intValue());
        }
        selectionModel.clearSelection();
    }

    public void setSongs(List<Song> songs) {
        allSongs = new ArrayList<>(songs.size());
        allSongs.addAll(songs);
        for (Song song : songs) {
            allSongsListView.getItems().add(song);
        }

        HashMap<String, Song> hashMap = new HashMap<>(songs.size());
        for (Song song : songs) {
            hashMap.put(song.getUuid(), song);
        }
        SongController.setSongCollectionForSongsInHashMap(songCollections, hashMap);
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public void setSongController(SongController songController) {
        this.songController = songController;
    }

    void setEditing(boolean editing, SongCollection selectedItem) {
        this.editing = editing;
        oldSongCollection = selectedItem;
        songCollectionNameTextField.setText(oldSongCollection.getName());
        for (Song song : oldSongCollection.getSongs()) {
            currentListView.getItems().add(song);
        }
    }
}
