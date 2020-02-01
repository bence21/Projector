package projector.controller;

import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Slider;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;
import javafx.scene.layout.BorderPane;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import projector.application.Settings;
import projector.application.Updater;
import projector.controller.song.SongController;
import projector.network.TCPClient;
import projector.network.TCPServer;
import projector.remote.RemoteServer;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class SettingsController {
    @FXML
    private ComboBox<String> appearanceComboBox;
    @FXML
    private ToggleButton allowRemoteButton;
    @FXML
    private CheckBox bibleShortNameCheckBox;
    @FXML
    private CheckBox breakLinesCheckbox;
    @FXML
    private Slider breakAfterSlider;
    @FXML
    private Button connectToSharedButton;
    @FXML
    private Button shareOnLocalNetworkButton;
    @FXML
    private CheckBox referenceChapterSorting;
    @FXML
    private CheckBox referenceVerseSorting;
    @FXML
    private ComboBox<String> languageComboBox;
    @FXML
    private BorderPane mainBorderPain;
    @FXML
    private ScrollPane scrollPane;
    @FXML
    private Slider maxFontSlider;
    @FXML
    private CheckBox accentsCheckBox;
    @FXML
    private ColorPicker backgroundColorPicker;
    @FXML
    private ColorPicker progressLineColorPicker;
    @FXML
    private RadioButton progressLinePositionTopRadioButton;
    @FXML
    private RadioButton progressLinePositionBottomRadioButton;
    @FXML
    private ColorPicker colorPicker;
    @FXML
    private RadioButton colorRadioButton;
    @FXML
    private RadioButton imageRadioButton;
    @FXML
    private TextField imagePathTextField;
    @FXML
    private Button imageBrowseButton;
    @FXML
    private CheckBox fastModeCheckBox;
    @FXML
    private Slider slider;
    @FXML
    private CheckBox showReferenceOnlyCheckBox;
    @FXML
    private ComboBox<String> fontWeightComboBox;
    @FXML
    private ListView<Text> listView;
    @FXML
    private CheckBox referenceItalicCheckBox;
    @FXML
    private Spinner<Integer> progressLineThicknessSpinner;
    @FXML
    private TextField topTextBox;
    @FXML
    private TextField leftTextBox;
    @FXML
    private TextField bottomTextBox;
    @FXML
    private TextField rightTextBox;
    private Settings settings;
    private ProjectionScreenController projectionScreenController;

    @FXML
    private CheckBox previewLoadOnStartCheckbox;
    private SongController songController;

    private List<Listener> listeners;
    private boolean initialized = false;
    private Stage stage;

    synchronized void lazyInitialize() {
        if (initialized) {
            return;
        }
        initialized = true;
        settings = Settings.getInstance();
        ToggleGroup group = new ToggleGroup();
        colorRadioButton.setToggleGroup(group);
        imageRadioButton.setToggleGroup(group);
        languageComboBox.getItems().addAll("en", "hu", "ro");
        languageComboBox.setValue(settings.getPreferredLanguage().getLanguage());
        backgroundColorPicker.setValue(Settings.getInstance().getBackgroundColor());
        colorPicker.setValue(Settings.getInstance().getColor());
        progressLineColorPicker.setValue(settings.getProgressLineColor());
        if (Settings.getInstance().isBackgroundImage()) {
            imageRadioButton.setSelected(true);
        } else {
            colorRadioButton.setSelected(true);
        }
        imagePathTextField.setText(Settings.getInstance().getBackgroundImagePath());
        fastModeCheckBox.setSelected(Settings.getInstance().isFastMode());
        slider.setValue(Settings.getInstance().getLineSpace());
        maxFontSlider.setValue(settings.getMaxFont());
        boolean breakLines = settings.isBreakLines();
        breakLinesCheckbox.setSelected(breakLines);
        breakAfterSlider.setDisable(breakLines);
        breakAfterSlider.setValue(settings.getBreakAfter());
        breakLinesCheckbox.selectedProperty().addListener((observable, oldValue, newValue) -> breakAfterSlider.setDisable(newValue));
        String iK = Settings.getInstance().getFont();
        Text tmpK = new Text(iK);
        tmpK.setFont(Font.font(iK));
        listView.getItems().add(tmpK);
        FontWeight fontWeight = Settings.getInstance().getFontWeight();
        addFonts(fontWeight);
        fontWeightComboBox.getItems().add("NORMAL");
        fontWeightComboBox.getItems().add("BOLD");
        // fontWeightComboBox.getItems().add("BLACK");
        // fontWeightComboBox.getItems().add("EXTRA_BOLD");
        // fontWeightComboBox.getItems().add("EXTRA_LIGHT");
        // fontWeightComboBox.getItems().add("LIGHT");
        // fontWeightComboBox.getItems().add("MEDIUM");
        // fontWeightComboBox.getItems().add("SEMI_BOLD");
        // fontWeightComboBox.getItems().add("THIN");
        fontWeightComboBox.getSelectionModel().select(settings.getFontWeightString());
        fontWeightComboBox.valueProperty().addListener((observable, oldValue, newValue) -> fontWeightValueChange(newValue));
        listView.getSelectionModel().select(0);
        listView.getSelectionModel().selectedItemProperty().addListener(this::changed);
        slider.setMax(10);
        slider.valueChangingProperty().addListener(this::changed);
        showReferenceOnlyCheckBox.setSelected(Settings.getInstance().isShowReferenceOnly());
        referenceItalicCheckBox.setSelected(Settings.getInstance().isReferenceItalic());
        previewLoadOnStartCheckbox.setSelected(settings.isPreviewLoadOnStart());
        referenceChapterSorting.setSelected(settings.isReferenceChapterSorting());
        referenceVerseSorting.setSelected(settings.isReferenceVerseSorting());
        initializeProgressLine();
        initializeNetworkButtons();
        progressLineThicknessSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 10, settings.getProgressLineThickness()));
        bibleShortNameCheckBox.setSelected(settings.getBibleShortName());
        switch (settings.getSceneStyleFile()) {
            case "application.css":
                appearanceComboBox.getSelectionModel().select(0);
                break;
            case "applicationDark.css":
                appearanceComboBox.getSelectionModel().select(1);
                break;
        }
        loadMarginsToTextFields();
    }

    private void initializeNetworkButtons() {
        shareOnLocalNetworkButton.setOnAction(event -> {
            settings.setShareOnNetwork(true);
            TCPServer.startShareNetwork(projectionScreenController, songController);
            connectToSharedButton.setDisable(true);
        });
        connectToSharedButton.setOnAction(event -> TCPClient.connectToShared(projectionScreenController));
        settings.connectedToSharedProperty().addListener((observable, oldValue, newValue) -> shareOnLocalNetworkButton.setDisable(newValue));
        allowRemoteButton.setSelected(settings.isAllowRemote());
    }

    private void initializeProgressLine() {
        ToggleGroup group = new ToggleGroup();
        progressLinePositionTopRadioButton.setToggleGroup(group);
        progressLinePositionBottomRadioButton.setToggleGroup(group);
        if (settings.isProgressLinePositionIsTop()) {
            progressLinePositionTopRadioButton.setSelected(true);
        } else {
            progressLinePositionBottomRadioButton.setSelected(true);
        }
    }

    private synchronized void fontWeightValueChange(String newValue) {
        settings.setFontWeight(newValue);
        listView.getItems().clear();
        String iK1 = Settings.getInstance().getFont();
        Text tmpK1 = new Text(iK1);
        tmpK1.setFont(Font.font(iK1));
        listView.getItems().add(tmpK1);
        FontWeight fontWeight1;
        switch (newValue) {
            case "BOLD":
                fontWeight1 = FontWeight.BOLD;
                break;
            case "BLACK":
                fontWeight1 = FontWeight.BLACK;
                break;
            case "EXTRA_BOLD":
                fontWeight1 = FontWeight.EXTRA_BOLD;
                break;
            case "EXTRA_LIGHT":
                fontWeight1 = FontWeight.EXTRA_LIGHT;
                break;
            case "LIGHT":
                fontWeight1 = FontWeight.LIGHT;
                break;
            case "MEDIUM":
                fontWeight1 = FontWeight.MEDIUM;
                break;
            case "SEMI_BOLD":
                fontWeight1 = FontWeight.SEMI_BOLD;
                break;
            case "THIN":
                fontWeight1 = FontWeight.THIN;
                break;
            default:
                fontWeight1 = FontWeight.NORMAL;
                break;
        }
        addFonts(fontWeight1);
        listView.getSelectionModel().select(0);
        projectionScreenController.reload();
    }

    private void addFonts(FontWeight fontWeight1) {
        for (java.awt.Font i : GraphicsEnvironment.getLocalGraphicsEnvironment().getAllFonts()) {
            Text tmp = new Text(i.getFontName());
            tmp.setFont(Font.font(i.getFontName(), fontWeight1, 20));
            if (!Objects.equals(tmp.getFont().getFamily(), "System") && !i.getFontName().equals("System")) {
                listView.getItems().add(tmp);
            }
        }
    }

    public synchronized void onSaveButtonAction() {
        // settings.setBiblePath(biblePathTextField.getText());
        settings.setMaxFont((int) maxFontSlider.getValue());
        settings.setBreakLines(breakLinesCheckbox.isSelected());
        settings.setBreakAfter((int) breakAfterSlider.getValue());
        settings.setWithAccents(accentsCheckBox.isSelected());
        settings.setBackgroundColor(backgroundColorPicker.getValue());
        settings.setColor(colorPicker.getValue());
        settings.setProgressLineColor(progressLineColorPicker.getValue());
        if (imageRadioButton.isSelected()) {
            settings.setBackgroundImage(true);
            BackgroundImage myBI = new BackgroundImage(new Image(imagePathTextField.getText(), 1024, 768, false, true),
                    BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.DEFAULT,
                    BackgroundSize.DEFAULT);
            settings.setBackgroundImage(myBI);
            settings.setBackgroundImagePath(imagePathTextField.getText().trim());
        } else {
            settings.setBackgroundImage(false);
        }
        settings.setFastMode(fastModeCheckBox.isSelected());
        settings.setFont(listView.getSelectionModel().getSelectedItem().getText());
        settings.setLineSpace(slider.getValue());
        settings.setFontWeight(fontWeightComboBox.getSelectionModel().getSelectedItem());
        settings.setShowReferenceOnly(showReferenceOnlyCheckBox.isSelected());
        settings.setReferenceItalic(referenceItalicCheckBox.isSelected());
        settings.setPreviewLoadOnStart(previewLoadOnStartCheckbox.isSelected());
        settings.setReferenceChapterSorting(referenceChapterSorting.isSelected());
        settings.setReferenceVerseSorting(referenceVerseSorting.isSelected());
        settings.setPreferredLanguage(languageComboBox.getValue());
        settings.setProgressLinePositionIsTop(progressLinePositionTopRadioButton.isSelected());
        Integer value = progressLineThicknessSpinner.getValue();
        if (value > 10) {
            value = 10;
        }
        settings.setProgressLineThickness(value);
        settings.setBibleShortName(bibleShortNameCheckBox.isSelected());
        switch (appearanceComboBox.getValue()) {
            case "Light":
                settings.setSceneStyleFile("application.css");
                break;
            case "Dark":
                settings.setSceneStyleFile("applicationDark.css");
                break;
        }
        setMarginsToSettings(settings);
        settings.save();
        projectionScreenController.setBackGroundColor(backgroundColorPicker.getValue());
        if (listeners != null) {
            for (Listener listener : listeners) {
                listener.onSave();
            }
        }
        stage.hide();
    }

    public synchronized void setSettings(Settings settings) {
        this.settings = settings;
        // biblePathTextField.setText(settings.getBiblePath());
        accentsCheckBox.setSelected(settings.isWithAccents());
    }

    public void onImageBrowseButtonAction() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(Settings.getInstance().getResourceBundle().getString("Chose the image file"));
        fileChooser.setInitialDirectory(new File(new File(".").getAbsolutePath()));
        File selectedFile = fileChooser.showOpenDialog(null);
        if (selectedFile != null) {
            try {
                imagePathTextField.setText(selectedFile.getCanonicalFile().toURI().toString());
                imagePathTextField.positionCaret(imagePathTextField.getText().length() - 1);
                // System.out.println(selectedFile.getAbsolutePath());
                // System.out.println(selectedFile.getParentFile().toURI());
                // System.out.println(selectedFile.getParentFile());
                // System.out.println(selectedFile.getCanonicalPath());
                // System.out.println(selectedFile.getCanonicalFile().toURI());
                // System.out.println(selectedFile.getCanonicalFile());
            } catch (IOException e) {
                e.printStackTrace();
            }
            // System.out.println(selectedFile.getPath());
        }
    }

    synchronized void setProjectionScreenController(ProjectionScreenController projectionScreenController) {
        this.projectionScreenController = projectionScreenController;
    }

    public void onUpdateButtonAction() {
        Updater updater = Updater.getInstance();
        updater.updateExe();
    }

    private void loadMarginsToTextFields() {
        topTextBox.setText(settings.getTopMargin() + "");
        leftTextBox.setText(settings.getLeftMargin() + "");
        rightTextBox.setText(settings.getRightMargin() + "");
        bottomTextBox.setText(settings.getBottomMargin() + "");
    }

    private void setMarginsToSettings(Settings settings) {
        String rightMargin = rightTextBox.getText();
        String leftMargin = leftTextBox.getText();
        String topMargin = topTextBox.getText();
        String bottomMargin = bottomTextBox.getText();
        settings.setTopMargin(Double.parseDouble(topMargin));
        settings.setLeftMargin(Double.parseDouble(leftMargin));
        settings.setRightMargin(Double.parseDouble(rightMargin));
        settings.setBottomMargin(Double.parseDouble(bottomMargin));
    }

    public void setPrefHeight(double d) {
        mainBorderPain.setPrefHeight(d);
        scrollPane.setPrefHeight(d);
    }

    public void setPrefWidth(double d) {
        mainBorderPain.setPrefWidth(d);
        scrollPane.setPrefWidth(d);
    }

    private synchronized void changed(ObservableValue<? extends Text> observable, Text oldValue, Text newValue) {
        if (newValue != null && !newValue.getText().isEmpty()) {
            settings.setFont(newValue.getText());
            projectionScreenController.reload();
        }
    }

    private synchronized void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
        settings.setLineSpace(slider.getValue());
        projectionScreenController.reload();
    }

    public synchronized void setSongController(SongController songController) {
        this.songController = songController;
    }

    public void addOnSaveListener(Listener listener) {
        if (listeners == null) {
            listeners = new ArrayList<>();
        }
        listeners.add(listener);
    }

    public void allowRemoteButtonOnAction() {
        settings.setAllowRemote(allowRemoteButton.isSelected());
        if (settings.isAllowRemote()) {
            RemoteServer.startRemoteServer(projectionScreenController, songController);
        }
    }

    public Stage getStage() {
        return stage;
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public interface Listener {
        void onSave();
    }
}
