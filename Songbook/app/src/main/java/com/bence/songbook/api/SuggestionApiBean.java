package com.bence.songbook.api;

import android.util.Log;

import com.bence.projector.common.dto.SuggestionDTO;
import com.bence.songbook.api.retrofit.ApiManager;
import com.bence.songbook.api.retrofit.SuggestionApi;

import retrofit2.Call;

public class SuggestionApiBean {
    private static final String TAG = SuggestionApiBean.class.getName();
    private SuggestionApi suggestionApi;

    public SuggestionApiBean() {
        suggestionApi = ApiManager.getClient().create(SuggestionApi.class);
    }

    public SuggestionDTO uploadSuggestion(SuggestionDTO suggestionDTO) {
        Call<SuggestionDTO> call = suggestionApi.newSuggestion(suggestionDTO);
        try {
            return call.execute().body();
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
        }
        return null;
    }
}
