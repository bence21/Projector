package com.bence.songbook.api;

import android.util.Log;

import com.bence.projector.common.dto.LanguageDTO;
import com.bence.songbook.api.assembler.LanguageAssembler;
import com.bence.songbook.api.retrofit.ApiManager;
import com.bence.songbook.api.retrofit.LanguageApi;
import com.bence.songbook.models.Language;

import java.io.IOException;
import java.util.List;

import retrofit2.Call;

public class LanguageApiBean {
    private static final String TAG = LanguageApiBean.class.getName();
    private LanguageApi languageApi;
    private LanguageAssembler languageAssembler;

    public LanguageApiBean() {
        languageApi = ApiManager.getClient().create(LanguageApi.class);
        languageAssembler = LanguageAssembler.getInstance();
    }

    public List<Language> getLanguages() {
        Call<List<LanguageDTO>> call = languageApi.getLanguages();
        try {
            List<LanguageDTO> languageDTOs = call.execute().body();
            return languageAssembler.createModelList(languageDTOs);
        } catch (IOException e) {
            Log.e(TAG, e.getMessage(), e);
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
        }
        return null;
    }
}
