package projector.controller;

import javafx.application.Platform;
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
import projector.application.PdfViewerTabState;
import projector.application.ProjectorState;
import projector.application.SessionAutosave;
import projector.application.VideoViewerTabState;
import projector.controller.util.PdfService;
import projector.application.Settings;
import projector.controller.song.ScheduleController;
import projector.controller.song.SongController;
import projector.controller.util.ProjectionScreensUtil;
import projector.model.CustomCanvas;
import projector.network.TCPClient;
import projector.network.TCPServer;
import projector.remote.RemoteServer;
import projector.service.CustomCanvasService;
import projector.utils.BibleVerseTextFlow;
import projector.utils.GlobalKeyListenerExample;
import projector.utils.scene.text.SongVersePartTextFlow;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.logging.Level;

import static projector.utils.SceneUtils.getCustomStage;

public class MyController {

    public static final String TAB_SONGS = "songs";
    public static final String TAB_BIBLE_SEARCH = "bibleSearch";
    public static final String TAB_BIBLE = "bible";
    public static final String TAB_RECENT = "recent";
    public static final String TAB_GALLERY = "gallery";
    public static final String TAB_PROJECTION_SCREENS = "projectionScreens";
    public static final String TAB_PDF_PREFIX = "pdf:";
    public static final String TAB_VIDEO_PREFIX = "video:";

