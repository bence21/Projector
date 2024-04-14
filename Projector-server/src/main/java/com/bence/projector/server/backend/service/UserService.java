package com.bence.projector.server.backend.service;

import com.bence.projector.server.backend.model.Language;
import com.bence.projector.server.backend.model.User;

import java.util.List;

public interface UserService extends BaseService<User> {
    User findByEmail(String username);

    User registerUser(User user);

    List<User> findAllReviewers();

    List<User> findAllReviewersByLanguage(Language language);

    List<User> findAllAdmins();

    User findOneByUuid(String uuid);
}
