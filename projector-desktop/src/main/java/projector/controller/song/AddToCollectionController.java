package projector.controller.song;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import projector.application.Settings;
import projector.model.Song;
import projector.model.SongCollection;
import projector.model.SongCollectionElement;
import projector.service.ServiceManager;
import projector.service.SongCollectionService;

import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;

public class AddToCollectionController {
    @FXML
    private ComboBox<SongCollection> collectionComboBox;
    @FXML
    private TextField ordinalNumberTextField;
    @FXML
    private Button saveButton;
    private Song selectedSong;
    private SongController songController;
    private SongCollectionService songCollectionService;
    private Stage stage;

    public void initialize() {
        songCollectionService = ServiceManager.getSongCollectionService();
        List<SongCollection> songCollections = songCollectionService.findAll();
        collectionComboBox.getItems().addAll(songCollections);
    }

    public void onSaveButton() {
        if (selectedSong.getUuid() == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            ResourceBundle resourceBundle = Settings.getInstance().getResourceBundle();
            alert.setTitle(resourceBundle.getString("Warning"));
            alert.setHeaderText("");
            alert.setContentText(resourceBundle.getString("Song is not uploaded! You must upload first."));
            alert.showAndWait();
            return;
        }
        SongCollectionElement songCollectionElement = new SongCollectionElement();
        songCollectionElement.setOrdinalNumber(ordinalNumberTextField.getText().trim());
        songCollectionElement.setSongUuid(selectedSong.getUuid());
        SongCollection songCollection = collectionComboBox.getSelectionModel().getSelectedItem();
        songCollection.setModifiedDate(new Date());
        songCollection.setNeedUpload(true);
        songCollectionService.create(songCollection);
        songCollectionElement.setSongCollection(songCollection);
        ServiceManager.getSongCollectionElementService().create(songCollectionElement);

        selectedSong.addToSongCollections(songCollection);
        selectedSong.addToSongCollectionElements(songCollectionElement);
        songController.addSongCollections();
        stage.close();
    }

    void setSelectedSong(Song selectedSong) {
        this.selectedSong = selectedSong;
    }

    public void setSongController(SongController songController) {
        this.songController = songController;
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }
}
