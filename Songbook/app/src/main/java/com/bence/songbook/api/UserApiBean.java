package com.bence.songbook.api;

import android.util.Log;

import com.bence.projector.common.dto.UserDTO;
import com.bence.songbook.api.retrofit.ApiManager;
import com.bence.songbook.api.retrofit.UserApi;

import retrofit2.Call;

public class UserApiBean {
    private static final String TAG = UserApiBean.class.getName();
    private final UserApi userApi;

    public UserApiBean() {
        userApi = ApiManager.getClient().create(UserApi.class);
    }

    public UserDTO getLoggedInUser() {
        Call<UserDTO> call = userApi.getLoggedInUser();
        try {
            return call.execute().body();
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
        }
        return null;
    }
}
