package projector.controller;

import com.bence.projector.common.dto.LoginDTO;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import projector.api.retrofit.ApiManager;
import projector.controller.listener.LoginListener;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import static projector.utils.AlertUtil.getAppAlert;

public class LoginController {

    private final List<LoginListener> loginListeners = new ArrayList<>(1);
    @FXML
    private TextField emailTextField;
    @FXML
    private PasswordField passwordTextField;
    @FXML
    private Button loginButton;

    public void initialize() {
        passwordTextField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                loginButtonOnAction();
                focusOnLoginButton();
            }
        });
        focusOnLoginButton();
    }

    public void focusOnLoginButton() {
        loginButton.requestFocus();
    }

    public void addListener(LoginListener loginListener) {
        loginListeners.add(loginListener);
    }

    public void loginButtonOnAction() {
        final LoginDTO user = new LoginDTO();
        user.setUsername(emailTextField.getText().trim());
        user.setPassword(passwordTextField.getText().trim());
        if (user.getUsername().isEmpty()) {
            Alert alert = getAppAlert(Alert.AlertType.WARNING, getClass());
            alert.setTitle("Missing email!");
            alert.setHeaderText("Please enter a email");
            alert.setContentText("The email field cannot be left blank");
            alert.show();
            return;
        } else if (user.getPassword().isEmpty()) {
            Alert alert = getAppAlert(Alert.AlertType.WARNING, getClass());
            alert.setTitle("Missing password!");
            alert.setHeaderText("Please enter a password");
            alert.setContentText("The password field cannot be left blank");
            alert.show();
            return;
        }
        for (LoginListener loginListener : loginListeners) {
            loginListener.onLogin(user);
        }
    }

    public void clear() {
        emailTextField.setText("");
        passwordTextField.setText("");
    }

    public void createAccountClick() {
        if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
            try {
                Desktop.getDesktop().browse(new URI(ApiManager.BASE_URL_S + "/#/registration"));
            } catch (IOException | URISyntaxException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
