package projector.api.retrofit;

import com.bence.projector.common.dto.UserDTO;

import retrofit2.Call;
import retrofit2.http.GET;

public interface UserApi {

    @GET("/api/username")
    Call<UserDTO> getLoggedInUser();
}
