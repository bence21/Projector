package com.bence.projector.server.backend.service;

import com.bence.projector.server.backend.model.Book;

import java.util.List;

public interface BookService extends BaseService<Book> {
    void deleteAll(List<Book> books);
}
