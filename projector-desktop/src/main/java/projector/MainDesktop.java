package projector;

import com.google.gson.Gson;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Rectangle2D;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Menu;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Popup;
import javafx.stage.Screen;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import projector.application.ApplicationUtil;
import projector.application.ApplicationVersion;
import projector.application.ProjectionScreenSettings;
import projector.application.Settings;
import projector.application.Updater;
import projector.config.Log4j2Config;
import projector.controller.BibleController;
import projector.controller.FirstSetupController;
import projector.controller.MyController;
import projector.controller.ProjectionScreenController;
import projector.controller.song.SongController;
import projector.controller.util.ProjectionScreenHolder;
import projector.controller.util.ProjectionScreensUtil;
import projector.controller.util.WindowController;
import projector.repository.ormLite.DatabaseHelper;
import projector.service.CustomCanvasService;
import projector.utils.AppProperties;
import projector.utils.AppState;
import projector.utils.monitors.MonitorUtil;

import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.Date;
import java.util.ListIterator;
import java.util.Properties;

import static java.lang.Thread.sleep;
import static projector.utils.HandleUnexpectedError.setDefaultUncaughtExceptionHandler;
import static projector.utils.ProcessUtil.killOtherProcesses;
import static projector.utils.SceneUtils.addIconToStage;
import static projector.utils.SceneUtils.addStylesheetToSceneBySettings;
import static projector.utils.SceneUtils.createWindowController;
import static projector.utils.SceneUtils.getTransparentStage;
import static projector.utils.SceneUtils.getWindowController;

public class MainDesktop extends Application {

    private static final Logger LOG = LoggerFactory.getLogger(MainDesktop.class);
    private static Pane globalRoot;
    private MyController myController;
    private ProjectionScreenController projectionScreenController;
    private final ProjectionScreensUtil projectionScreensUtil = ProjectionScreensUtil.getInstance();
    private Stage tmpStage;
    private Stage primaryStage;
    private Scene primaryScene;
    private Stage canvasStage;
    private ObservableList<Screen> screenObservableList;
    private Settings settings;
    private Date startDate;
    private Screen mainScreen;
    private WindowController windowController;

    public static void main(String[] args) {
        Log4j2Config.getInstance().initializeLog4j2OnMac();
        accessibilityAssistiveTechnologiesProblem();
        if (!killOtherProcesses(!AppState.getInstance().isClosed())) {
            return;
        }
        openState();
        launch(args);
    }

    private static void openState() {
        AppState appState = AppState.getInstance();
        appState.setClosed(false);
        appState.save();
    }

    private static void accessibilityAssistiveTechnologiesProblem() {
        Properties props = System.getProperties();
        // saveToJson(props, "systemProperties.json");
        props.setProperty("javax.accessibility.assistive_technologies", "");
        props.setProperty("java.accessibility.assistive_technologies", "");
        // saveToJson(props, "systemProperties2.json");
    }

    @SuppressWarnings("unused")
    private static void saveToJson(Properties props, String filename) {
        String json = convertPropertiesToJson(props);
        writeJsonToFile(json, filename);
    }

    private static String convertPropertiesToJson(Properties properties) {
        Gson gson = new Gson();
        return gson.toJson(properties);
    }

    private static void writeJsonToFile(String json, String filename) {
        try (FileWriter writer = new FileWriter(filename)) {
            writer.write(json);
        } catch (IOException e) {
            LOG.error(e.getMessage(), e);
        }
    }

    public static Pane getRoot() {
        return globalRoot;
    }

    @Override
    public void start(Stage primaryStage) {
        try {
            this.primaryStage = primaryStage;
            ApplicationUtil.getInstance().setPrimaryStage(primaryStage);
            if (ApplicationVersion.getInstance().getVersion() < 25 &&
                    ApplicationVersion.getInstance().isNotTesting() &&
                    !AppProperties.getInstance().isMacOs()
            ) {
                openFirstSetupView(primaryStage);
            } else {
                openLauncherView(primaryStage);
            }
            setDefaultUncaughtExceptionHandler();
        } catch (Exception e) {
            //noinspection CallToPrintStackTrace
            e.printStackTrace();
            LOG.error(e.getMessage(), e);
        }
    }

