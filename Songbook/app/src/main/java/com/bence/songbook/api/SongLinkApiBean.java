package com.bence.songbook.api;

import android.util.Log;

import com.bence.projector.common.dto.SongLinkDTO;
import com.bence.songbook.api.retrofit.ApiManager;
import com.bence.songbook.api.retrofit.SongLinkApi;

import retrofit2.Call;

public class SongLinkApiBean {
    private static final String TAG = SongLinkApiBean.class.getName();
    private SongLinkApi songLinkApi;

    public SongLinkApiBean() {
        songLinkApi = ApiManager.getClient().create(SongLinkApi.class);
    }

    public SongLinkDTO uploadSongLink(SongLinkDTO songLinkDTO) {
        Call<SongLinkDTO> call = songLinkApi.newSongLink(songLinkDTO);
        try {
            return call.execute().body();
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
        }
        return null;
    }
}
