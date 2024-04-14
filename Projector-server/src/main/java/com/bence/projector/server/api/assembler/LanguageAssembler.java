package com.bence.projector.server.api.assembler;

import com.bence.projector.common.dto.LanguageDTO;
import com.bence.projector.server.backend.model.Language;
import org.springframework.stereotype.Component;

@Component
public class LanguageAssembler implements GeneralAssembler<Language, LanguageDTO> {

    @Override
    public LanguageDTO createDto(Language language) {
        if (language == null) {
            return null;
        }
        LanguageDTO languageDTO = new LanguageDTO();
        languageDTO.setUuid(language.getUuid());
        languageDTO.setEnglishName(language.getEnglishName());
        languageDTO.setNativeName(language.getNativeName());
        languageDTO.setSize(language.getSongsCount());
        return languageDTO;
    }

    @Override
    public Language createModel(LanguageDTO languageDTO) {
        final Language language = new Language();
        language.setUuid(languageDTO.getUuid());
        return updateModel(language, languageDTO);
    }

    @Override
    public Language updateModel(Language language, LanguageDTO languageDTO) {
        language.setEnglishName(languageDTO.getEnglishName());
        language.setNativeName(languageDTO.getNativeName());
        return language;
    }
}
