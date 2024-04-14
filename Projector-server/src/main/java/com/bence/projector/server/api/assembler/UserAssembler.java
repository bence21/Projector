package com.bence.projector.server.api.assembler;

import com.bence.projector.common.dto.LanguageDTO;
import com.bence.projector.common.dto.UserDTO;
import com.bence.projector.server.backend.model.Language;
import com.bence.projector.server.backend.model.Role;
import com.bence.projector.server.backend.model.User;
import com.bence.projector.server.backend.service.LanguageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class UserAssembler implements GeneralAssembler<User, UserDTO> {

    private final LanguageAssembler languageAssembler;
    private final LanguageService languageService;

    @Autowired
    public UserAssembler(LanguageAssembler languageAssembler, LanguageService languageService) {
        this.languageAssembler = languageAssembler;
        this.languageService = languageService;
    }

    @Override
    public UserDTO createDto(User user) {
        if (user == null) {
            return null;
        }
        UserDTO userDTO = new UserDTO();
        userDTO.setUuid(user.getUuid());
        userDTO.setEmail(user.getEmail());
        userDTO.setPreferredLanguage(user.getPreferredLanguage());
        userDTO.setRole(user.getRole().getValue());
        userDTO.setSurname(user.getSurname());
        userDTO.setFirstName(user.getFirstName());
        userDTO.setActivated(user.isActivated());
        List<LanguageDTO> languageDTOS = languageAssembler.createDtoList(user.getReviewLanguages());
        if (languageDTOS == null) {
            languageDTOS = new ArrayList<>(0);
        }
        userDTO.setReviewLanguages(languageDTOS);
        userDTO.setModifiedDate(user.getModifiedDate());
        userDTO.setHadUploadedSongs(user.isHadUploadedSongs());
        return userDTO;
    }

    @Override
    public User createModel(UserDTO userDTO) {
        if (userDTO == null) {
            return null;
        }
        User user = new User();
        return updateModel(user, userDTO);
    }

    @Override
    public User updateModel(User user, UserDTO userDTO) {
        if (user != null) {
            user.setEmail(userDTO.getEmail());
            user.setPreferredLanguage(userDTO.getPreferredLanguage());
            user.setRole(Role.getInstance(userDTO.getRole()));
            user.setSurname(userDTO.getSurname());
            user.setFirstName(userDTO.getFirstName());
            List<Language> reviewLanguages = new ArrayList<>(userDTO.getReviewLanguages().size());
            for (LanguageDTO languageDTO : userDTO.getReviewLanguages()) {
                Language language = languageService.findOneByUuid(languageDTO.getUuid());
                if (language != null) {
                    reviewLanguages.add(language);
                }
            }
            user.setReviewLanguages(reviewLanguages);
        }
        return user;
    }
}
