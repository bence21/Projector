package com.bence.songbook.service;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.bence.projector.common.dto.LoginDTO;
import com.bence.songbook.api.HttpStatus;
import com.bence.songbook.api.LoginApiBean;
import com.bence.songbook.models.LoggedInUser;
import com.bence.songbook.repository.impl.ormLite.LoggedInUserRepositoryImpl;

import java.util.List;

import okhttp3.Headers;
import okhttp3.Response;

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

    public LoggedInUser getLoggedInUser(Context context) {
        LoggedInUserRepositoryImpl loggedInUserRepository = new LoggedInUserRepositoryImpl(context);
        List<LoggedInUser> loggedInUsers = loggedInUserRepository.findAll();
        if (loggedInUsers == null || loggedInUsers.size() <= 0) {
            return null;
        }
        return loggedInUsers.get(0);
    }

    public boolean isLoggedIn(Context context) {
        return getLoggedInUser(context) != null;
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

    public boolean loginToServer(Context context) {
        LoggedInUser loggedInUser = getLoggedInUser(context);
        if (loggedInUser == null) {
            return false;
        }
        LoginApiBean loginApiBean = new LoginApiBean();
        LoginDTO loginDTO = new LoginDTO();
        loginDTO.setUsername(loggedInUser.getEmail());
        loginDTO.setPassword(loggedInUser.getPassword());
        return loginApiBean.login(loginDTO);
    }

    public boolean loginIfNeeded(Headers headers, Context context, Response response) {
        if (loginNeeded(headers, response)) {
            return loginToServer(context);
        }
        return false;
    }

    public String getEmailFromUserOrGmail(Context context) {
        LoggedInUser loggedInUser = UserService.getInstance().getLoggedInUser(context);
        if (loggedInUser != null) {
            return loggedInUser.getEmail();
        }
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPreferences.getString("gmail", "");
    }
}
