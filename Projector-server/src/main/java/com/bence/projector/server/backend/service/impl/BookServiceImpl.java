package com.bence.projector.server.backend.service.impl;

import com.bence.projector.server.backend.model.Book;
import com.bence.projector.server.backend.service.BookService;
import com.bence.projector.server.backend.service.ChapterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BookServiceImpl extends BaseServiceImpl<Book> implements BookService {

    @Autowired
    private ChapterService chapterService;

    @Override
    public Book save(Book book) {
        Book savedBook = super.save(book);
        chapterService.save(book.getChapters());
        return savedBook;
    }

    @Override
    public Iterable<Book> save(List<Book> books) {
        for (Book book : books) {
            this.save(book);
        }
        return books;
    }
}
