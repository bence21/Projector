package projector.controller.util;


import com.bence.projector.common.dto.LoginDTO;
import okhttp3.Headers;
import okhttp3.Response;
import projector.api.LoginApiBean;
import projector.api.util.HttpStatus;
import projector.model.LoggedInUser;

import java.util.List;

public class UserService {
    private static UserService instance;

    private UserService() {

    }

    public static UserService getInstance() {
        if (instance == null) {
            instance = new UserService();
        }
        return instance;
    }

    public LoggedInUser getLoggedInUser() {
        return LoginService.getInstance().getLoggedInUser();
    }

    public boolean isLoggedIn() {
        return getLoggedInUser() != null;
    }

    public boolean loginNeeded(Headers headers, Response response) {
        if (response != null && response.code() == HttpStatus.NOT_AUTHORIZED) {
            return true;
        }
        List<String> locations = headers.values("Location");
        for (String s : locations) {
            if (s.endsWith("/#/login")) {
                return true;
            }
        }
        return false;
    }

    public boolean loginToServer() {
        LoggedInUser loggedInUser = getLoggedInUser();
        if (loggedInUser == null) {
            return false;
        }
        LoginApiBean loginApiBean = new LoginApiBean();
        LoginDTO loginDTO = new LoginDTO();
        loginDTO.setUsername(loggedInUser.getEmail());
        loginDTO.setPassword(loggedInUser.getPassword());
        if (loginDTO.getPassword() == null) {
            return false;
        }
        return loginApiBean.login(loginDTO);
    }

    public boolean loginIfNeeded(Headers headers, Response response) {
        if (loginNeeded(headers, response)) {
            return loginToServer();
        }
        return false;
    }
}
