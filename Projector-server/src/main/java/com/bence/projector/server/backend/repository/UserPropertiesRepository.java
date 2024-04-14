package com.bence.projector.server.backend.repository;

import com.bence.projector.server.backend.model.UserProperties;
import org.springframework.data.repository.CrudRepository;

public interface UserPropertiesRepository extends CrudRepository<UserProperties, Long> {
}
