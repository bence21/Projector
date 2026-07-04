package projector.api;

import com.bence.projector.common.dto.SongCollectionDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import projector.api.assembler.SongCollectionAssembler;
import projector.api.retrofit.ApiManager;
import projector.api.retrofit.SongCollectionApi;
import projector.model.Language;
import projector.model.SongCollection;
import retrofit2.Call;

import java.net.UnknownHostException;
import java.util.Date;
import java.util.List;

public class SongCollectionApiBean {
    private static final Logger LOG = LoggerFactory.getLogger(SongCollectionApiBean.class);
    private SongCollectionApi songCollectionApi;
    private SongCollectionAssembler songCollectionAssembler;

    public SongCollectionApiBean() {
        songCollectionApi = ApiManager.getClient().create(SongCollectionApi.class);
        songCollectionAssembler = SongCollectionAssembler.getInstance();
    }

    public RemoteFetchResult<List<SongCollection>> getSongCollectionsResult(Language language, Date lastModifiedDate) {
        Call<List<SongCollectionDTO>> call = songCollectionApi.getSongCollections(language.getUuid(), lastModifiedDate.getTime());
        return RemoteFetchSupport.execute(call, songCollectionDTOs -> {
            List<SongCollection> songCollections = songCollectionAssembler.createModelList(songCollectionDTOs);
            for (SongCollection songCollection : songCollections) {
                songCollection.setLanguage(language);
            }
            return songCollections;
        });
    }

    public SongCollection uploadSongCollection(SongCollection songCollection) {
        final SongCollectionDTO dto = songCollectionAssembler.createDto(songCollection);
        Call<SongCollectionDTO> call = songCollectionApi.uploadSongCollection(dto);
        try {
            SongCollectionDTO songCollectionDTO = call.execute().body();
            return songCollectionAssembler.createModel(songCollectionDTO);
        } catch (UnknownHostException e) {
            return null;
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
        return null;
    }
}
