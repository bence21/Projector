package com.bence.songbook.api;

import android.content.Context;
import android.util.Log;

import com.bence.projector.common.dto.FavouriteSongDTO;
import com.bence.songbook.api.assembler.FavouriteSongAssembler;
import com.bence.songbook.api.retrofit.ApiManager;
import com.bence.songbook.api.retrofit.FavouriteSongApi;
import com.bence.songbook.models.FavouriteSong;
import com.bence.songbook.service.UserService;

import java.util.Date;
import java.util.List;

import retrofit2.Call;
import retrofit2.Response;

public class FavouriteSongApiBean {
    private static final String TAG = FavouriteSongApiBean.class.getName();
    private final FavouriteSongAssembler favouriteSongAssembler;
    private final FavouriteSongApi favouriteSongApi;
    private final Context context;

    public FavouriteSongApiBean(Context context) {
        this.context = context;
        favouriteSongApi = ApiManager.getClient().create(FavouriteSongApi.class);
        favouriteSongAssembler = FavouriteSongAssembler.getInstance(context);
    }

    public List<FavouriteSongDTO> uploadFavouriteSongs(List<FavouriteSong> favouriteSongs) {
        return uploadFavouriteSongs(favouriteSongs, false);
    }

    private List<FavouriteSongDTO> uploadFavouriteSongs(List<FavouriteSong> favouriteSongs, boolean secondTry) {
        List<FavouriteSongDTO> dtos = favouriteSongAssembler.createDTOS(favouriteSongs);
        Call<List<FavouriteSongDTO>> call = favouriteSongApi.uploadFavouriteSong(dtos);
        try {
            Response<List<FavouriteSongDTO>> favouriteSongDTOResponse = call.execute();
            if (favouriteSongDTOResponse.isSuccessful()) {
                return favouriteSongDTOResponse.body();
            } else if (!secondTry && UserService.getInstance().loginIfNeeded(favouriteSongDTOResponse.headers(), context, favouriteSongDTOResponse.raw())) {
                return uploadFavouriteSongs(favouriteSongs, true);
            }
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
        }
        return null;
    }

    public List<FavouriteSongDTO> getFavouriteSongs(Date serverModifiedDate) {
        return getFavouriteSongs(serverModifiedDate, false);
    }

    private List<FavouriteSongDTO> getFavouriteSongs(Date serverModifiedDate, boolean secondTry) {
        Call<List<FavouriteSongDTO>> call = favouriteSongApi.getFavouriteSongsAfterModifiedDate(serverModifiedDate.getTime());
        try {
            Response<List<FavouriteSongDTO>> response = call.execute();
            if (response.isSuccessful()) {
                return response.body();
            } else if (!secondTry && UserService.getInstance().loginIfNeeded(response.headers(), context, response.raw())) {
                return getFavouriteSongs(serverModifiedDate, true);
            }
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
        }
        return null;
    }
}
