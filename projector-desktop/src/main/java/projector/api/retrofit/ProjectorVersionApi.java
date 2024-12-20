package projector.api.retrofit;

import com.bence.projector.common.dto.ProjectorVersionDTO;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

import java.util.List;

public interface ProjectorVersionApi {
    @GET("/api/projectorVersionsAfterNr/v5/{nr}")
    Call<List<ProjectorVersionDTO>> getProjectorVersionsAfterNr(@Path("nr") int nr);
}
