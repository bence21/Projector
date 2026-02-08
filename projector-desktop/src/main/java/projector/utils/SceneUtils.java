package projector.utils;

import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import projector.application.Settings;
import projector.controller.util.WindowController;

import java.io.InputStream;
import java.net.URL;

public class SceneUtils {
    private static final Logger LOG = LoggerFactory.getLogger(SceneUtils.class);

    public static void addIconToStage(Stage stage, Class<?> aClass) {
        addOneIcon(stage, aClass, "/icons/icon32.png");
        addOneIcon(stage, aClass, "/icons/icon24.png");
        addOneIcon(stage, aClass, "/icons/icon16.png");
    }

    private static void addOneIcon(Stage stage, Class<?> aClass, String name) {
        InputStream resourceAsStream = aClass.getResourceAsStream(name);
        if (resourceAsStream != null) {
            Image image = new Image(resourceAsStream);
            stage.getIcons().add(image); // initial add
            Platform.runLater(() -> {
                stage.getIcons().add(image); // we add it again to make sure it is added
            });
        } else {
            System.out.println("Not found name: " + name);
        }
    }

    public static void addStylesheetToScene(Scene scene, Class<?> aClass, String s) {
        URL resource = aClass.getResource(s);
        if (resource != null) {
            scene.getStylesheets().add(resource.toExternalForm());
        }
    }

    public static void addStylesheetToSceneBySettings(Scene scene, Class<?> aClass) {
        addStylesheetToScene(scene, aClass, "/view/" + Settings.getInstance().getSceneStyleFile());
    }

    public static Stage getAStage(Class<?> aClass) {
        Stage stage = new Stage();
        addIconToStage(stage, aClass);
        return stage;
    }

    public static Stage getTransparentStage(Class<?> aClass) {
        Stage stage = new Stage();
        addIconToStage(stage, aClass);
        stage.initStyle(StageStyle.TRANSPARENT);
        return stage;
    }

    public static Stage getCustomStage(Class<?> aClass, Scene scene) {
        return getWindowController(aClass, scene).getStage();
    }

    public static WindowController getWindowController(Class<?> aClass, Scene scene) {
        Stage stage = getAStage(aClass);
        return createWindowController(aClass, scene, stage);
    }

    public static Stage getCustomStage2(Class<?> aClass, Scene scene, double width, double height) {
        Stage stage = getCustomStage(aClass, scene);
        stage.setWidth(width);
        stage.setHeight(height);
        return stage;
    }

    public static Stage getCustomStage3(Class<?> aClass, Pane root) {
        return getCustomStage2(aClass, new Scene(root), root.getPrefWidth(), root.getPrefHeight());
    }

    public static WindowController createWindowController(Class<?> aClass, Scene scene, Stage stage) {
        try {
            if (stage.getStyle() != StageStyle.TRANSPARENT && !stage.isShowing()) {
                stage.initStyle(StageStyle.TRANSPARENT);
            }
        } catch (IllegalStateException e) {
            LOG.error(e.getMessage(), e);
        }
        WindowController windowController = WindowController.getInstance(aClass, stage, scene);
        if (windowController == null) {
            return null;
        }
        Scene windowControllerScene = windowController.getScene();
        windowControllerScene.setFill(Color.TRANSPARENT);
        stage.setScene(windowControllerScene);
        return windowController;
    }
}
