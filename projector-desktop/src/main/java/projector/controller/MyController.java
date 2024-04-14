package projector.controller;

import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Menu;
import javafx.scene.control.MultipleSelectionModel;
import javafx.scene.control.SingleSelectionModel;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.ToggleButton;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import org.jnativehook.GlobalScreen;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import projector.MainDesktop;
import projector.application.ApplicationVersion;
import projector.application.ProjectorState;
import projector.application.Settings;
import projector.controller.song.ScheduleController;
import projector.controller.song.SongController;
import projector.model.CustomCanvas;
import projector.network.TCPClient;
import projector.network.TCPServer;
import projector.remote.RemoteServer;
import projector.service.CustomCanvasService;
import projector.utils.BibleVerseTextFlow;
import projector.utils.GlobalKeyListenerExample;
import projector.utils.scene.text.SongVersePartTextFlow;

import java.awt.*;
import java.io.IOException;
import java.util.ResourceBundle;
import java.util.logging.Level;

import static projector.utils.SceneUtils.getCustomStage;

public class MyController {

    private static final Logger LOG = LoggerFactory.getLogger(MyController.class);
    private static MyController instance = null;
    @FXML
    private ToggleButton showProjectionScreenToggleButton;
    @FXML
    private ProjectionScreenController projectionScreenController;
    @FXML
    private BibleController bibleController;
    @FXML
    private BibleSearchController bibleSearchController;

    @FXML
    private SongController songController;
    @FXML
    private RecentController recentController;
    @FXML
    private HistoryController historyController;
    @FXML
    private GalleryController galleryController;
    @FXML
    private UtilsController utilsController;
    @FXML
    private ProjectionScreensController projectionScreensController;
    @SuppressWarnings("FieldCanBeLocal")
    @FXML
    private ScheduleController scheduleController;
    private SettingsController settingsController;
    @FXML
    private Button previewButton;
    @FXML
    private ToggleButton blankButton;
    @FXML
    private Button clearButton;
    @FXML
    private ToggleButton lockButton;
    @FXML
    private TabPane tabPane;
    private Settings settings;
    @FXML
    private Tab songsTab;
    @FXML
    private Tab bibleSearchTab;
    @FXML
    private Tab bibleTab;
    @FXML
    private Tab recentTab;
    @FXML
    private Tab galleryTab;
    @FXML
    private Tab projectionScreensTab;
    private MainDesktop mainDesktop;
    private Stage settingsStage;

    public static MyController getInstance() {
        return instance;
    }

    public static double calculateSizeByScale(double size) {
        double screenScale = screenScale();
        return size / screenScale;
    }

    public static double scaleByPrimaryScreen(double x) {
        return x * screenScale();
    }

    private static double screenScale() {
        try {
            double screenResolution = Toolkit.getDefaultToolkit().getScreenResolution();
            return screenResolution / 96;
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            return 1;
        }
    }

    public void setPrimaryStage(Stage primaryStage) {
        projectionScreenController.setPrimaryStage(primaryStage);
        primaryStage.toFront();
        primaryStage.requestFocus();
    }

    public ProjectionScreenController getProjectionScreenController() {
        return projectionScreenController;
    }

    public void setProjectionScreenController(ProjectionScreenController projectionScreenController) {
        this.projectionScreenController = projectionScreenController;
        bibleController.setProjectionScreenController(projectionScreenController);
        songController.setProjectionScreenController(projectionScreenController);
        settingsController.setProjectionScreenController(projectionScreenController);
        settingsController.setSongController(songController);
        utilsController.setProjectionScreenController(projectionScreenController);
        projectionScreenController.setBlank(true);
        projectionScreenController.setGalleryController(galleryController);
        if (settings.isAllowRemote()) {
            RemoteServer.startRemoteServer(projectionScreenController, songController);
        }
        //initializeGlobalKeyListener(projectionScreenController);
        automaticNetworks();
    }

    @SuppressWarnings("unused")
    private void initializeGlobalKeyListener(ProjectionScreenController projectionScreenController) {
        try {
            GlobalScreen.registerNativeHook();
            GlobalKeyListenerExample nativeKeyListener = new GlobalKeyListenerExample();
            nativeKeyListener.setProjectionScreenController(projectionScreenController);
            GlobalScreen.addNativeKeyListener(nativeKeyListener);
            java.util.logging.Logger logger = java.util.logging.Logger.getLogger(GlobalScreen.class.getPackage().getName());
            logger.setLevel(Level.OFF);
        } catch (Exception ex) {
            System.err.println("There was a problem registering the native hook.");
            System.err.println(ex.getMessage());
        }
    }

