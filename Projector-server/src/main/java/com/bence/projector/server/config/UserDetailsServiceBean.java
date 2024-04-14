package com.bence.projector.server.config;

import com.bence.projector.server.backend.model.User;
import com.bence.projector.server.backend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserDetailsServiceBean implements UserDetailsService {

    @Autowired
    private UserService userService;

    @Override
    public UserDetails loadUserByUsername(final String username) throws UsernameNotFoundException {
        User user = userService.findByEmail(username);
        if (user != null) {
            return new org.springframework.security.core.userdetails.User(user.getEmail(), user.getPassword(),
                    AuthorityUtils.createAuthorityList(user.getRole().toString()));
        } else {
            throw new UsernameNotFoundException("THIS_USER_DOES_NOT_EXIST");
        }
    }
}
