package com.bence.projector.server.backend.repository;

import com.bence.projector.server.backend.model.User;
import org.springframework.data.repository.CrudRepository;

public interface UserRepository extends CrudRepository<User, Long> {
    User findByEmail(String email);

    User findOneByUuid(String uuid);
}
