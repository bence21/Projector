package com.bence.songbook.api.retrofit;

import com.bence.projector.common.dto.SongDTO;
import com.bence.projector.common.dto.SongTitleDTO;
import com.bence.projector.common.dto.SongViewsDTO;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface SongApi {
    @GET("/api/songs")
    Call<List<SongDTO>> getSongs();

    @GET("/api/songsAfterModifiedDate/{modifiedDate}")
    Call<List<SongDTO>> getSongsAfterModifiedDate(@Path("modifiedDate") Long modifiedDate);

    @GET("/api/songs/language/{language}")
    Call<List<SongDTO>> getSongsByLanguage(@Path("language") String language);

    @GET("/api/songs/language/{language}/modifiedDate/{modifiedDate}")
    Call<List<SongDTO>> getSongsByLanguageAndAfterModifiedDate(@Path("language") String languageUuid, @Path("modifiedDate") Long modifiedDate);

    @PUT("/api/song/{uuid}/incViews")
    Call<SongDTO> uploadView(@Path("uuid") String uuid);

    @POST("/api/song/upload")
    Call<SongDTO> uploadSong(@Body SongDTO songDTO);

    @GET("/api/song/{uuid}")
    Call<SongDTO> getSong(@Path("uuid") String uuid);

    @GET("/api/songsYoutube")
    Call<List<SongTitleDTO>> getSongsContainingYoutubeUrl();

    @PUT("/api/song/{uuid}/incFavourites")
    Call<SongDTO> uploadIncFavourite(@Path("uuid") String uuid);

    @GET("/api/songViews/language/{language}")
    Call<List<SongViewsDTO>> getSongViewsByLanguage(@Path("language") String language);
}
