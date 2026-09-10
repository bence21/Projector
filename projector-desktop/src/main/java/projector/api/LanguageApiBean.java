package projector.api;

import com.bence.projector.common.dto.LanguageDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import projector.api.assembler.LanguageAssembler;
import projector.api.retrofit.ApiManager;
import projector.api.retrofit.LanguageApi;
import projector.model.Language;
import retrofit2.Call;

import java.util.List;

public class LanguageApiBean {
    private static final Logger LOG = LoggerFactory.getLogger(LanguageApiBean.class);
    private final LanguageApi languageApi;
    private final LanguageAssembler languageAssembler;

    public LanguageApiBean() {
        languageApi = ApiManager.getClient().create(LanguageApi.class);
        languageAssembler = LanguageAssembler.getInstance();
    }

    public RemoteFetchResult<List<Language>> getLanguagesResult() {
        Call<List<LanguageDTO>> call = languageApi.getLanguages();
        return fetchLanguages(call);
    }

    public List<Language> getLanguages() {
        return getLanguagesResult().getDataOrNull();
    }

    public List<Language> getDeletedLanguages() {
        Call<List<LanguageDTO>> call = languageApi.getDeletedLanguages();
        RemoteFetchResult<List<Language>> result = fetchLanguages(call);
        if (result.isFailure()) {
            LOG.warn("Failed to fetch deleted languages: {}", result.getFailureKind());
        }
        return result.getDataOrNull();
    }

    private RemoteFetchResult<List<Language>> fetchLanguages(Call<List<LanguageDTO>> call) {
        return RemoteFetchSupport.execute(call, languageAssembler::createModelList);
    }
}
