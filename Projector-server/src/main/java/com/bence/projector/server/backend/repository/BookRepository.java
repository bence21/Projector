package com.bence.projector.server.backend.repository;

import com.bence.projector.server.backend.model.Book;
import org.springframework.data.repository.CrudRepository;

public interface BookRepository extends CrudRepository<Book, Long> {
}
