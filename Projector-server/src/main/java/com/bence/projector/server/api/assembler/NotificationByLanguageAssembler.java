package com.bence.projector.server.api.assembler;

import com.bence.projector.common.dto.LanguageDTO;
import com.bence.projector.common.dto.NotificationByLanguageDTO;
import com.bence.projector.server.backend.model.Language;
import com.bence.projector.server.backend.model.NotificationByLanguage;
import com.bence.projector.server.backend.service.LanguageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class NotificationByLanguageAssembler implements GeneralAssembler<NotificationByLanguage, NotificationByLanguageDTO> {
    private final LanguageService languageService;
    private final LanguageAssembler languageAssembler;

    @Autowired
    public NotificationByLanguageAssembler(LanguageService languageService, LanguageAssembler languageAssembler) {
        this.languageService = languageService;
        this.languageAssembler = languageAssembler;
    }

    @Override
    public NotificationByLanguageDTO createDto(NotificationByLanguage notificationByLanguage) {
        NotificationByLanguageDTO dto = new NotificationByLanguageDTO();
        LanguageDTO languageDTO = languageAssembler.createDto(notificationByLanguage.getLanguage());
        dto.setLanguage(languageDTO);
        dto.setNewSongs(notificationByLanguage.isNewSongs());
        dto.setSuggestions(notificationByLanguage.isSuggestions());
        dto.setSuggestionsDelay(notificationByLanguage.getSuggestionsDelay());
        dto.setNewSongsDelay(notificationByLanguage.getNewSongsDelay());
        return dto;
    }

    @Override
    public NotificationByLanguage createModel(NotificationByLanguageDTO notificationByLanguageDTO) {
        NotificationByLanguage notificationByLanguage = new NotificationByLanguage();
        return updateModel(notificationByLanguage, notificationByLanguageDTO);
    }

    @Override
    public NotificationByLanguage updateModel(NotificationByLanguage notificationByLanguage, NotificationByLanguageDTO notificationByLanguageDTO) {
        LanguageDTO languageDTO = notificationByLanguageDTO.getLanguage();
        Language language = languageService.findOneByUuid(languageDTO.getUuid());
        notificationByLanguage.setLanguage(language);
        notificationByLanguage.setNewSongs(notificationByLanguageDTO.getNewSongs());
        notificationByLanguage.setSuggestions(notificationByLanguageDTO.getSuggestions());
        notificationByLanguage.setSuggestionsDelay(notificationByLanguageDTO.getSuggestionsDelay());
        notificationByLanguage.setNewSongsDelay(notificationByLanguageDTO.getNewSongsDelay());
        return notificationByLanguage;
    }
}
