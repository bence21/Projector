package projector.api.retrofit;

import com.bence.projector.common.dto.BibleDTO;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

import java.util.List;

public interface BibleApi {
    @GET("/api/bibles")
    Call<List<BibleDTO>> getBibles();

    @GET("/api/bibleTitles")
    Call<List<BibleDTO>> getBibleTitles();

    @GET("/api/bible/{uuid}")
    Call<BibleDTO> getBible(@Path("uuid") String uuid);

    @POST("/api/bible")
    Call<BibleDTO> postBible(@Body BibleDTO bibleDTO);
}
