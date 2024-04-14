package com.bence.songbook.api;

import android.util.Log;

import com.bence.projector.common.dto.YouTubeApiDTO;
import com.bence.songbook.api.retrofit.ApiManager;
import com.bence.songbook.api.retrofit.YouTubeApi;

import retrofit2.Call;
import retrofit2.Response;

public class YouTubeApiBean {
    private static final String TAG = YouTubeApiBean.class.getName();
    private final YouTubeApi youTubeApi;

    public YouTubeApiBean() {
        youTubeApi = ApiManager.getClient().create(YouTubeApi.class);
    }

    public String getYouTubeApiKey() {
        Call<YouTubeApiDTO> call = youTubeApi.getYouTubeApiKey();
        try {
            Response<YouTubeApiDTO> execute = call.execute();
            if (execute.isSuccessful()) {
                YouTubeApiDTO youTubeApiDTO = execute.body();
                if (youTubeApiDTO != null) {
                    return youTubeApiDTO.getYouTubeApiKey();
                }
            }
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
        }
        return "";
    }
}
