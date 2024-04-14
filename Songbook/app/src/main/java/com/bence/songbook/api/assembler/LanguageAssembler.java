package com.bence.songbook.api.assembler;

import com.bence.projector.common.dto.LanguageDTO;
import com.bence.songbook.models.Language;

import java.util.ArrayList;
import java.util.List;

public class LanguageAssembler implements GeneralAssembler<Language, LanguageDTO> {

    private static LanguageAssembler instance;

    private LanguageAssembler() {
    }

    public static LanguageAssembler getInstance() {
        if (instance == null) {
            instance = new LanguageAssembler();
        }
        return instance;
    }

    @Override
    public synchronized Language createModel(LanguageDTO languageDTO) {
        return updateModel(new Language(), languageDTO);
    }

    @Override
    public synchronized Language updateModel(Language language, LanguageDTO languageDTO) {
        if (language != null) {
            language.setUuid(languageDTO.getUuid());
            language.setEnglishName(languageDTO.getEnglishName());
            language.setNativeName(languageDTO.getNativeName());
            language.setSize(languageDTO.getSize());
            language.setSelectedForDownload(false);
        }
        return language;
    }

    @Override
    public synchronized List<Language> createModelList(List<LanguageDTO> ds) {
        if (ds == null) {
            return null;
        }
        List<Language> models = new ArrayList<>();
        for (LanguageDTO languageDTO : ds) {
            models.add(createModel(languageDTO));
        }
        return models;
    }

    LanguageDTO createDTO(Language language) {
        LanguageDTO languageDTO = new LanguageDTO();
        languageDTO.setEnglishName(language.getEnglishName());
        languageDTO.setNativeName(language.getNativeName());
        languageDTO.setUuid(language.getUuid());
        return languageDTO;
    }
}
