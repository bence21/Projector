package projector.controller;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import projector.application.Settings;
import projector.controller.util.OnResultListener;

import java.util.ResourceBundle;

import static projector.controller.BibleController.setSceneStyleFile;
import static projector.utils.SceneUtils.addIconToStage;
import static projector.utils.SceneUtils.getAStage;

public class MessageDialogController {
    private static final Logger LOG = LoggerFactory.getLogger(MessageDialogController.class);
    public HBox hBox;
    public Label headerLabel;
    private Stage stage;

    public static MessageDialogController getMessageDialog(Class<?> aClass, String title) {
        try {
            Stage stage = getAStage(aClass);
            stage.initStyle(StageStyle.DECORATED);
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(aClass.getResource("/view/MessageDialogView.fxml"));
            BorderPane borderPane = loader.load();
            MessageDialogController controller = loader.getController();
            controller.setStage(stage);
            Scene scene = new Scene(borderPane, borderPane.getPrefWidth(), borderPane.getPrefHeight());
            setSceneStyleFile(scene);
            stage.setScene(scene);
            addIconToStage(stage, aClass);
            stage.setTitle(title);
            return controller;
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
        return null;
    }

    public static void confirmDeletion(OnResultListener onResultListener, Logger log, Class<?> aClass) {
        try {
            ResourceBundle resourceBundle = Settings.getInstance().getResourceBundle();
            MessageDialogController messageDialog = MessageDialogController.getMessageDialog(aClass, resourceBundle.getString("Confirm deletion") + "!");
            if (messageDialog == null) {
                return;
            }
            Button confirmButton = new Button(resourceBundle.getString("Delete"));
            confirmButton.setId("confirmButton");
            messageDialog.addButton(confirmButton);
            messageDialog.addCancelButton();
            confirmButton.setOnAction(event -> {
                messageDialog.close();
                onResultListener.onResult();
            });
            messageDialog.showAndWait();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public void setHeaderText(String s) {
        headerLabel.setText(s);
    }

    public void addButton(Button button) {
        hBox.getChildren().add(button);
    }

    public void close() {
        if (stage != null) {
            stage.close();
        }
    }

    public void addCancelButton() {
        addACloseButton("Cancel");
    }

    public void addOkButton() {
        addACloseButton("Ok");
    }

    private void addACloseButton(String s) {
        Button button = new Button(s);
        addButton(button);
        button.setOnAction(event -> close());
    }

    public void addHeaderText(String s) {
        headerLabel.setText(headerLabel.getText() + s);
    }

    public void show() {
        stage.show();
    }

    public void showAndWait() {
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.showAndWait();
    }
}
