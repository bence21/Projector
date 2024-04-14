package projector.controller.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import projector.api.LoginApiBean;
import projector.api.retrofit.ApiManager;
import projector.model.LoggedInUser;
import projector.service.LoggedInUserService;
import projector.service.ServiceManager;

import java.util.List;

public class LoginService {
    private static final Logger LOG = LoggerFactory.getLogger(LoginService.class);
    private static LoginService instance;
    private LoggedInUser loggedInUser;

    private LoginService() {
    }

    public static synchronized LoginService getInstance() {
        if (instance == null) {
            instance = new LoginService();
        }
        return instance;
    }

    public LoggedInUser getLoggedInUser() {
        return loggedInUser;
    }

    public void checkSignIn() {
        LoggedInUserService loggedInUserService = ServiceManager.getLoggedInUserService();
        List<LoggedInUser> loggedInUsers = loggedInUserService.findAll();
        for (LoggedInUser loggedInUser : loggedInUsers) {
            this.loggedInUser = loggedInUser;
            return;
        }
    }

    public void logout() {
        if (loggedInUser != null) {
            try {
                ServiceManager.getLoggedInUserService().delete(loggedInUser);
            } catch (Exception e) {
                LOG.error(e.getMessage(), e);
            }
            ApiManager.getInstance().setCookies(null);
            new Thread(() -> {
                LoginApiBean loginApiBean = new LoginApiBean();
                loginApiBean.logout();
            }).start();
        }
        loggedInUser = null;
    }
}
