package projector.api;

import com.bence.projector.common.dto.ProjectorVersionDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import projector.api.retrofit.ApiManager;
import projector.api.retrofit.ProjectorVersionApi;
import retrofit2.Call;

import java.net.ConnectException;
import java.net.UnknownHostException;
import java.util.List;

public class ProjectorVersionApiBean {
    private static final Logger LOG = LoggerFactory.getLogger(ProjectorVersionApiBean.class);
    private final ProjectorVersionApi projectorVersionApi;

    public ProjectorVersionApiBean() {
        projectorVersionApi = ApiManager.getClient().create(ProjectorVersionApi.class);
    }

    public List<ProjectorVersionDTO> getProjectorVersionsAfterNr(int projectorVersionsAfterNr) {
        Call<List<ProjectorVersionDTO>> call = projectorVersionApi.getProjectorVersionsAfterNr(projectorVersionsAfterNr);
        try {
            return call.execute().body();
        } catch (ConnectException | UnknownHostException ignored) {
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
        return null;
    }
}