    public void initialize() {
        instance = this;
        settings = Settings.getInstance();
        settings.setBibleController(bibleController);
        bibleSearchController.setBibleController(bibleController);
        bibleSearchController.setMainController(this);
        bibleController.setMainController(this);
        songController.setMainController(this);
        bibleController.setBibleSearchController(bibleSearchController);
        bibleController.setRecentController(recentController);
        bibleController.setHistoryController(historyController);
        songController.setRecentController(recentController);
        scheduleController = new ScheduleController();
        songController.setScheduleController(scheduleController);
        recentController.setSongController(songController);
        recentController.setBibleController(bibleController);
        scheduleController.setSongController(songController);
        historyController.setBibleController(bibleController);
        blankButton.setFocusTraversable(false);
        clearButton.setFocusTraversable(false);
        lockButton.setFocusTraversable(false);
        previewButton.setFocusTraversable(false);
        blankButton.setSelected(false);
        SingleSelectionModel<Tab> tabPaneSelectionModel = tabPane.getSelectionModel();
        tabPaneSelectionModel.selectedIndexProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.equals(4)) {
                historyController.loadRecents();
            }
        });
        tabPaneSelectionModel.selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.equals(songsTab)) {
                songController.lazyInitialize();
            } else if (newValue.equals(bibleSearchTab)) {
                bibleSearchController.lazyInitialize();
                bibleSearchController.initializeBibles();
            } else if (newValue.equals(bibleTab)) {
                bibleController.lazyInitialize();
                bibleController.initializeBibles();
            } else if (newValue.equals(projectionScreensTab)) {
                projectionScreensController.lazyInitialize(projectionScreensTab);
            } else if (newValue.equals(galleryTab)) {
                galleryController.onTabOpened();
            }
        });
        tabPane.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                tabPane.getTabs().get(tabPane.getSelectionModel().getSelectedIndex()).getContent().requestFocus();
            }
        });
        tabPane.setFocusTraversable(false);
    }

    public void initialTabSelect() {
        tabPane.getSelectionModel().select(songsTab);
    }

    private void automaticNetworks() {
        if (ApplicationVersion.getInstance().isTesting()) {
            return;
        }
        if (settings.isShareOnLocalNetworkAutomatically()) {
            TCPServer.startShareNetwork(projectionScreenController, songController);
        }
        if (settings.isConnectToSharedAutomatically()) {
            TCPClient.connectToShared(projectionScreenController);
        }
    }

    public void initializeSettingsController(Menu settingsMenu) {
        try {
            initializeSettingsController_(settingsMenu);
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
    }

    private void initializeSettingsController_(Menu settingsMenu) {
        ResourceBundle resourceBundle = settings.getResourceBundle();
        String title = resourceBundle.getString("Settings");
        Label menuLabel = new Label(title);
        menuLabel.setOnMouseClicked(event -> onSettingsMenu());
        settingsMenu.setText("");
        settingsMenu.setGraphic(menuLabel);
        settingsMenu.setOnAction(event -> onSettingsMenu());
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource("/view/Settings.fxml"));
        loader.setResources(resourceBundle);
        try {
            Pane root = loader.load();
            GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
            int height = gd.getDisplayMode().getHeight();
            Scene scene = new Scene(root);
            settingsStage = getCustomStage(getClass(), scene);
            settingsStage.setWidth(850);
            settingsStage.setHeight(calculateSizeByScale(height - 100));
            settingsStage.setTitle(title);
            settingsController = loader.getController();
            settingsController.setStage(settingsStage);
            settingsController.setSettings(settings);
            bibleController.setSettingsController(settingsController);
        } catch (IOException ignored) {
        }
    }

    public void setBlank() {
        blankButton.setSelected(!blankButton.isSelected());
        try {
            blankButtonOnAction();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void setBlank(boolean selected) {
        if (blankButton.isSelected() != selected) {
            blankButton.setSelected(selected);
            projectionScreenController.setBlank(selected);
            songController.onBlankButtonSelected(selected);
        }
    }

    public void previewButtonOnAction() {
        projectionScreenController.createPreview();
    }

    public void blankButtonOnAction() {
        projectionScreenController.setBlank(blankButton.isSelected());
        // recentController.setBlank(blankButton.isSelected());
        songController.onBlankButtonSelected(blankButton.isSelected());
    }

    public void lockButtonOnAction() {
        projectionScreenController.setLock(lockButton.isSelected());
        final ResourceBundle resourceBundle = settings.getResourceBundle();
        if (lockButton.isSelected()) {
            lockButton.setText(resourceBundle.getString("Unlock"));
        } else {
            lockButton.setText(resourceBundle.getString("Lock"));
        }
    }

    public void close() {
        recentController.close();
        songController.onClose();
        bibleController.onClose();
        if (settings.isShareOnNetwork()) {
            TCPServer.close();
        }
        if (settings.isConnectedToShared()) {
            TCPClient.close();
        }
        if (settings.isAllowRemote()) {
            RemoteServer.close();
        }
    }

    public void selectTab(int tabIndex) {
        tabPane.getSelectionModel().select(tabIndex - 1);
    }

    public void setSelecting(boolean isSelecting) {
        bibleController.setSelecting(isSelecting);
    }

    ToggleButton getBlankButton() {
        return blankButton;
    }

    public void goPrev() {
        int selectedIndex = tabPane.getSelectionModel().getSelectedIndex();
        if (selectedIndex == 0) {
            MultipleSelectionModel<BibleVerseTextFlow> selectionModel = bibleController.getVerseListView().getSelectionModel();
            int selectionModelSelectedIndex = selectionModel.getSelectedIndex();
            if (selectionModelSelectedIndex - 1 >= 0) {
                selectionModel.clearAndSelect(selectionModelSelectedIndex - 1);
            }
        } else if (selectedIndex == 2) {
            MultipleSelectionModel<SongVersePartTextFlow> selectionModel = songController.getSongListView().getSelectionModel();
            int selectionModelSelectedIndex = selectionModel.getSelectedIndex();
            if (selectionModelSelectedIndex - 1 >= 0) {
                selectionModel.clearAndSelect(selectionModelSelectedIndex - 1);
            }
        } else if (tabPane.getSelectionModel().getSelectedItem() == galleryTab) {
            galleryController.setPrevious();
        }
    }

    public void goNext() {
        int selectedIndex = tabPane.getSelectionModel().getSelectedIndex();
        if (selectedIndex == 0) {
            ListView<BibleVerseTextFlow> verseListView = bibleController.getVerseListView();
            MultipleSelectionModel<BibleVerseTextFlow> selectionModel = verseListView.getSelectionModel();
            if (selectionModel.getSelectedIndex() + 1 < verseListView.getItems().size()) {
                selectionModel.clearAndSelect(selectionModel.getSelectedIndex() + 1);
            }
        } else if (selectedIndex == 2) {
            ListView<SongVersePartTextFlow> songListView = songController.getSongListView();
            MultipleSelectionModel<SongVersePartTextFlow> selectionModel = songListView.getSelectionModel();
            if (selectionModel.getSelectedIndex() + 1 < songListView.getItems().size()) {
                selectionModel.clearAndSelect(selectionModel.getSelectedIndex() + 1);
            }
            songController.selectNextSongFromScheduleIfLastIndex();
        } else if (tabPane.getSelectionModel().getSelectedItem() == galleryTab) {
            galleryController.setNext();
        }
    }

    public void duplicateCanvas() {
        projectionScreenController.duplicate();
    }

    public void previewCanvas() {
        projectionScreenController.createPreview();
    }

    public void onKeyPressed(KeyEvent event) {
        songController.onKeyPressed(event);
    }

    public void showHideProjectionScreen() {
        if (showProjectionScreenToggleButton.isSelected()) {
            mainDesktop.setProjectionScreenStage(true);
        } else {
            mainDesktop.hideProjectionScreen();
        }
    }

    public void setMain(MainDesktop mainDesktop) {
        this.mainDesktop = mainDesktop;
    }

    public EventHandler<KeyEvent> globalKeyEventHandler() {
        return event -> {
            try {
                if (event.getCode() == KeyCode.F1) {
                    setBlank();
                    event.consume();
                }
            } catch (Exception e) {
                LOG.error(e.getMessage(), e);
            }
        };
    }

    public SettingsController getSettingsController() {
        return settingsController;
    }

    public void onSettingsMenu() {
        settingsController.lazyInitialize();
        settingsStage.show();
        settingsStage.toFront();
    }

    public BibleController getBibleController() {
        return bibleController;
    }

    public SongController getSongController() {
        return songController;
    }

    public void createCustomCanvas() {
        projectionScreenController.createCustomStageWithIterator(CustomCanvasService.getInstance().getCustomCanvases().iterator());
    }

    public void createCustomCanvasStage(CustomCanvas customCanvas) {
        projectionScreenController.createNewCustomStage(customCanvas);
    }

    public void handleKeyPress(KeyEvent event) {
        if (galleryController != null) {
            galleryController.handleKeyPress(event);
        }
    }

    public void setShowProjectionScreenToggleButtonToggle(boolean selected) {
        showProjectionScreenToggleButton.setSelected(selected);
    }

    public void updateProjectorState(ProjectorState projectorState) {
        try {
            projectorState.setBlank(blankButton.isSelected());
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
    }

    public void setByProjectorState(ProjectorState projectorState) {
        setBlank(projectorState.isBlank());
    }

    public void clearButtonOnAction() {
        projectionScreenController.clearAll();
    }
}
