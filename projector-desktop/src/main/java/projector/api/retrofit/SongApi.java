package projector.api.retrofit;

import com.bence.projector.common.dto.LoginSongDTO;
import com.bence.projector.common.dto.SongDTO;
import com.bence.projector.common.dto.SongFavouritesDTO;
import com.bence.projector.common.dto.SongViewsDTO;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

import java.util.List;

public interface SongApi {
    @GET("/api/songs")
    Call<List<SongDTO>> getSongs();

    @PUT("/password/api/song/{songId}")
    Call<SongDTO> updateSong(@Path("songId") String songUuid, @Body LoginSongDTO loginSongDTO);

    @GET("/api/songsAfterModifiedDate/{modifiedDate}")
    Call<List<SongDTO>> getSongsAfterModifiedDate(@Path("modifiedDate") Long modifiedDate);

    @POST("/api/song/upload")
    Call<SongDTO> uploadSong(@Body SongDTO songDTO);

    @GET("/api/songs/language/{language}/modifiedDate/{modifiedDate}")
    Call<List<SongDTO>> getSongsByLanguageAndAfterModifiedDate(@Path("language") String languageUuid, @Path("modifiedDate") Long modifiedDate);

    @GET("/api/songViews/language/{language}")
    Call<List<SongViewsDTO>> getSongViewsByLanguage(@Path("language") String language);

    @GET("/api/songFavourites/language/{language}")
    Call<List<SongFavouritesDTO>> getSongFavouritesByLanguage(@Path("language") String language);

    @GET("/api/song/{uuid}")
    Call<SongDTO> getSong(@Path("uuid") String uuid);
}
