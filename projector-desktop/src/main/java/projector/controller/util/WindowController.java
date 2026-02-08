package projector.controller.util;

import com.bence.projector.common.dto.UserDTO;
import com.goxr3plus.fxborderlessscene.borderless.BorderlessScene;
import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.MenuBar;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.stage.Popup;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import projector.MainDesktop;
import projector.api.LoginApiBean;
import projector.api.UserApiBean;
import projector.application.ApplicationVersion;
import projector.application.Settings;
import projector.controller.AccountPopupController;
import projector.controller.LoginController;
import projector.controller.MyController;
import projector.model.FavouriteSong;
import projector.model.Language;
import projector.model.LoggedInUser;
import projector.model.Song;
import projector.service.FavouriteSongService;
import projector.service.LanguageService;
import projector.service.LoggedInUserService;
import projector.service.ServiceManager;
import projector.service.SongService;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static projector.utils.ColorUtil.getMainBorderColor;
import static projector.utils.SceneUtils.getCustomStage3;

public class WindowController {
    private static final Logger LOG = LoggerFactory.getLogger(WindowController.class);
    public HBox titleBar;
    @FXML
    private Button signInButton;

    @SuppressWarnings("unused")
    @FXML
    private StackPane mainStackPane;
    @FXML
    private MenuBar menuBar;
    @FXML
    private Label topLabel;
    @FXML
    private Button minimize;
    @FXML
    private Button maximizeNormalize;
    @FXML
    private Button exit;
    @FXML
    private BorderPane mainBorderPane;

    private BorderlessScene borderlessScene;
    private StackPane root;
    private Stage signInStage = null;
    private LoginController loginController;
    private Stage ownerStage;
    private Popup accountPopup;

    public static WindowController getInstance(Class<?> aClass, Stage stage, Scene scene) {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(aClass.getResource("/view/WindowController.fxml"));
        loader.setResources(Settings.getInstance().getResourceBundle());
        try {
            StackPane root = loader.load();
            setSceneStyleFileForPane(aClass, root);
            WindowController windowController = loader.getController();
            windowController.root = root;
            windowController.setup(stage, scene);
            return windowController;
        } catch (IOException e) {
            LOG.error(e.getMessage(), e);
        }
        return null;
    }

    private static void setSceneStyleFileForPane(Class<?> aClass, Pane pane) {
        URL resource = aClass.getResource("/view/" + Settings.getInstance().getSceneStyleFile());
        if (resource != null) {
            ObservableList<String> stylesheets = pane.getStylesheets();
            stylesheets.clear();
            stylesheets.add(resource.toExternalForm());
        }
    }

    private static FXMLLoader getViewLoader(@SuppressWarnings("SameParameterValue") String fileName) {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(MainDesktop.class.getResource("/view/" + fileName + ".fxml"));
        loader.setResources(Settings.getInstance().getResourceBundle());
        return loader;
    }

    private static LoggedInUser getLoggedInUserByEmail(String email) {
        LoggedInUserService loggedInUserService = ServiceManager.getLoggedInUserService();
        LoggedInUser loggedInUser;
        loggedInUser = loggedInUserService.findByEmail(email);
        if (loggedInUser == null) {
            loggedInUser = new LoggedInUser();
            loggedInUser.setEmail(email);
        }
        return loggedInUser;
    }

    public StackPane getRoot() {
        return root;
    }

