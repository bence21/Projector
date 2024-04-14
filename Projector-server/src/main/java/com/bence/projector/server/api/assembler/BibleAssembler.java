package com.bence.projector.server.api.assembler;

import com.bence.projector.common.dto.BibleDTO;
import com.bence.projector.common.dto.BibleVerseDTO;
import com.bence.projector.common.dto.BookDTO;
import com.bence.projector.common.dto.ChapterDTO;
import com.bence.projector.server.backend.model.Bible;
import com.bence.projector.server.backend.model.BibleVerse;
import com.bence.projector.server.backend.model.Book;
import com.bence.projector.server.backend.model.Chapter;
import com.bence.projector.server.backend.model.VerseIndex;
import com.bence.projector.server.backend.service.LanguageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Component
public class BibleAssembler implements GeneralAssembler<Bible, BibleDTO> {

    private final LanguageService languageService;

    @Autowired
    public BibleAssembler(LanguageService languageService) {
        this.languageService = languageService;
    }

    @Override
    public BibleDTO createDto(Bible bible) {
        if (bible == null) {
            return null;
        }
        BibleDTO bibleDTO = new BibleDTO();
        bibleDTO.setUuid(bible.getUuid());
        bibleDTO.setCreatedDate(bible.getCreatedDate());
        bibleDTO.setModifiedDate(bible.getModifiedDate());
        ArrayList<BookDTO> bookDTOS = new ArrayList<>();
        if (bible.getBooks() != null) {
            for (Book book : bible.getBooks()) {
                BookDTO bookDTO = new BookDTO();
                ArrayList<ChapterDTO> chapterDTOS = new ArrayList<>();
                for (Chapter chapter : book.getChapters()) {
                    ChapterDTO chapterDTO = new ChapterDTO();
                    ArrayList<BibleVerseDTO> verseDTOS = new ArrayList<>();
                    for (BibleVerse bibleVerse : chapter.getVerses()) {
                        BibleVerseDTO verseDTO = new BibleVerseDTO();
                        verseDTO.setText(bibleVerse.getText());
                        verseDTO.setVerseIndices(getVerseIndicesAsLong(bibleVerse.getVerseIndices()));
                        verseDTOS.add(verseDTO);
                    }
                    chapterDTO.setVerses(verseDTOS);
                    chapterDTOS.add(chapterDTO);
                }
                bookDTO.setChapters(chapterDTOS);
                bookDTO.setShortName(book.getShortName());
                bookDTO.setTitle(book.getTitle());
                bookDTOS.add(bookDTO);
            }
        }
        bibleDTO.setBooks(bookDTOS);
        bibleDTO.setName(bible.getName());
        bibleDTO.setShortName(bible.getShortName());
        if (bible.getLanguage() != null) {
            bibleDTO.setLanguageUuid(bible.getLanguage().getUuid());
        }
        return bibleDTO;
    }

    private List<Long> getVerseIndicesAsLong(List<VerseIndex> verseIndices) {
        if (verseIndices == null) {
            return null;
        }
        ArrayList<Long> longs = new ArrayList<>();
        for (VerseIndex verseIndex : verseIndices) {
            longs.add(verseIndex.getIndexNumber());
        }
        return longs;
    }

    @Override
    public Bible createModel(BibleDTO bibleDTO) {
        final Bible bible = new Bible();
        bible.setCreatedDate(bibleDTO.getCreatedDate());
        if (bible.getCreatedDate() == null) {
            bible.setCreatedDate(new Date());
        }
        return updateModel(bible, bibleDTO);
    }

    @Override
    public Bible updateModel(Bible bible, BibleDTO bibleDTO) {
        bible.setModifiedDate(new Date());
        ArrayList<Book> books = new ArrayList<>();
        for (BookDTO bookDTO : bibleDTO.getBooks()) {
            Book book = new Book();
            ArrayList<Chapter> chapterS = new ArrayList<>();
            for (ChapterDTO chapterDTO : bookDTO.getChapters()) {
                Chapter chapter = new Chapter();
                ArrayList<BibleVerse> verses = new ArrayList<>();
                for (BibleVerseDTO bibleVerse : chapterDTO.getVerses()) {
                    BibleVerse verse = new BibleVerse();
                    verse.setText(bibleVerse.getText());
                    verse.setVerseIndices(getVerseIndicesAsVerseIndex(bibleVerse.getVerseIndices()));
                    verses.add(verse);
                }
                chapter.setVerses(verses);
                chapterS.add(chapter);
            }
            book.setChapters(chapterS);
            book.setShortName(bookDTO.getShortName());
            book.setTitle(bookDTO.getTitle());
            books.add(book);
        }
        bible.setBooks(books);
        bible.setName(bibleDTO.getName());
        bible.setShortName(bibleDTO.getShortName());
        if (bibleDTO.getLanguageUuid() != null) {
            bible.setLanguage(languageService.findOneByUuid(bibleDTO.getLanguageUuid()));
        }
        return bible;
    }

    private List<VerseIndex> getVerseIndicesAsVerseIndex(List<Long> verseIndices) {
        ArrayList<VerseIndex> indices = new ArrayList<>();
        for (Long index: verseIndices) {
            VerseIndex verseIndex = new VerseIndex();
            verseIndex.setIndexNumber(index);
            indices.add(verseIndex);
        }
        return indices;
    }
}
