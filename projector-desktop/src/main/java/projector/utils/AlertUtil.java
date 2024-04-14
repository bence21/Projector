package projector.utils;

import javafx.scene.control.Alert;
import javafx.stage.Stage;

import static projector.utils.SceneUtils.addIconToStage;

public class AlertUtil {
    public static Alert getAppAlert(Alert.AlertType alertType, Class<?> aClass) {
        Alert alert = new Alert(alertType);
        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
        addIconToStage(stage, aClass);
        return alert;
    }
}
