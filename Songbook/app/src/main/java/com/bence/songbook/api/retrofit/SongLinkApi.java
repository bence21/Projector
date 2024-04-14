package com.bence.songbook.api.retrofit;

import com.bence.projector.common.dto.SongLinkDTO;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface SongLinkApi {

    @POST("/api/songLink")
    Call<SongLinkDTO> newSongLink(@Body SongLinkDTO songLinkDTO);
}