    /**
     * Checking the functionality of the Borderless Scene Library
     */
    private void setup(Stage stage, Scene scene) {
        this.ownerStage = stage;
        setScene(scene);
        borderlessScene = new BorderlessScene(stage, getRoot());
        // To move the window around by pressing a node:
        borderlessScene.setMoveControl(topLabel);
        topLabel.setText(stage.getTitle());
        stage.titleProperty().addListener((observable, oldValue, newValue) -> topLabel.setText(newValue));
        int version = ApplicationVersion.getInstance().getVersion();
        String versionLabel = Settings.getInstance().getResourceBundle().getString("Projector version");
        topLabel.setTooltip(new Tooltip(versionLabel + ": " + version));
        String titleFocused = "windowTitleFocused";
        stage.focusedProperty().addListener((observable, oldValue, newValue) -> {
            ObservableList<String> styleClass = topLabel.getStyleClass();
            if (newValue) {
                styleClass.add(titleFocused);
            } else {
                styleClass.remove(titleFocused);
            }
        });

        exit.setOnAction(a -> {
            EventHandler<WindowEvent> onCloseRequest = stage.getOnCloseRequest();
            if (onCloseRequest != null) {
                onCloseRequest.handle(null);
            }
            stage.close();
        });
        minimize.setOnAction(a -> stage.setIconified(true));
        maximizeNormalize.setOnAction(a -> borderlessScene.maximizeStage());
        SimpleBooleanProperty maximizedProperty = maximizedProperty();
        maximizedProperty.addListener((observable, oldValue, newValue) -> {
            ImageView imageView = new ImageView();
            imageView.setFitHeight(29);
            imageView.setFitWidth(45);
            imageView.setPickOnBounds(true);
            imageView.setPreserveRatio(true);
            String s;
            if (newValue) {
                s = "maximized";
            } else {
                s = "maximize";
            }
            InputStream resourceAsStream = getClass().getResourceAsStream("/icons/" + s + ".png");
            if (resourceAsStream != null) {
                Image image = new Image(resourceAsStream);
                imageView.setImage(image);
            }
            maximizeNormalize.setGraphic(imageView);
        });
        stage.fullScreenProperty().addListener((observable, oldValue, newValue) -> {
            titleBar.setManaged(!newValue);
            titleBar.setVisible(!newValue);
            if (newValue) {
                setBorderWidth2(0);
            } else {
                setBorderWidth();
                borderlessScene.ensureMaximizeStage(false);
            }
        });
    }

    public SimpleBooleanProperty maximizedProperty() {
        return borderlessScene.getController().maximizedProperty();
    }

    public Scene getScene() {
        return getRoot().getScene();
    }

    private void setScene(Scene scene) {
        mainBorderPane.setCenter(scene.getRoot());
        setBorderWidth();
    }

    public Stage getStage() {
        return ownerStage;
    }

    private void setBorderWidth() {
        setBorderWidth2(1);
    }

    private void setBorderWidth2(int width) {
        try {
            Screen screen = Screen.getPrimary();
            double scaleX = screen.getOutputScaleX();
            Border border = new Border(new BorderStroke(getMainBorderColor(), BorderStrokeStyle.SOLID, CornerRadii.EMPTY, new BorderWidths(width / scaleX)));
            mainBorderPane.setBorder(border);
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
    }

    public MenuBar getMenuBar() {
        return menuBar;
    }

    public void setStylesheet(String stylesheet) {
        StackPane pane = getRoot();
        if (pane == null) {
            return;
        }
        ObservableList<String> stylesheets = pane.getStylesheets();
        stylesheets.clear();
        stylesheets.add(stylesheet);
    }

    public void showSignInButton() {
        checkSignIn();
        signInButton.setManaged(true);
        signInButton.setVisible(true);
        signInButton.setOnAction(event -> {
            LoggedInUser user = LoginService.getInstance().getLoggedInUser();
            if (user != null) {
                createAccountPopup(user);
                return;
            }
            if (signInStage != null) {
                if (!signInStage.isShowing() && loginController != null) {
                    loginController.clear();
                }
                alignSignInStage(signInStage);
                signInStage.show();
                signInStage.requestFocus();
                loginController.focusOnLoginButton();
                return;
            }
            try {
                FXMLLoader loader = getViewLoader("Login");
                Pane root = loader.load();
                loginController = loader.getController();
                Stage stage = getCustomStage3(getClass(), root);
                stage.setTitle(Settings.getInstance().getResourceBundle().getString("Sign In"));
                alignSignInStage(stage);
                stage.show();
                loginController.focusOnLoginButton();
                loginController.addListener(loginDTO -> {
                    Thread thread = new Thread(() -> {
                        try {
                            LoginApiBean loginApiBean = new LoginApiBean();
                            if (loginApiBean.login(loginDTO)) {
                                UserApiBean userApiBean = new UserApiBean();
                                UserDTO loggedInUserDTO = userApiBean.getLoggedInUser();
                                if (loggedInUserDTO != null) {
                                    LoggedInUserService loggedInUserService = ServiceManager.getLoggedInUserService();
                                    LoggedInUser loggedInUser = getLoggedInUserByEmail(loggedInUserDTO.getEmail());
                                    loggedInUser.setPassword(loginDTO.getPassword());
                                    loggedInUser.setSurname(loggedInUserDTO.getSurname());
                                    loggedInUser.setFirstName(loggedInUserDTO.getFirstName());
                                    loggedInUserService.create(loggedInUser);
                                    checkSignIn();
                                    Platform.runLater(() -> signInStage.close());
                                }
                            }
                        } catch (Exception e) {
                            LOG.error(e.getMessage(), e);
                        }
                    });
                    thread.start();
                });
                signInStage = stage;
            } catch (Exception e) {
                LOG.error(e.getMessage(), e);
            }
        });
    }

    private void alignSignInStage(Stage signInStage) {
        Bounds bounds = signInButton.localToScreen(signInButton.getBoundsInLocal());
        signInStage.setX(bounds.getMinX());
        signInStage.setY(bounds.getMinY() + signInButton.getHeight());
    }

    private void createAccountPopup(LoggedInUser loggedInUser) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/view/AccountPopup.fxml"));
            Pane root = fxmlLoader.load();
            AccountPopupController accountPopupController = fxmlLoader.getController();
            accountPopupController.setUser(loggedInUser);
            accountPopupController.setOnLogout(() -> {
                accountPopup.hide();
                checkSignIn();
                clearUserData();
            });
            accountPopup = new Popup();
            ObservableList<Node> content = accountPopup.getContent();
            if (content == null) {
                return;
            }
            content.add(root);
            setSceneStyleFileForPane(getClass(), root);
            accountPopup.setAutoHide(true);
            accountPopup.show(this.ownerStage, ownerStage.getX() + signInButton.getLayoutX(), ownerStage.getY() + signInButton.getLayoutY() + signInButton.getHeight());
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
    }

