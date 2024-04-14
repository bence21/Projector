package com.bence.songbook.api;

import android.util.Log;

import com.bence.projector.common.dto.LoginDTO;
import com.bence.songbook.api.retrofit.ApiManager;
import com.bence.songbook.api.retrofit.LoginApi;

import java.util.ArrayList;
import java.util.List;

import okhttp3.Cookie;
import okhttp3.Headers;
import retrofit2.Call;
import retrofit2.Response;

public class LoginApiBean {
    private static final String TAG = LoginApiBean.class.getName();
    private final LoginApi loginApi;

    public LoginApiBean() {
        loginApi = ApiManager.getClient().create(LoginApi.class);
    }

    public boolean login(LoginDTO loginDTO) {
        Call<Void> loginCall = loginApi.login(loginDTO.getUsername(), loginDTO.getPassword());
        try {
            Response<Void> execute = loginCall.execute();
            setCookie(execute.headers(), execute);
            return true;
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
        }
        return false;
    }

    @SuppressWarnings("UnusedReturnValue")
    public boolean logout() {
        Call<Void> logoutCall = loginApi.logout();
        try {
            logoutCall.execute();
            return true;
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
        }
        return false;
    }

    private void setCookie(Headers headers, Response<Void> execute) {
        List<String> cookieStrings = headers.values("Set-Cookie");
        List<Cookie> cookies = null;
        for (String cookieString : cookieStrings) {
            //noinspection resource
            Cookie cookie = Cookie.parse(execute.raw().request().url(), cookieString);
            if (cookie == null) {
                continue;
            }
            if (cookies == null) {
                cookies = new ArrayList<>();
            }
            cookies.add(cookie);
        }
        ApiManager.getInstance().setCookies(cookies);
    }
}
