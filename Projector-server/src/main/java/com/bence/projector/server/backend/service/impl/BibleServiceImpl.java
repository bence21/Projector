package com.bence.projector.server.backend.service.impl;

import com.bence.projector.server.backend.model.Bible;
import com.bence.projector.server.backend.model.BibleVerse;
import com.bence.projector.server.backend.model.Book;
import com.bence.projector.server.backend.model.Chapter;
import com.bence.projector.server.backend.model.VerseIndex;
import com.bence.projector.server.backend.repository.BibleRepository;
import com.bence.projector.server.backend.service.BibleService;
import com.bence.projector.server.backend.service.BibleVerseService;
import com.bence.projector.server.backend.service.BookService;
import com.bence.projector.server.backend.service.VerseIndexService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedList;
import java.util.List;

@Service
public class BibleServiceImpl extends BaseServiceImpl<Bible> implements BibleService {

    @Autowired
    private BibleRepository bibleRepository;
    @Autowired
    private BookService bookService;
    @Autowired
    private BibleVerseService bibleVerseService;
    @Autowired
    private VerseIndexService verseIndexService;

    @Override
    public Bible findOneByUuid(String uuid) {
        return bibleRepository.findOneByUuid(uuid);
    }

    @Override
    public void saveToBooks(Bible bible) {
        super.save(bible);
        bookService.save(bible.getBooks());
    }

    @Override
    public Bible save(Bible bible) {
        return saveABibleTransactional(bible);
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    private Bible saveABibleTransactional(Bible bible) {
        Bible savedBible = super.save(bible);
        bookService.save(bible.getBooks());
        List<BibleVerse> verses = new LinkedList<>();
        for (Book book : bible.getBooks()) {
            for (Chapter chapter : book.getChapters()) {
                verses.addAll(chapter.getVerses());
            }
        }
        bibleVerseService.save(verses);
        List<VerseIndex> verseIndices = new LinkedList<>();
        for (BibleVerse bibleVerse : verses) {
            verseIndices.addAll(bibleVerse.getVerseIndices());
        }
        setBibleToVerseIndices(bible, verseIndices);
        verseIndexService.save(verseIndices);
        return savedBible;
    }

    private void setBibleToVerseIndices(Bible bible, List<VerseIndex> verseIndices) {
        for (VerseIndex verseIndex : verseIndices) {
            verseIndex.setBible(bible);
        }
    }

    @Override
    public Iterable<Bible> save(List<Bible> models) {
        for (Bible bible : models) {
            save(bible);
        }
        return models;
    }

    @SuppressWarnings("unused")
    public void deleteByUuid(String id) {
        Bible bible = findOneByUuid(id);
        if (bible == null) {
            return;
        }
        bookService.deleteAll(bible.getBooks());
        super.delete(bible.getId());
    }

}
