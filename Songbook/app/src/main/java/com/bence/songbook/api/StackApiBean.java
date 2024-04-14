package com.bence.songbook.api;

import android.util.Log;

import com.bence.projector.common.dto.StackDTO;
import com.bence.songbook.api.retrofit.ApiManager;
import com.bence.songbook.api.retrofit.StackApi;

import retrofit2.Call;

public class StackApiBean {
    private static final String TAG = StackApiBean.class.getName();
    private StackApi stackApi;

    public StackApiBean() {
        stackApi = ApiManager.getClient().create(StackApi.class);
    }

    public StackDTO uploadStack(StackDTO stackDTO) {
        Call<StackDTO> call = stackApi.create(stackDTO);
        try {
            return call.execute().body();
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
        }
        return null;
    }
}
