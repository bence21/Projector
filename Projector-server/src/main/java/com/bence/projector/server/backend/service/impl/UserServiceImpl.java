package com.bence.projector.server.backend.service.impl;

import com.bence.projector.server.backend.model.Language;
import com.bence.projector.server.backend.model.Role;
import com.bence.projector.server.backend.model.User;
import com.bence.projector.server.backend.repository.UserRepository;
import com.bence.projector.server.backend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class UserServiceImpl extends BaseServiceImpl<User> implements UserService {
    @Autowired
    private UserRepository userRepository;
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    public User findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    public User registerUser(final User user) {
        if (user != null) {
            if (user.getRole() == null) {
                user.setRole(Role.ROLE_USER);
            }
            user.setPassword(passwordEncoder.encode(user.getPassword()));
            return this.save(user);
        }
        return null;
    }

    private boolean containsInList(Language language, List<Language> languages) {
        if (language == null || languages == null) {
            return false;
        }
        for (Language aLanguage : languages) {
            if (aLanguage.getId().equals(language.getId())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public List<User> findAllReviewersByLanguage(Language language) {
        List<User> reviewers = findAllReviewers();
        List<User> reviewersByLanguage = new ArrayList<>();
        for (User reviewer : reviewers) {
            if (containsInList(language, reviewer.getReviewLanguages())) {
                reviewersByLanguage.add(reviewer);
            }
        }
        addAdminsToUsers(reviewersByLanguage);
        return reviewersByLanguage;
    }

    @Override
    public List<User> findAllReviewers() {
        List<User> users = findAll();
        List<User> reviewers = new ArrayList<>();
        for (User user : users) {
            if (user.getRole().equals(Role.ROLE_REVIEWER)) {
                reviewers.add(user);
            }
        }
        addAdminsToUsers(reviewers);
        return reviewers;
    }

    private void addAdminsToUsers(List<User> users) {
        List<User> admins = findAllAdmins();
        for (User admin : admins) {
            boolean was = false;
            for (User user : users) {
                if (user.getEmail().equals(admin.getEmail())) {
                    was = true;
                    break;
                }
            }
            if (!was) {
                users.add(admin);
            }
        }
    }

    @Override
    public List<User> findAllAdmins() {
        List<User> users = findAll();
        List<User> admins = new ArrayList<>();
        for (User user : users) {
            if (user.getRole().equals(Role.ROLE_ADMIN)) {
                admins.add(user);
            }
        }
        return admins;
    }

    @Override
    public User findOneByUuid(String uuid) {
        return userRepository.findOneByUuid(uuid);
    }
}
