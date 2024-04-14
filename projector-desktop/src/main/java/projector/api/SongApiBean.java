package projector.api;

import com.bence.projector.common.dto.LanguageDTO;
import com.bence.projector.common.dto.LoginDTO;
import com.bence.projector.common.dto.LoginSongDTO;
import com.bence.projector.common.dto.SongDTO;
import com.bence.projector.common.dto.SongFavouritesDTO;
import com.bence.projector.common.dto.SongViewsDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import projector.api.assembler.SongAssembler;
import projector.api.retrofit.ApiManager;
import projector.api.retrofit.SongApi;
import projector.model.Language;
import projector.model.Song;
import projector.service.ServiceManager;
import retrofit2.Call;
import retrofit2.Response;

import java.net.ConnectException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class SongApiBean {
    private static final Logger LOG = LoggerFactory.getLogger(SongApiBean.class);
    private final SongApi songApi;
    private final SongAssembler songAssembler;

    public SongApiBean() {
        songApi = ApiManager.getClient().create(SongApi.class);
        songAssembler = SongAssembler.getInstance();
    }

    public List<Song> getSongs() {
        Call<List<SongDTO>> call = songApi.getSongs();
        return executeSongsCall(call);
    }

    private List<Song> executeSongsCall(Call<List<SongDTO>> call) {
        try {
            List<SongDTO> songDTOs = call.execute().body();
            removeDeleted(songDTOs);
            return songAssembler.createModelList(songDTOs);
        } catch (UnknownHostException e) {
            return null;
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
        return null;
    }

    private void removeDeleted(List<SongDTO> songDTOs) {
        if (songDTOs != null) {
            List<SongDTO> toDelete = new ArrayList<>();
            for (SongDTO songDTO : songDTOs) {
                if (songDTO.isDeleted()) {
                    toDelete.add(songDTO);
                }
            }
            songDTOs.removeAll(toDelete);
        }
    }

    public Song updateSong(Song song, LoginDTO loginDTO) throws ApiException {
        final SongDTO dto = songAssembler.createDto(song);
        Date serverModifiedDate = song.getServerModifiedDate();
        if (serverModifiedDate != null) {
            dto.setModifiedDate(serverModifiedDate);
        }
        LoginSongDTO loginSongDTO = new LoginSongDTO();
        loginSongDTO.setSongDTO(dto);
        loginSongDTO.setUsername(loginDTO.getUsername());
        loginSongDTO.setPassword(loginDTO.getPassword());
        Call<SongDTO> call = songApi.updateSong(song.getUuid(), loginSongDTO);
        try {
            Response<SongDTO> response = call.execute();
            int code = response.code();
            if (response.code() == 409) {
                throw new ApiException("Already modified by others. You need to update your song first!");
            }
            System.out.println("response.code() = " + code);
            SongDTO songDTO = response.body();
            return songAssembler.createModel(songDTO);
        } catch (UnknownHostException e) {
            return null;
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
        return null;
    }

    private Song doCallSong(Call<SongDTO> call) {
        try {
            SongDTO songDTO = call.execute().body();
            return songAssembler.createModel(songDTO);
        } catch (UnknownHostException e) {
            return null;
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
        return null;
    }

    public Song uploadSong(Song song) {
        final SongDTO dto = songAssembler.createDto(song);
        Call<SongDTO> call = songApi.uploadSong(dto);
        return doCallSong(call);
    }

    public List<Song> getSongsByLanguageAndAfterModifiedDate(Language language, Long modifiedDate) {
        Call<List<SongDTO>> call = songApi.getSongsByLanguageAndAfterModifiedDate(language.getUuid(), modifiedDate);
        List<Song> songs;
        try {
            List<SongDTO> songDTOs = call.execute().body();
            songs = songAssembler.createModelList(songDTOs);
            return songs;
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
        return null;
    }

    public List<SongViewsDTO> getSongViewsByLanguage(Language language) {
        Call<List<SongViewsDTO>> call = songApi.getSongViewsByLanguage(language.getUuid());
        try {
            return call.execute().body();
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
        return null;
    }

    public List<SongFavouritesDTO> getSongFavouritesByLanguage(Language language) {
        Call<List<SongFavouritesDTO>> call = songApi.getSongFavouritesByLanguage(language.getUuid());
        try {
            return call.execute().body();
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
        return null;
    }

    public Song getSongByUuid(String uuid) {
        Call<SongDTO> call = songApi.getSong(uuid);
        try {
            SongDTO songDTO = call.execute().body();
            Song song = songAssembler.createModel(songDTO);
            if (song == null || songDTO == null) {
                return null;
            }
            LanguageDTO languageDTO = songDTO.getLanguageDTO();
            if (languageDTO != null) {
                song.setLanguage(ServiceManager.getLanguageService().findByUuid(languageDTO.getUuid()));
            }
            return song;
        } catch (ConnectException ignored) {
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
        return null;
    }
}
