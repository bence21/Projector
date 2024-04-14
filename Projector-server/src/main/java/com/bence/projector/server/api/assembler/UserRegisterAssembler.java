package com.bence.projector.server.api.assembler;

import com.bence.projector.common.dto.UserRegisterDTO;
import com.bence.projector.server.backend.model.User;
import org.springframework.stereotype.Component;

@Component
public class UserRegisterAssembler implements GeneralAssembler<User, UserRegisterDTO> {

    @Override
    public UserRegisterDTO createDto(User user) {
        if (user == null) {
            return null;
        }
        UserRegisterDTO userRegisterDTO = new UserRegisterDTO();
        userRegisterDTO.setUuid(user.getUuid());
        userRegisterDTO.setId(user.getUuid());
        userRegisterDTO.setEmail(user.getEmail());
        userRegisterDTO.setSurname(user.getSurname());
        userRegisterDTO.setFirstName(user.getFirstName());
        userRegisterDTO.setPhone(user.getPhone());
        userRegisterDTO.setPassword(user.getPassword());
        userRegisterDTO.setPreferredLanguage(user.getPreferredLanguage());
        return userRegisterDTO;
    }

    @Override
    public User createModel(UserRegisterDTO userRegisterDTO) {
        User user = new User();
        return updateModel(user, userRegisterDTO);
    }

    @Override
    public User updateModel(User user, UserRegisterDTO userRegisterDTO) {
        if (user != null) {
            user.setEmail(userRegisterDTO.getEmail());
            user.setSurname(userRegisterDTO.getSurname());
            user.setFirstName(userRegisterDTO.getFirstName());
            user.setPhone(userRegisterDTO.getPhone());
            user.setPassword(userRegisterDTO.getPassword());
            user.setPreferredLanguage(userRegisterDTO.getPreferredLanguage());
        }
        return user;
    }

}