    private void checkSignIn() {
        LoginService loginService = LoginService.getInstance();
        loginService.checkSignIn();
        LoggedInUser loggedInUser = loginService.getLoggedInUser();
        String text;
        boolean signedIn = loggedInUser != null;
        if (signedIn) {
            text = loggedInUser.getSurname() + " " + loggedInUser.getFirstName();
            if (text.trim().isEmpty()) {
                text = "Account";
            }
            syncUserData();
        } else {
            text = Settings.getInstance().getResourceBundle().getString("Sign In");
        }
        String finalText = text;
        Platform.runLater(() -> signInButton.setText(finalText));
        MyController.getInstance().getSongController().onSignInUpdated(signedIn);
    }

    private void clearUserData() {
        new Thread(() -> {
            clearFavouriteSongs();
            clearLanguagesFavouriteSongLastServerModifiedDate();
            MyController.getInstance().getSongController().reloadInitialSongs();
        }).start();
    }

    private void clearFavouriteSongs() {
        FavouriteSongService favouriteSongService = ServiceManager.getFavouriteSongService();
        List<FavouriteSong> favouriteSongs = favouriteSongService.findAll();
        List<Song> modifiedSongs = new ArrayList<>();
        SongService songService = ServiceManager.getSongService();
        for (FavouriteSong favouriteSong : favouriteSongs) {
            Song song = songService.getFromMemoryOrSongNoUpdate(favouriteSong.getSong());
            if (song != null) {
                song.setFavourite(null);
                modifiedSongs.add(song);
            }
        }
        songService.create(modifiedSongs);
        favouriteSongService.delete(favouriteSongs);
    }

    private void clearLanguagesFavouriteSongLastServerModifiedDate() {
        LanguageService languageService = ServiceManager.getLanguageService();
        List<Language> languages = languageService.findAll();
        for (Language language : languages) {
            language.setFavouriteSongLastServerModifiedDate(null);
        }
        languageService.create(languages);
    }

    private void syncUserData() {
        ServiceManager.getFavouriteSongService().syncFavouritesFromServer(() -> MyController.getInstance().getSongController().onFavouritesUpdated());
    }
}
