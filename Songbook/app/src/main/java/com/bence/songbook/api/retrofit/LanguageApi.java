package com.bence.songbook.api.retrofit;

import com.bence.projector.common.dto.LanguageDTO;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;

public interface LanguageApi {
    @GET("/api/languages")
    Call<List<LanguageDTO>> getLanguages();
}
