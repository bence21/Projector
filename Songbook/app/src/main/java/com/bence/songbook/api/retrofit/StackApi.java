package com.bence.songbook.api.retrofit;

import com.bence.projector.common.dto.StackDTO;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface StackApi {

    @POST("/api/stack")
    Call<StackDTO> create(@Body StackDTO stackDTO);
}