    private static final Logger LOG = LoggerFactory.getLogger(MyController.class);
    private static MyController instance = null;
    @FXML
    private ToggleButton showProjectionScreenToggleButton;
    private final ProjectionScreensUtil projectionScreensUtil = ProjectionScreensUtil.getInstance();
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
    private final Map<String, Tab> openPdfTabs = new HashMap<>();
    private final Map<String, PdfViewerController> openPdfControllers = new HashMap<>();
    private final Map<String, Tab> openVideoTabs = new HashMap<>();
    private final Map<String, VideoViewerController> openVideoControllers = new HashMap<>();

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
        projectionScreenController.setBlank(true);
        projectionScreenController.setGalleryController(galleryController);
        if (settings.isAllowRemote()) {
            RemoteServer.startRemoteServer(songController);
        }
        //initializeGlobalKeyListener();
        automaticNetworks();
    }

    @SuppressWarnings("unused")
    private void initializeGlobalKeyListener() {
        try {
            GlobalScreen.registerNativeHook();
            GlobalKeyListenerExample nativeKeyListener = new GlobalKeyListenerExample();
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
        blankButton.selectedProperty().addListener((observable, oldValue, newValue) -> {
            SessionAutosave.getInstance().notifySessionChanged();
        });
        SingleSelectionModel<Tab> tabPaneSelectionModel = tabPane.getSelectionModel();
        tabPaneSelectionModel.selectedIndexProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.equals(4)) {
                historyController.loadRecents();
            }
        });
        tabPaneSelectionModel.selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            SessionAutosave.getInstance().notifySessionChanged();
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
            TCPClient.startAutoConnectLoop();
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
            projectionScreensUtil.setBlank(selected);
            songController.onBlankButtonSelected(selected);
        }
    }

    public void previewButtonOnAction() {
        projectionScreenController.createPreview();
    }

    public void blankButtonOnAction() {
        projectionScreensUtil.setBlank(blankButton.isSelected());
        // recentController.setBlank(blankButton.isSelected());
        songController.onBlankButtonSelected(blankButton.isSelected());
    }

    public void lockButtonOnAction() {
        projectionScreensUtil.setLock(lockButton.isSelected());
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
            projectionScreensUtil.onProjectionToggle();
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
            if (scheduleController != null) {
                scheduleController.updateProjectorState(projectorState);
            }
            if (bibleController != null) {
                bibleController.updateProjectorState(projectorState);
            }
            if (galleryController != null) {
                galleryController.updateProjectorState(projectorState);
            }
            savePdfVideoTabsToState(projectorState);
            projectorState.setSelectedTabId(tabToId(tabPane.getSelectionModel().getSelectedItem()));
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
    }

    public void restoreScheduleFromProjectorState(ProjectorState projectorState) {
        if (scheduleController != null) {
            scheduleController.restoreFromProjectorState(projectorState);
        }
    }

    public void restoreGalleryFromProjectorState(ProjectorState projectorState) {
        if (galleryController != null) {
            galleryController.setByProjectorState(projectorState);
        }
    }

    private void savePdfVideoTabsToState(ProjectorState projectorState) {
        List<PdfViewerTabState> pdfTabStates = new ArrayList<>();
        PdfService pdfService = PdfService.getInstance();
        for (String path : openPdfTabs.keySet()) {
            PdfViewerTabState tabState = new PdfViewerTabState();
            tabState.setFilePath(path);
            if (pdfService.hasCurrentPage(path)) {
                tabState.setPageIndex(pdfService.getCurrentPage(path));
            } else {
                tabState.setPageIndex(-1);
            }
            PdfViewerController controller = openPdfControllers.get(path);
            if (controller != null) {
                tabState.setScrollVvalue(controller.getScrollVvalue());
            }
            pdfTabStates.add(tabState);
        }
        projectorState.setOpenPdfTabs(pdfTabStates);
        projectorState.setOpenPdfPaths(new ArrayList<>(openPdfTabs.keySet()));

        List<VideoViewerTabState> videoTabStates = new ArrayList<>();
        for (String path : openVideoTabs.keySet()) {
            VideoViewerController controller = openVideoControllers.get(path);
            if (controller != null) {
                videoTabStates.add(controller.captureSessionState(path));
            } else {
                VideoViewerTabState tabState = new VideoViewerTabState();
                tabState.setFilePath(path);
                videoTabStates.add(tabState);
            }
        }
        projectorState.setOpenVideoTabs(videoTabStates);
        projectorState.setOpenVideoPaths(new ArrayList<>(openVideoTabs.keySet()));
    }

    public void setByProjectorState(ProjectorState projectorState) {
        setBlank(projectorState.isBlank());
        restoreTabsFromProjectorState(projectorState);
    }

    private String tabToId(Tab tab) {
        if (tab == null) {
            return TAB_SONGS;
        }
        if (tab == songsTab) {
            return TAB_SONGS;
        }
        if (tab == bibleSearchTab) {
            return TAB_BIBLE_SEARCH;
        }
        if (tab == bibleTab) {
            return TAB_BIBLE;
        }
        if (tab == recentTab) {
            return TAB_RECENT;
        }
        if (tab == galleryTab) {
            return TAB_GALLERY;
        }
        if (tab == projectionScreensTab) {
            return TAB_PROJECTION_SCREENS;
        }
        for (Map.Entry<String, Tab> entry : openPdfTabs.entrySet()) {
            if (entry.getValue() == tab) {
                return TAB_PDF_PREFIX + entry.getKey();
            }
        }
        for (Map.Entry<String, Tab> entry : openVideoTabs.entrySet()) {
            if (entry.getValue() == tab) {
                return TAB_VIDEO_PREFIX + entry.getKey();
            }
        }
        return TAB_SONGS;
    }

    private Tab idToTab(String tabId) {
        if (tabId == null) {
            return songsTab;
        }
        if (TAB_SONGS.equals(tabId)) {
            return songsTab;
        }
        if (TAB_BIBLE_SEARCH.equals(tabId)) {
            return bibleSearchTab;
        }
        if (TAB_BIBLE.equals(tabId)) {
            return bibleTab;
        }
        if (TAB_RECENT.equals(tabId)) {
            return recentTab;
        }
        if (TAB_GALLERY.equals(tabId)) {
            return galleryTab;
        }
        if (TAB_PROJECTION_SCREENS.equals(tabId)) {
            return projectionScreensTab;
        }
        if (tabId.startsWith(TAB_PDF_PREFIX)) {
            return openPdfTabs.get(tabId.substring(TAB_PDF_PREFIX.length()));
        }
        if (tabId.startsWith(TAB_VIDEO_PREFIX)) {
            return openVideoTabs.get(tabId.substring(TAB_VIDEO_PREFIX.length()));
        }
        return songsTab;
    }

    private void restoreTabsFromProjectorState(ProjectorState projectorState) {
        List<PdfViewerTabState> pdfTabStates = projectorState.getOpenPdfTabs();
        if (pdfTabStates != null && !pdfTabStates.isEmpty()) {
            for (PdfViewerTabState tabState : pdfTabStates) {
                restorePdfTab(tabState);
            }
        } else {
            List<String> pdfPaths = projectorState.getOpenPdfPaths();
            if (pdfPaths != null) {
                for (String path : pdfPaths) {
                    openPdfViewerTab(path);
                }
            }
        }
        List<VideoViewerTabState> videoTabStates = projectorState.getOpenVideoTabs();
        if (videoTabStates != null && !videoTabStates.isEmpty()) {
            for (VideoViewerTabState tabState : videoTabStates) {
                restoreVideoTab(tabState);
            }
        } else {
            List<String> videoPaths = projectorState.getOpenVideoPaths();
            if (videoPaths != null) {
                for (String path : videoPaths) {
                    openVideoViewerTab(path);
                }
            }
        }
        Tab tab = idToTab(projectorState.getSelectedTabId());
        if (tab != null && tabPane.getTabs().contains(tab)) {
            tabPane.getSelectionModel().select(tab);
        }
    }

    private void restorePdfTab(PdfViewerTabState tabState) {
        if (tabState == null || tabState.getFilePath() == null) {
            return;
        }
        String path = tabState.getFilePath();
        if (tabState.getPageIndex() >= 0) {
            PdfService.getInstance().setCurrentPage(path, tabState.getPageIndex());
        }
        openPdfViewerTab(path);
        if (tabState.getScrollVvalue() >= 0) {
            PdfViewerController controller = openPdfControllers.get(path);
            if (controller != null) {
                double scrollVvalue = tabState.getScrollVvalue();
                Platform.runLater(() -> controller.setScrollVvalue(scrollVvalue));
            }
        }
    }

    private void restoreVideoTab(VideoViewerTabState tabState) {
        if (tabState == null || tabState.getFilePath() == null) {
            return;
        }
        openVideoViewerTab(tabState.getFilePath(), tabState);
    }

    public void clearButtonOnAction() {
        projectionScreensUtil.clearAll();
    }

    public void openPdfViewerTab(String filePath) {
        if (!projector.controller.util.PdfService.isPdfFile(filePath)) {
            return;
        }

        // Check if PDF tab is already open
        if (openPdfTabs.containsKey(filePath)) {
            Tab existingTab = openPdfTabs.get(filePath);
            tabPane.getSelectionModel().select(existingTab);
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("/view/PdfViewer.fxml"));
            loader.setResources(Settings.getInstance().getResourceBundle());
            Pane root = loader.load();
            Tab pdfTab = getPdfTab(filePath, loader, root);

            openPdfTabs.put(filePath, pdfTab);
            tabPane.getTabs().add(pdfTab);
            tabPane.getSelectionModel().select(pdfTab);
            SessionAutosave.getInstance().notifySessionChanged();
        } catch (Exception e) {
            LOG.error("Error opening PDF viewer tab for: {}", filePath, e);
        }
    }

    private Tab getPdfTab(String filePath, FXMLLoader loader, Pane root) {
        PdfViewerController controller = loader.getController();
        controller.setPdfFile(filePath);

        File file = new File(filePath);
        String fileName = file.getName();
        Tab pdfTab = new Tab(fileName, root);
        pdfTab.setClosable(true);

        // Store controller reference
        openPdfControllers.put(filePath, controller);

        // Handle tab closing
        pdfTab.setOnClosed(event -> {
            controller.cleanup();
            projector.controller.util.PdfService.getInstance().closeDocument(filePath);
            openPdfTabs.remove(filePath);
            openPdfControllers.remove(filePath);
            SessionAutosave.getInstance().notifySessionChanged();
        });
        return pdfTab;
    }

    public void openVideoViewerTab(String filePath) {
        openVideoViewerTab(filePath, null);
    }

    public void openVideoViewerTab(String filePath, VideoViewerTabState restoreState) {
        if (!GalleryController.isMediaFile(filePath)) {
            return;
        }

        // Check if video tab is already open
        if (openVideoTabs.containsKey(filePath)) {
            Tab existingTab = openVideoTabs.get(filePath);
            tabPane.getSelectionModel().select(existingTab);
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("/view/VideoViewer.fxml"));
            loader.setResources(Settings.getInstance().getResourceBundle());
            Pane root = loader.load();
            Tab videoTab = getVideoTab(filePath, loader, root, restoreState);

            openVideoTabs.put(filePath, videoTab);
            tabPane.getTabs().add(videoTab);
            tabPane.getSelectionModel().select(videoTab);
            SessionAutosave.getInstance().notifySessionChanged();
        } catch (Exception e) {
            LOG.error("Error opening video viewer tab for: {}", filePath, e);
        }
    }

    private Tab getVideoTab(String filePath, FXMLLoader loader, Pane root, VideoViewerTabState restoreState) {
        VideoViewerController controller = loader.getController();
        if (restoreState != null) {
            controller.setPendingSessionRestore(restoreState);
        }
        controller.setVideoFile(filePath);

        File file = new File(filePath);
        String fileName = file.getName();
        Tab videoTab = new Tab(fileName, root);
        videoTab.setClosable(true);

        // Store controller reference
        openVideoControllers.put(filePath, controller);

        // Handle tab closing
        videoTab.setOnClosed(event -> {
            controller.cleanup();
            openVideoTabs.remove(filePath);
            openVideoControllers.remove(filePath);
            SessionAutosave.getInstance().notifySessionChanged();
        });
        return videoTab;
    }

    public void closeAllPdfFiles() {
        // Create a copy of the file paths to avoid concurrent modification
        java.util.List<String> filePaths = new java.util.ArrayList<>(openPdfTabs.keySet());

        // First, shutdown all executor services and wait for them to finish
        // This prevents background threads from accessing closed PDF documents
        for (String filePath : filePaths) {
            PdfViewerController controller = openPdfControllers.get(filePath);
            if (controller != null) {
                controller.cleanup();
            }
        }

        // Now that all rendering tasks are finished, close the documents
        for (String filePath : filePaths) {
            projector.controller.util.PdfService.getInstance().closeDocument(filePath);

            Tab tab = openPdfTabs.get(filePath);
            if (tab != null) {
                tabPane.getTabs().remove(tab);
            }
        }

        openPdfTabs.clear();
        openPdfControllers.clear();

        // Also close any remaining documents in PdfService as a safety measure
        projector.controller.util.PdfService.getInstance().closeAllDocuments();
    }

    public void closeAllVideoFiles() {
        // Create a copy of the file paths to avoid concurrent modification
        java.util.List<String> filePaths = new java.util.ArrayList<>(openVideoTabs.keySet());

        // Cleanup all video controllers
        for (String filePath : filePaths) {
            VideoViewerController controller = openVideoControllers.get(filePath);
            if (controller != null) {
                controller.cleanup();
            }

            Tab tab = openVideoTabs.get(filePath);
            if (tab != null) {
                tabPane.getTabs().remove(tab);
            }
        }

        openVideoTabs.clear();
        openVideoControllers.clear();
    }
}
