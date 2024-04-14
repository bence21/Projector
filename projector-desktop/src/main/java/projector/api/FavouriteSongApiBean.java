package projector.api;


import com.bence.projector.common.dto.FavouriteSongDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import projector.api.assembler.FavouriteSongAssembler;
import projector.api.retrofit.ApiManager;
import projector.api.retrofit.FavouriteSongApi;
import projector.controller.util.UserService;
import projector.model.FavouriteSong;
import retrofit2.Call;
import retrofit2.Response;

import java.net.ConnectException;
import java.util.Date;
import java.util.List;

public class FavouriteSongApiBean {
    private static final Logger LOG = LoggerFactory.getLogger(FavouriteSongApiBean.class);
    private final FavouriteSongAssembler favouriteSongAssembler;
    private final FavouriteSongApi favouriteSongApi;

    public FavouriteSongApiBean() {
        favouriteSongApi = ApiManager.getClient().create(FavouriteSongApi.class);
        favouriteSongAssembler = FavouriteSongAssembler.getInstance();
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
            } else if (!secondTry && UserService.getInstance().loginIfNeeded(favouriteSongDTOResponse.headers(), favouriteSongDTOResponse.raw())) {
                return uploadFavouriteSongs(favouriteSongs, true);
            }
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
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
            } else if (!secondTry && UserService.getInstance().loginIfNeeded(response.headers(), response.raw())) {
                return getFavouriteSongs(serverModifiedDate, true);
            }
        } catch (ConnectException ignored) {
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
        return null;
    }
}
