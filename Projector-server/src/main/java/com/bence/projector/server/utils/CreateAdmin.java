package com.bence.projector.server.utils;

import com.bence.projector.server.backend.model.Role;
import com.bence.projector.server.backend.model.User;
import com.bence.projector.server.backend.service.UserService;

public class CreateAdmin {

    public static void registerAdmin(UserService userService) {
        final String email = "developer@yahoo.com";
        final User byEmail = userService.findByEmail(email);
        if (byEmail == null) {
            User user = new User();
            user.setEmail(email);
            user.setPassword("asuhdoifhkj12lhrzxvc8ui2134fja");
            user.setRole(Role.ROLE_ADMIN);
            userService.registerUser(user);
        }
    }
}
