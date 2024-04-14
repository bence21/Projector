package com.bence.songbook.api;

import android.util.Log;

import com.bence.projector.common.dto.SongCollectionDTO;
import com.bence.songbook.ProgressMessage;
import com.bence.songbook.api.assembler.SongCollectionAssembler;
import com.bence.songbook.api.retrofit.ApiManager;
import com.bence.songbook.api.retrofit.SongCollectionApi;
import com.bence.songbook.models.Language;
import com.bence.songbook.models.SongCollection;

import java.io.IOException;
import java.util.Date;
import java.util.List;

import retrofit2.Call;

public class SongCollectionApiBean {
    private static final String TAG = SongCollectionApiBean.class.getName();
    private SongCollectionApi songCollectionApi;
    private SongCollectionAssembler songCollectionAssembler;

    public SongCollectionApiBean() {
        songCollectionApi = ApiManager.getClient().create(SongCollectionApi.class);
        songCollectionAssembler = SongCollectionAssembler.getInstance();
    }

    public List<SongCollection> getSongCollections(Language language, Date lastModifiedDate) {
        Call<List<SongCollectionDTO>> call = songCollectionApi.getSongCollections(language.getUuid(), lastModifiedDate.getTime());
        try {
            List<SongCollectionDTO> songCollectionDTOs = call.execute().body();
            List<SongCollection> songCollections = songCollectionAssembler.createModelList(songCollectionDTOs);
            for (SongCollection songCollection : songCollections) {
                songCollection.setLanguage(language);
            }
            return songCollections;
        } catch (IOException e) {
            Log.e(TAG, e.getMessage(), e);
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
        }
        return null;
    }

    public List<SongCollection> getSongCollections(Language language, Date lastModifiedDate, ProgressMessage progressMessage) {
        Call<List<SongCollectionDTO>> call = songCollectionApi.getSongCollections(language.getUuid(), lastModifiedDate.getTime());
        try {
            List<SongCollectionDTO> songCollectionDTOs = call.execute().body();
            if (songCollectionDTOs != null) {
                progressMessage.onSetMax(songCollectionDTOs.size());
            }
            List<SongCollection> songCollections = songCollectionAssembler.createModelList(songCollectionDTOs, progressMessage);
            for (SongCollection songCollection : songCollections) {
                songCollection.setLanguage(language);
            }
            return songCollections;
        } catch (IOException e) {
            Log.e(TAG, e.getMessage(), e);
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
        }
        return null;
    }
}
