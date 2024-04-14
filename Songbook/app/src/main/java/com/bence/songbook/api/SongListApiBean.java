package com.bence.songbook.api;

import android.content.Context;
import android.util.Log;

import com.bence.projector.common.dto.SongListDTO;
import com.bence.songbook.api.assembler.SongListAssembler;
import com.bence.songbook.api.retrofit.ApiManager;
import com.bence.songbook.api.retrofit.SongListApi;
import com.bence.songbook.models.SongList;

import retrofit2.Call;

public class SongListApiBean {
    private static final String TAG = SongListApiBean.class.getName();
    private final SongListAssembler songListAssembler;
    private final SongListApi songListApi;

    public SongListApiBean(Context context) {
        songListApi = ApiManager.getClient().create(SongListApi.class);
        songListAssembler = SongListAssembler.getInstance(context);
    }

    public SongListDTO uploadSongList(SongList songList) {
        if (songList == null) {
            return null;
        }
        SongListDTO dto = songListAssembler.createDto(songList);
        Call<SongListDTO> call = songListApi.uploadSongList(dto);
        try {
            return call.execute().body();
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
        }
        return null;
    }

    public SongList getSongList(String songListUuid) {
        Call<SongListDTO> call = songListApi.getSongList(songListUuid);
        try {
            SongListDTO songListDTO = call.execute().body();
            return songListAssembler.createModel(songListDTO);
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
        }
        return null;
    }
}
