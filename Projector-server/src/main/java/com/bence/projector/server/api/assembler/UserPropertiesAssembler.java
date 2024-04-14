package com.bence.projector.server.api.assembler;

import com.bence.projector.common.dto.UserPropertiesDTO;
import com.bence.projector.server.backend.model.NotificationByLanguage;
import com.bence.projector.server.backend.model.UserProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class UserPropertiesAssembler implements GeneralAssembler<UserProperties, UserPropertiesDTO> {
    private final NotificationByLanguageAssembler notificationByLanguageAssembler;

    @Autowired
    public UserPropertiesAssembler(NotificationByLanguageAssembler notificationByLanguageAssembler) {
        this.notificationByLanguageAssembler = notificationByLanguageAssembler;
    }

    @Override
    public UserPropertiesDTO createDto(UserProperties userProperties) {
        UserPropertiesDTO userPropertiesDTO = new UserPropertiesDTO();
        userPropertiesDTO.setNotificationsByLanguage(notificationByLanguageAssembler.createDtoList(userProperties.getNotifications()));
        return userPropertiesDTO;
    }

    @Override
    public UserProperties createModel(UserPropertiesDTO userPropertiesDTO) {
        UserProperties userProperties = new UserProperties();
        return updateModel(userProperties, userPropertiesDTO);
    }

    @Override
    public UserProperties updateModel(UserProperties userProperties, UserPropertiesDTO userPropertiesDTO) {
        List<NotificationByLanguage> notifications = notificationByLanguageAssembler.createModelList(userPropertiesDTO.getNotificationsByLanguage());
        userProperties.setNotifications_(notifications);
        return userProperties;
    }
}
