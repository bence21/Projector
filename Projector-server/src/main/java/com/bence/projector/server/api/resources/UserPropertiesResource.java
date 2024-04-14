package com.bence.projector.server.api.resources;

import com.bence.projector.common.dto.UserPropertiesDTO;
import com.bence.projector.server.api.assembler.UserPropertiesAssembler;
import com.bence.projector.server.backend.model.Role;
import com.bence.projector.server.backend.model.User;
import com.bence.projector.server.backend.model.UserProperties;
import com.bence.projector.server.backend.service.LanguageService;
import com.bence.projector.server.backend.service.UserPropertiesService;
import com.bence.projector.server.backend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
public class UserPropertiesResource {
    private final UserPropertiesAssembler userPropertiesAssembler;
    private final UserService userService;
    private final LanguageService languageService;
    private final UserPropertiesService userPropertiesService;

    @Autowired
    public UserPropertiesResource(UserPropertiesAssembler userPropertiesAssembler, UserService userService, LanguageService languageService, UserPropertiesService userPropertiesService) {
        this.userPropertiesAssembler = userPropertiesAssembler;
        this.userService = userService;
        this.languageService = languageService;
        this.userPropertiesService = userPropertiesService;
    }

    public static User getUserFromPrincipalAndUserService(Principal principal, UserService userService) {
        if (principal != null) {
            String email = principal.getName();
            return userService.findByEmail(email);
        }
        return null;
    }

    @RequestMapping(value = "user/api/userProperties", method = RequestMethod.GET)
    public ResponseEntity<Object> getUserProperties(Principal principal) {
        User user = getUserFromPrincipal(principal);
        if (user != null) {
            UserProperties userProperties = getUserProperties(user);
            return new ResponseEntity<>(userPropertiesAssembler.createDto(userProperties), HttpStatus.ACCEPTED);
        }
        return new ResponseEntity<>(HttpStatus.PRECONDITION_FAILED);
    }

    private UserProperties getUserProperties(User user) {
        UserProperties userProperties;
        if (user.getRole() == Role.ROLE_ADMIN) {
            userProperties = user.getUserProperties(languageService.findAll());
        } else {
            userProperties = user.getUserProperties();
        }
        return userProperties;
    }

    private User getUserFromPrincipal(Principal principal) {
        return getUserFromPrincipalAndUserService(principal, userService);
    }

    @RequestMapping(value = "user/api/userProperties", method = RequestMethod.PUT)
    public ResponseEntity<Object> userProperties(@RequestBody final UserPropertiesDTO userPropertiesDTO, Principal principal) {
        User user = getUserFromPrincipal(principal);
        if (user != null) {
            UserProperties userProperties = getUserProperties(user);
            userPropertiesAssembler.updateModel(userProperties, userPropertiesDTO);
            userPropertiesService.save(userProperties);
            return new ResponseEntity<>(userPropertiesAssembler.createDto(userProperties), HttpStatus.ACCEPTED);
        }
        return new ResponseEntity<>(HttpStatus.PRECONDITION_FAILED);
    }
}
