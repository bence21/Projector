package com.bence.songbook.api;

import android.content.Context;
import android.util.Log;

import com.bence.projector.common.dto.SongDTO;
import com.bence.projector.common.dto.SongTitleDTO;
import com.bence.projector.common.dto.SongViewsDTO;
import com.bence.songbook.ProgressMessage;
import com.bence.songbook.api.assembler.SongAssembler;
import com.bence.songbook.api.retrofit.ApiManager;
import com.bence.songbook.api.retrofit.SongApi;
import com.bence.songbook.models.Language;
import com.bence.songbook.models.Song;
import com.bence.songbook.service.UserService;

import java.net.UnknownHostException;
import java.util.Date;
import java.util.List;

import retrofit2.Call;
import retrofit2.Response;

public class SongApiBean {

    public enum SongUploadFailureKind {
        SUCCESS,
        NEEDS_SIGN_IN,
        SESSION_NOT_REFRESHED,
        NETWORK_OR_SERVER
    }

    public static final class SongUploadResult {
        private final Song song;
        private final SongUploadFailureKind failureKind;

        private SongUploadResult(Song song, SongUploadFailureKind failureKind) {
            this.song = song;
            this.failureKind = failureKind;
        }

        public static SongUploadResult success(Song song) {
            return new SongUploadResult(song, SongUploadFailureKind.SUCCESS);
        }

        public static SongUploadResult failure(SongUploadFailureKind kind) {
            return new SongUploadResult(null, kind);
        }

        public Song getSong() {
            return song;
        }

        public SongUploadFailureKind getFailureKind() {
            return failureKind;
        }
    }

    private static final String TAG = SongApiBean.class.getName();
    private final SongApi songApi;
    private final SongAssembler songAssembler;

    public SongApiBean() {
        songApi = ApiManager.getClient().create(SongApi.class);
        songAssembler = SongAssembler.getInstance();
    }

    public List<Song> getSongs(final ProgressMessage progressMessage) {
        Call<List<SongDTO>> call = songApi.getSongs();
        try {
            List<SongDTO> songDTOs = call.execute().body();
            return songAssembler.createModelList(songDTOs, progressMessage);
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
        }
        return null;
    }

    public List<SongTitleDTO> getSongsContainingYoutubeUrl() {
        Call<List<SongTitleDTO>> call = songApi.getSongsContainingYoutubeUrl();
        try {
            return call.execute().body();
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
        }
        return null;
    }

    public List<Song> getSongsAfterModifiedDate(final ProgressMessage progressMessage, Date modifiedDate) {
        Call<List<SongDTO>> call = songApi.getSongsAfterModifiedDate(modifiedDate.getTime());
        try {
            List<SongDTO> songDTOs = call.execute().body();
            return songAssembler.createModelList(songDTOs, progressMessage);
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
        }
        return null;
    }

    public List<Song> getSongsAfterModifiedDate(Date modifiedDate) {
        Call<List<SongDTO>> call = songApi.getSongsAfterModifiedDate(modifiedDate.getTime());
        try {
            List<SongDTO> songDTOs = call.execute().body();
            return songAssembler.createModelList(songDTOs);
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
        }
        return null;
    }

    public List<Song> getSongsByLanguage(Language language, ProgressMessage progressMessage) {
        Call<List<SongDTO>> call = songApi.getSongsByLanguage(language.getUuid());
        List<Song> songs = null;
        try {
            List<SongDTO> songDTOs = call.execute().body();
            if (songDTOs != null) {
                progressMessage.onSetMax(songDTOs.size());
            }
            songs = songAssembler.createModelList(songDTOs, progressMessage);
            System.out.println("songs = " + songs);
            return songs;
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
        }
        return songs;
    }

    public List<Song> getSongsByLanguageAndAfterModifiedDate(Language language, Long modifiedDate) {
        Call<List<SongDTO>> call = songApi.getSongsByLanguageAndAfterModifiedDate(language.getUuid(), modifiedDate);
        List<Song> songs;
        try {
            List<SongDTO> songDTOs = call.execute().body();
            songs = songAssembler.createModelList(songDTOs);
            return songs;
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
        }
        return null;
    }

    public List<Song> getSongsByLanguageAndAfterModifiedDate(Language language, Long modifiedDate, ProgressMessage progressMessage) {
        Call<List<SongDTO>> call = songApi.getSongsByLanguageAndAfterModifiedDate(language.getUuid(), modifiedDate);
        List<Song> songs;
        try {
            List<SongDTO> songDTOs = call.execute().body();
            if (songDTOs != null) {
                progressMessage.onSetMax(songDTOs.size());
            }
            songs = songAssembler.createModelList(songDTOs, progressMessage);
            return songs;
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
        }
        return null;
    }

    public SongDTO uploadView(Song song) {
        Call<SongDTO> call = songApi.uploadView(song.getUuid());
        try {
            return call.execute().body();
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
        }
        return null;
    }

    public SongUploadResult uploadSong(Song song, Context context) {
        Context appContext = context != null ? context.getApplicationContext() : null;
        return uploadSong(song, appContext, false);
    }

    private SongUploadResult uploadSong(Song song, Context appContext, boolean secondTry) {
        final SongDTO dto = songAssembler.createDto(song);
        Call<SongDTO> call = songApi.uploadSong(dto);
        try {
            Response<SongDTO> response = call.execute();
            if (response.isSuccessful()) {
                SongDTO body = response.body();
                Song model = body != null ? songAssembler.createModel(body) : null;
                if (model != null && model.getUuid() != null && !model.getUuid().trim().isEmpty()) {
                    return SongUploadResult.success(model);
                }
            }
            if (!secondTry && appContext != null
                    && UserService.getInstance().loginIfNeeded(response.headers(), appContext, response.raw())) {
                return uploadSong(song, appContext, true);
            }
            if (UserService.getInstance().loginNeeded(response.headers(), response.raw())) {
                if (appContext == null || !UserService.getInstance().isLoggedIn(appContext)) {
                    return SongUploadResult.failure(SongUploadFailureKind.NEEDS_SIGN_IN);
                }
                return SongUploadResult.failure(SongUploadFailureKind.SESSION_NOT_REFRESHED);
            }
            return SongUploadResult.failure(SongUploadFailureKind.NETWORK_OR_SERVER);
        } catch (UnknownHostException e) {
            return SongUploadResult.failure(SongUploadFailureKind.NETWORK_OR_SERVER);
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
            return SongUploadResult.failure(SongUploadFailureKind.NETWORK_OR_SERVER);
        }
    }

    public Song getSong(String songUuid) {
        Call<SongDTO> call = songApi.getSong(songUuid);
        try {
            SongDTO songDTO = call.execute().body();
            return songAssembler.createModel(songDTO);
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
        }
        return null;
    }

    public SongDTO uploadIncFavourite(Song song) {
        Call<SongDTO> call = songApi.uploadIncFavourite(song.getUuid());
        try {
            return call.execute().body();
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
        }
        return null;
    }

    public List<SongViewsDTO> getSongViewsByLanguage(Language language) {
        Call<List<SongViewsDTO>> call = songApi.getSongViewsByLanguage(language.getUuid());
        try {
            return call.execute().body();
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
        }
        return null;
    }
}
