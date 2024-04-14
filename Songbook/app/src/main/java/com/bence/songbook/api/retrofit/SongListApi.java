package com.bence.songbook.api.retrofit;

import com.bence.projector.common.dto.SongListDTO;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface SongListApi {

    @POST("/api/songList")
    Call<SongListDTO> uploadSongList(@Body SongListDTO songListDTO);

    @GET("/api/songList/{uuid}")
    Call<SongListDTO> getSongList(@Path("uuid") String uuid);
}
