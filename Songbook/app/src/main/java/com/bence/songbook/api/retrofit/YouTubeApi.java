package com.bence.songbook.api.retrofit;

import com.bence.projector.common.dto.YouTubeApiDTO;

import retrofit2.Call;
import retrofit2.http.GET;

public interface YouTubeApi {

    @GET("/api/youtube/youtube_api_key")
    Call<YouTubeApiDTO> getYouTubeApiKey();
}
