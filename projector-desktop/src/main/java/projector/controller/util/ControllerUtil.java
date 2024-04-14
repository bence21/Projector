package projector.controller.util;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import projector.MainDesktop;
import projector.application.Settings;

import java.net.URL;

import static projector.utils.SceneUtils.getCustomStage2;

public class ControllerUtil {


    public static void setStyleFile(Scene scene, Class<?> aClass) {
        URL resource = aClass.getResource("/view/" + Settings.getInstance().getSceneStyleFile());
        if (resource != null) {
            scene.getStylesheets().add(resource.toExternalForm());
        }
    }

    public static FXMLLoader getFxmlLoader(String fxmlFile) {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(MainDesktop.class.getResource("/view/" + fxmlFile + ".fxml"));
        loader.setResources(Settings.getInstance().getResourceBundle());
        return loader;
    }

    public static Stage getStageWithRoot(Class<?> aClass, Pane root) {
        Scene scene = new Scene(root);
        setStyleFile(scene, aClass);
        return getCustomStage2(aClass, scene, root.getPrefWidth(), root.getPrefHeight());
    }
}
