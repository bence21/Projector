package com.bence.projector.server.backend.repository;

import com.bence.projector.server.backend.model.User;
import org.springframework.data.repository.CrudRepository;

import java.util.Date;
import java.util.List;

public interface UserRepository extends CrudRepository<User, Long> {
    User findByEmail(String email);

    User findOneByUuid(String uuid);

    List<User> findAllByCreatedDateAfter(Date date);

    List<User> findAllByDeletedFalseOrDeletedIsNull();
}
