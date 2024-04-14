package com.bence.projector.server.api.resources.util;

import com.bence.projector.server.backend.model.User;
import com.bence.projector.server.backend.service.UserService;

import java.security.Principal;

public class UserPrincipalUtil {

    public static User getUserFromPrincipalAndUserService(Principal principal, UserService userService) {
        if (principal != null) {
            String email = principal.getName();
            return userService.findByEmail(email);
        }
        return null;
    }
}
