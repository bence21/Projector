package com.bence.songbook.api.retrofit;

import com.bence.projector.common.dto.SongCollectionDTO;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface SongCollectionApi {
    @GET("/api/songCollections/language/{language}/lastModifiedDate/{lastModifiedDate}")
    Call<List<SongCollectionDTO>> getSongCollections(@Path("language") String language, @Path("lastModifiedDate") Long lastModifiedDate);
}
