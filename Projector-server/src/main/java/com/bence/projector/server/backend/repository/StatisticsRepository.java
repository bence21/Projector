package com.bence.projector.server.backend.repository;

import com.bence.projector.server.backend.model.Statistics;
import org.springframework.data.repository.CrudRepository;

public interface StatisticsRepository extends CrudRepository<Statistics, Long> {
}
