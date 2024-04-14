package projector.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import projector.controller.util.LoginService;
import projector.controller.util.OnResultListener;
import projector.model.LoggedInUser;

public class AccountPopupController {

    @FXML
    private BorderPane accountPopupPane;
    @FXML
    private Label userLabel;
    private OnResultListener onResultListener = null;

    public void setUser(LoggedInUser user) {
        userLabel.setText(user.getEmail());
    }

    @FXML
    private void handleLogout() {
        LoginService.getInstance().logout();
        if (onResultListener != null) {
            onResultListener.onResult();
        }
    }

    public void setOnLogout(OnResultListener onResultListener) {
        this.onResultListener = onResultListener;
    }
}
