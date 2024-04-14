package com.bence.projector.server.api.resources;

import com.bence.projector.common.dto.UserDTO;
import com.bence.projector.server.api.assembler.UserAssembler;
import com.bence.projector.server.backend.model.User;
import com.bence.projector.server.backend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.security.Principal;

@Controller
public class LoginResource {

    @Autowired
    private UserService userService;

    @Autowired
    private UserAssembler userAssembler;

    @RequestMapping(method = RequestMethod.GET, value = "/api/username")
    public ResponseEntity<UserDTO> getUsername(Principal principal) {
        if (principal != null) {
            String email = principal.getName();
            User user = userService.findByEmail(email);
            if (user != null) {
                return new ResponseEntity<>(userAssembler.createDto(user), HttpStatus.ACCEPTED);
            }
        }
        return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
    }
}
