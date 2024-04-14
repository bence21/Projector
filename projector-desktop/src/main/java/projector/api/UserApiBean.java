package projector.api;


import com.bence.projector.common.dto.UserDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import projector.api.retrofit.ApiManager;
import projector.api.retrofit.UserApi;
import retrofit2.Call;

public class UserApiBean {
    private static final Logger LOG = LoggerFactory.getLogger(UserApiBean.class);
    private final UserApi userApi;

    public UserApiBean() {
        userApi = ApiManager.getClient().create(UserApi.class);
    }

    public UserDTO getLoggedInUser() {
        Call<UserDTO> call = userApi.getLoggedInUser();
        try {
            return call.execute().body();
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
        return null;
    }
}