    private void openLauncherView(Stage primaryStage) throws IOException {
        startDate = new Date();
        Stage stage = getTransparentStage(getClass());
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource("/view/LauncherView.fxml"));
        BorderPane borderPane = loader.load();
        Scene scene = new Scene(borderPane, borderPane.getPrefWidth(), borderPane.getPrefHeight());
        scene.setFill(Color.TRANSPARENT);
        stage.setScene(scene);
        stage.setTitle("Projector - starting");
        Thread thread = new Thread(() -> {
            try {
                sleep(50);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            Platform.runLater(() -> {
                try {
                    start2(primaryStage);
                } catch (Exception e) {
                    LOG.error(e.getMessage(), e);
                }
                stage.close();
            });
        });
        stage.show();
        if (ApplicationVersion.getInstance().isNotTesting()) {
            thread.start();
        } else {
            start2(primaryStage);
            primaryStage.show();
        }
    }

    private void openFirstSetupView(Stage primaryStage) throws IOException {
        DatabaseHelper.freeze();
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource("/view/FirstSetupView.fxml"));
        BorderPane borderPane = loader.load();
        FirstSetupController firstSetupController = loader.getController();
        firstSetupController.setListener(() -> {
            DatabaseHelper.unfreeze();
            Settings.emptyInstance();
            start2(primaryStage);
        });
        Scene scene = new Scene(borderPane, borderPane.getPrefWidth(), borderPane.getPrefHeight());
        createWindowController(getClass(), scene, primaryStage);
        primaryStage.setWidth(borderPane.getPrefWidth());
        primaryStage.setHeight(borderPane.getPrefHeight());
        primaryStage.setTitle("Projector - setup");
        primaryStage.show();
    }

    public void start2(Stage primaryStage) {
        Settings.shouldBeNull();
        loadInBackGround();
        addIconToStage(primaryStage, getClass());
        primaryStage.setMinHeight(600);
        primaryStage.setWidth(settings.getMainWidth());
        primaryStage.setHeight(settings.getMainHeight());
        primaryStage.show();
        primaryStage.setTitle("Projector");
        primaryStage.setX(0);
        primaryStage.setY(0);
        myController.setPrimaryStage(primaryStage);
        setProjectionScreen();
        if (canvasStage != null) {
            canvasStage.show();
        }
        createPreview();
        myController.initialTabSelect();
        primaryStage.requestFocus();
        ApplicationUtil.getInstance().checkForProjectorState();
    }

    @SuppressWarnings("unused")
    private double getScreenTrueMinXByTransparentStage() {
        try {
            Stage stage = new Stage();
            Pane root = new Pane();
            Background background = new Background(new BackgroundFill(Color.TRANSPARENT, CornerRadii.EMPTY, Insets.EMPTY));
            root.setBackground(background);
            stage.setOpacity(0.001);
            Scene scene = new Scene(root, 100, 100);
            scene.setFill(Color.TRANSPARENT);
            stage.setScene(scene);
            stage.show();
            double stageX;
            try {
                stage.setMaximized(true);
                stageX = stage.getX();
                stage.setMaximized(false);
            } finally {
                stage.close();
            }
            return stageX;
        } catch (Exception e) {
            return 0;
        }
    }

    private void createPreview() {
        if (settings.isPreviewLoadOnStart()) {
            projectionScreenController.createPreview();
        }
    }

    public void loadInBackGround() {
        Updater.getInstance().saveApplicationStartedWithVersion();
        primaryScene = null;
        settings = Settings.getInstance();
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("/view/MainView.fxml"));
            loader.setResources(Settings.getInstance().getResourceBundle());
            Pane root = loader.load();
            MainDesktop.globalRoot = root;
            myController = loader.getController();
            BibleController bibleController = myController.getBibleController();
            SongController songController = myController.getSongController();
            primaryScene = new Scene(root, settings.getMainWidth(), settings.getMainHeight());
            windowController = createWindowController(getClass(), primaryScene, primaryStage);
            addSettingsMenu(windowController);
            windowController.showSignInButton();
            primaryScene = primaryStage.getScene();
            primaryScene.addEventHandler(KeyEvent.KEY_PRESSED, event -> {
                if (event.isAltDown()) {
                    event.consume();
                }
                bibleController.onKeyEvent(event);
                songController.onKeyEvent(event);
            });
            addStylesheetToSceneBySettings(primaryScene, getClass());
            primaryScene.setOnKeyPressed(event -> {
                KeyCode keyCode = event.getCode();
                // remote stepper codes:
                // F5 - start
                // . end
                if (event.isControlDown()) {
                    if (keyCode == KeyCode.DIGIT1) {
                        myController.selectTab(1);
                    } else if (keyCode == KeyCode.DIGIT2) {
                        myController.selectTab(2);
                    } else if (keyCode == KeyCode.DIGIT3) {
                        myController.selectTab(3);
                    } else if (keyCode == KeyCode.DIGIT4) {
                        myController.selectTab(4);
                    } else if (keyCode == KeyCode.DIGIT5) {
                        myController.selectTab(5);
                    }
                }
                if (keyCode == KeyCode.PAGE_DOWN) {
                    myController.goNext();
                }
                if (keyCode == KeyCode.PAGE_UP) {
                    myController.goPrev();
                }
                if (event.isControlDown()) {
                    myController.setSelecting(true);
                }
                if (keyCode == KeyCode.F1) {
                    myController.setBlank();
                }
                //                if (event.getCode() == KeyCode.F3) {
                //                    setCanvasToSecondScreen();
                //                }
                if (keyCode == KeyCode.F7) {
                    myController.createCustomCanvas();
                }
                if (keyCode == KeyCode.F8) {
                    myController.duplicateCanvas();
                }
                if (keyCode == KeyCode.F5) {
                    myController.previewCanvas();
                }
                if (event.isAltDown()) {
                    event.consume();
                }
                if (!event.isConsumed()) {
                    myController.handleKeyPress(event);
                }
            });
            primaryScene.setOnKeyReleased(event -> {
                if (event.getCode() == KeyCode.CONTROL) {
                    myController.setSelecting(false);
                }
            });
            Scene tmpScene = primaryScene;
            primaryScene.addEventFilter(MouseEvent.DRAG_DETECTED, mouseEvent -> tmpScene.startFullDrag());
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
        System.out.println("primary stage loaded---------------");
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(MainDesktop.class.getResource("/view/ProjectionScreen.fxml"));
            Pane root = loader.load();
            projectionScreenController = loader.getController();
            projectionScreenController.setOnMainProjectionEvent();
            projectionScreenController.setRoot(root);
            ProjectionScreensUtil.getInstance().addProjectionScreenController(projectionScreenController, "Main projection");
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
        myController.setProjectionScreenController(projectionScreenController);
        myController.setMain(this);
        myController.getSettingsController().addOnSaveListener(() -> {
            ObservableList<String> stylesheets = primaryScene.getStylesheets();
            stylesheets.clear();
            URL resource = getClass().getResource("/view/" + settings.getSceneStyleFile());
            if (resource == null) {
                return;
            }
            String url = resource.toExternalForm();
            setUserAgentStylesheet(null);
            stylesheets.add(url);
            windowController.setStylesheet(url);
        });
        projectionScreenController.setInitialDotText();
        ProjectionScreensUtil.getInstance().setBlank(false);
        Updater.getInstance().checkForUpdate();
        if (startDate != null) {
            Date date1 = new Date();
            LOG.info((date1.getTime() - startDate.getTime()) + " ms");
        }
    }

    private void addSettingsMenu(WindowController windowController) {
        Menu settingsMenu = getSettingsMenu();
        if (windowController != null) {
            windowController.getMenuBar().getMenus().add(settingsMenu);
        }
        myController.initializeSettingsController(settingsMenu);
    }

    private Menu getSettingsMenu() {
        Menu settingsMenu = new Menu();
        settingsMenu.setMnemonicParsing(false);
        settingsMenu.setText(settings.getResourceBundle().getString("Settings"));
        return settingsMenu;
    }

    private void setProjectionScreen() {
        myController.setShowProjectionScreenToggleButtonToggle(settings.isAutomaticProjectionScreens());
        screenObservableList = Screen.getScreens();
        screenObservableList.addListener((ListChangeListener<Screen>) c -> setProjectionScreenStageCheckAutomatic());
        setProjectionScreenStageCheckAutomatic();
        primaryStage.setOnCloseRequest(we -> closeApplication());
        ApplicationUtil.getInstance().setListener(this::closeApplication);
        primarySceneEventHandler();
        myController.createCustomCanvas();
    }

    public void saveStateOnClose() {
        AppState appState = AppState.getInstance();
        appState.setClosed(true);
        appState.save();
    }

    private void closeApplication() {
        saveStateOnClose();
        System.out.println("Stage is closing");
        Settings settings = Settings.getInstance();
        settings.setApplicationRunning(false);
        settings.setMainHeight(primaryStage.getHeight());
        settings.setMainWidth(primaryStage.getWidth());
        settings.save();
        settings.setApplicationRunning(false);
        if (tmpStage != null) {
            tmpStage.close();
        }
        myController.close();
        ProjectionScreensUtil.getInstance().onClose();
        CustomCanvasService.getInstance().save();
    }

    private void primarySceneEventHandler() {
        if (primaryScene != null) {
            primaryScene.addEventHandler(KeyEvent.KEY_PRESSED, event -> {
                if (tmpStage != null) {
                    if (event.getCode() == KeyCode.F2) {
                        tmpStage.show();
                        primaryStage.show();
                    }
                    if (event.getCode() == KeyCode.F3) {
                        tmpStage.setFullScreen(true);
                        tmpStage.show();
                    }
                } else {
                    Popup popup = projectionScreenController.getPopup();
                    if (popup != null) {
                        if (event.getCode() == KeyCode.F2) {
                            popup.show(primaryStage);
                        }
                    }
                }
            });
        }
    }

    public void setProjectionScreenStageCheckAutomatic() {
        if (settings.isAutomaticProjectionScreens()) {
            setProjectionScreenStage(false);
        }
    }

    private ListIterator<Screen> getScreenListIteratorAndSetMainScreen() {
        ListIterator<Screen> it = screenObservableList.listIterator(0);
        while (it.hasPrevious()) {
            it.previous();
        }
        if (!it.hasNext()) {
            return null;
        }
        mainScreen = it.next(); // primary screen
        return it;
    }

    public void setProjectionScreenStage(boolean fromToggleButton) {
        ListIterator<Screen> it = getScreenListIteratorAndSetMainScreen();
        if (it == null) {
            return;
        }
        MonitorUtil.getInstance().clearMonitorInfos();
        showProjectionScreenOnNextScreen(it, fromToggleButton);
        int index = 0;
        while (it.hasNext()) {
            Screen nextScreen = it.next();
            try {
                ProjectionScreenController projectionScreenController = getProjectionScreenControllerOrDuplicate(index, nextScreen);
                ProjectionScreenHolder projectionScreenHolder = projectionScreenController.getProjectionScreenSettings().getProjectionScreenHolder();
                projectionScreenHolder.setScreenIndex(index + 2);
                projectionScreenController.setPrimaryStageVariable(primaryStage);
                createPopupForNextScreen(nextScreen, projectionScreenController);
            } catch (Exception e) {
                LOG.error(e.getMessage(), e);
            }
            ++index;
        }
        ProjectionScreensUtil.getInstance().closeFromIndex(index);
    }

    private ProjectionScreenController getProjectionScreenControllerOrDuplicate(Integer index, Screen screen) {
        ProjectionScreenHolder projectionScreenHolder = ProjectionScreensUtil.getInstance().getScreenHolderByIndex(index);
        ProjectionScreenController projectionScreenController;
        if (projectionScreenHolder != null) {
            projectionScreenController = projectionScreenHolder.getProjectionScreenController();
        } else {
            projectionScreenController = this.projectionScreenController.duplicate2(screen);
        }
        return projectionScreenController;
    }

    private void showProjectionScreenOnNextScreen(ListIterator<Screen> it, boolean fromToggleButton) {
        try {
            if (it.hasNext() && (canvasStage == null || !canvasStage.isShowing())) {
                projectionScreenController.setPrimaryStageVariable(primaryStage);
                createPopupForNextScreen(it.next(), projectionScreenController);
            } else {
                createCanvasStage(fromToggleButton);
            }
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
    }

    public void createPopupForNextScreen(Screen nextScreen, ProjectionScreenController projectionScreenController) {
        if (projectionScreenController == null || nextScreen == null) {
            return;
        }
        projectionScreenController.setMainDesktop(this);
        projectionScreenController.setScreen(nextScreen);
        Rectangle2D bounds = nextScreen.getBounds();
        double positionX = bounds.getMinX() + 0;
        double positionY = bounds.getMinY();
        double canvasWidth = bounds.getWidth();
        double canvasHeight = bounds.getHeight();
        projectionScreenController.setWidth(canvasWidth);
        projectionScreenController.setHeight(canvasHeight);
        projectionScreenController.setClip(canvasWidth, canvasHeight);
        boolean loadEmpty = !projectionScreenController.isSetTextCalled();
        Popup popup = projectionScreenController.getPopup();
        if (popup != null) {
            popup.getContent().clear();
            popup.hide();
            loadEmpty = false;
        }
        popup = new Popup();
        projectionScreenController.setPopup(popup);
        ObservableList<Node> content = popup.getContent();
        if (content == null) {
            return;
        }
        Pane root = projectionScreenController.getRoot();
        if (root == null) {
            return;
        }
        content.add(root);
        popup.setAutoFix(false);
        Stage primaryStage = projectionScreenController.getPrimaryStageVariable();
        popup.show(primaryStage, positionX, positionY);
        popup.setWidth(canvasWidth);
        popup.setHeight(canvasHeight);
        popup.setX(positionX);
        popup.setY(positionY);
        popup.setHideOnEscape(false);
        Scene scene = popup.getScene();
        scene.setCursor(Cursor.NONE);
        projectionScreenController.setScene(scene);
        if (loadEmpty) {
            projectionScreenController.loadEmpty();
        }
        projectionScreenController.setBackGroundColor();
        setSceneStyleSheet(scene);
        ProjectionScreenSettings projectionScreenSettings = projectionScreenController.getProjectionScreenSettings();
        if (projectionScreenSettings != null) {
            ProjectionScreenHolder projectionScreenHolder = projectionScreenSettings.getProjectionScreenHolder();
            if (projectionScreenHolder != null) {
                projectionScreenHolder.popupCreated();
            }
        }
    }

    private void createCanvasStage(boolean fromToggleButton) {
        Popup popup = projectionScreenController.getPopup();
        if (popup != null) {
            popup.getContent().clear();
            popup.hide();
            projectionScreenController.setPopup(null);
        }
        if (canvasStage != null) {
            canvasStage.show();
            return;
        }
        Scene scene = new Scene(projectionScreenController.getRoot(), 800, 600);
        WindowController canvasWindowController = getWindowController(getClass(), scene);
        scene = canvasWindowController.getScene();
        canvasStage = canvasWindowController.getStage();
        canvasStage.setTitle(Settings.getInstance().getResourceBundle().getString("Canvas"));
        getScreenListIteratorAndSetMainScreen();
        projectionScreenController.setScreen(mainScreen);
        projectionScreenController.getProjectionScreenSettings().getProjectionScreenHolder().setScreenIndex(0);
        tmpStage = canvasStage;
        canvasStage.setX(800);
        canvasStage.setY(0);
        canvasWindowController.maximizedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                tmpStage.setMaximized(true);
                tmpStage.setFullScreen(true);
            }
        });
        canvasStage.fullScreenProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue) {
                tmpStage.setMaximized(false);
            }
        });
        canvasStage.setOnCloseRequest(event -> tmpStage.hide());
        projectionScreenController.setStage(canvasStage);
        scene.widthProperty().addListener((observable, oldValue, newValue) -> projectionScreenController.repaint());
        scene.heightProperty().addListener((observable, oldValue, newValue) -> projectionScreenController.repaint());
        scene.setOnKeyPressed(event -> {
            KeyCode keyCode = event.getCode();
            if (keyCode == KeyCode.DOWN || keyCode == KeyCode.RIGHT || keyCode == KeyCode.PAGE_DOWN) {
                myController.goNext();
            } else if (keyCode == KeyCode.UP || keyCode == KeyCode.LEFT || keyCode == KeyCode.PAGE_UP) {
                myController.goPrev();
            } else if (keyCode == KeyCode.F3) {
                if (tmpStage.isFullScreen()) {
                    tmpStage.setFullScreen(false);
                    primaryStage.requestFocus();
                } else {
                    tmpStage.setFullScreen(true);
                    tmpStage.show();
                }
            }
            myController.onKeyPressed(event);
        });
        setSceneStyleSheet(scene);
        if (fromToggleButton) {
            canvasStage.show();
        }
    }

    private void setSceneStyleSheet(Scene scene) {
        URL resource = getClass().getResource("/view/" + settings.getSceneStyleFile());
        if (resource == null) {
            return;
        }
        addStylesheetToSceneBySettings(scene, getClass());
    }

    public void hideProjectionScreen() {
        projectionScreensUtil.hidePopups();
        if (canvasStage != null) {
            canvasStage.hide();
        }
    }
}
