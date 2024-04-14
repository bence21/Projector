package projector.api;

import com.bence.projector.common.dto.LoginDTO;
import okhttp3.Cookie;
import okhttp3.Headers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import projector.api.retrofit.ApiManager;
import projector.api.retrofit.LoginApi;
import retrofit2.Call;
import retrofit2.Response;

import java.util.ArrayList;
import java.util.List;

public class LoginApiBean {
    private static final Logger LOG = LoggerFactory.getLogger(LoginApiBean.class);
    private final LoginApi loginApi;

    public LoginApiBean() {
        loginApi = ApiManager.getClient().create(LoginApi.class);
    }

    private static void setCookie_(Headers headers, Response<Void> execute) {
        List<String> cookieStrings = headers.values("Set-Cookie");
        List<Cookie> cookies = null;
        //noinspection resource
        okhttp3.Response raw = execute.raw();
        for (String cookieString : cookieStrings) {
            try {
                Cookie cookie = Cookie.parse(raw.request().url(), cookieString);
                if (cookie == null) {
                    continue;
                }
                if (cookies == null) {
                    cookies = new ArrayList<>();
                }
                cookies.add(cookie);
            } catch (Exception e) {
                LOG.error(e.getMessage(), e);
            }
        }
        ApiManager.getInstance().setCookies(cookies);
    }

    public boolean login(LoginDTO loginDTO) {
        Call<Void> loginCall = loginApi.login(loginDTO.getUsername(), loginDTO.getPassword());
        try {
            Response<Void> execute = loginCall.execute();
            setCookie(execute.headers(), execute);
            return true;
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
        return false;
    }

    public boolean logout() {
        Call<Void> logoutCall = loginApi.logout();
        try {
            logoutCall.execute();
            return true;
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
        return false;
    }

    private void setCookie(Headers headers, Response<Void> execute) {
        try {
            setCookie_(headers, execute);
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
    }
}
