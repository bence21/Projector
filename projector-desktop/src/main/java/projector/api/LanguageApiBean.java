package projector.api;

import com.bence.projector.common.dto.LanguageDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import projector.api.assembler.LanguageAssembler;
import projector.api.retrofit.ApiManager;
import projector.api.retrofit.LanguageApi;
import projector.model.Language;
import retrofit2.Call;

import java.net.ConnectException;
import java.util.List;

public class LanguageApiBean {
    private static final Logger LOG = LoggerFactory.getLogger(LanguageApiBean.class);
    private final LanguageApi languageApi;
    private final LanguageAssembler languageAssembler;

    public LanguageApiBean() {
        languageApi = ApiManager.getClient().create(LanguageApi.class);
        languageAssembler = LanguageAssembler.getInstance();
    }

    public List<Language> getLanguages() {
        Call<List<LanguageDTO>> call = languageApi.getLanguages();
        return getLanguages(call);
    }

    private List<Language> getLanguages(Call<List<LanguageDTO>> call) {
        try {
            List<LanguageDTO> languageDTOs = call.execute().body();
            return languageAssembler.createModelList(languageDTOs);
        } catch (ConnectException ignored) {
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
        return null;
    }

    public List<Language> getDeletedLanguages() {
        Call<List<LanguageDTO>> call = languageApi.getDeletedLanguages();
        return getLanguages(call);
    }
}
