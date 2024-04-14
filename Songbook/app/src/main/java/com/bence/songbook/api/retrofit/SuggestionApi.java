package com.bence.songbook.api.retrofit;

import com.bence.projector.common.dto.SuggestionDTO;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface SuggestionApi {

    @POST("/api/suggestion")
    Call<SuggestionDTO> newSuggestion(@Body SuggestionDTO suggestionDTO);
}
