package projector.api.retrofit;

import com.bence.projector.common.dto.LanguageDTO;
import retrofit2.Call;
import retrofit2.http.GET;

import java.util.List;

public interface LanguageApi {
    @GET("/api/languages")
    Call<List<LanguageDTO>> getLanguages();

    @GET("/api/languages/deleted")
    Call<List<LanguageDTO>> getDeletedLanguages();
}
