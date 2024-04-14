package projector.api.assembler;

import com.bence.projector.common.dto.BibleDTO;
import com.bence.projector.common.dto.BibleVerseDTO;
import com.bence.projector.common.dto.BookDTO;
import com.bence.projector.common.dto.ChapterDTO;
import projector.model.Bible;
import projector.model.BibleVerse;
import projector.model.Book;
import projector.model.Chapter;
import projector.model.VerseIndex;
import projector.service.ServiceManager;

import java.util.ArrayList;
import java.util.List;

public class BibleAssembler implements GeneralAssembler<Bible, BibleDTO> {

    private static BibleAssembler instance;

    private BibleAssembler() {
    }

    public static BibleAssembler getInstance() {
        if (instance == null) {
            instance = new BibleAssembler();
        }
        return instance;
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
        for (Book book : bible.getBooks()) {
            BookDTO bookDTO = new BookDTO();
            ArrayList<ChapterDTO> chapterDTOS = new ArrayList<>();
            for (Chapter chapter : book.getChapters()) {
                ChapterDTO chapterDTO = new ChapterDTO();
                ArrayList<BibleVerseDTO> verseDTOS = new ArrayList<>();
                for (BibleVerse bibleVerse : chapter.getVerses()) {
                    BibleVerseDTO verseDTO = new BibleVerseDTO();
                    verseDTO.setText(bibleVerse.getText());
                    List<VerseIndex> indices = bibleVerse.getVerseIndices();
                    if (indices != null) {
                        List<Long> verseIndices = new ArrayList<>(indices.size());
                        for (VerseIndex verseIndex : indices) {
                            verseIndices.add(verseIndex.getIndexNumber());
                        }
                        verseDTO.setVerseIndices(verseIndices);
                    }
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
        bibleDTO.setBooks(bookDTOS);
        bibleDTO.setName(bible.getName());
        bibleDTO.setShortName(bible.getShortName());
        bibleDTO.setLanguageUuid(bible.getLanguage().getUuid());
        return bibleDTO;
    }

    @Override
    public Bible createModel(BibleDTO bibleDTO) {
        final Bible bible = new Bible();
        bible.setUuid(bibleDTO.getUuid());
        return updateModel(bible, bibleDTO);
    }

    private void createBibleBooks(Bible bible, BibleDTO bibleDTO) {
        ArrayList<Book> books = new ArrayList<>();
        for (BookDTO bookDTO : bibleDTO.getBooks()) {
            Book book = new Book();
            ArrayList<Chapter> chapterS = new ArrayList<>();
            short chapterNr = 1;
            for (ChapterDTO chapterDTO : bookDTO.getChapters()) {
                Chapter chapter = new Chapter();
                chapter.setNumber(chapterNr++);
                ArrayList<BibleVerse> verses = new ArrayList<>();
                short verseNr = 1;
                for (BibleVerseDTO bibleVerse : chapterDTO.getVerses()) {
                    BibleVerse verse = new BibleVerse();
                    verse.setNumber(verseNr++);
                    verse.setText(bibleVerse.getText());
                    List<Long> verseIndices = bibleVerse.getVerseIndices();
                    List<VerseIndex> indices = new ArrayList<>(verseIndices.size());
                    for (Long aLong : verseIndices) {
                        VerseIndex verseIndex = new VerseIndex();
                        verseIndex.setIndexNumber(aLong);
                        indices.add(verseIndex);
                    }
                    verse.setVerseIndices(indices);
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
    }

    private void updateBibleFields(Bible bible, BibleDTO bibleDTO) {
        bible.setCreatedDate(bibleDTO.getCreatedDate());
        bible.setModifiedDate(bibleDTO.getModifiedDate());
        bible.setName(bibleDTO.getName());
        bible.setShortName(bibleDTO.getShortName());
        if (bibleDTO.getLanguageUuid() != null) {
            bible.setLanguage(ServiceManager.getLanguageService().findByUuid(bibleDTO.getLanguageUuid()));
        }
    }

    @Override
    public Bible updateModel(Bible bible, BibleDTO bibleDTO) {
        updateBibleFields(bible, bibleDTO);
        createBibleBooks(bible, bibleDTO);
        return bible;
    }

    @Override
    public List<Bible> createModelList(List<BibleDTO> bibleDTOS) {
        List<Bible> bibles = new ArrayList<>();
        for (BibleDTO bibleDTO : bibleDTOS) {
            bibles.add(createModel(bibleDTO));
        }
        return bibles;
    }

    @Override
    public List<BibleDTO> createDtoList(List<Bible> bibles) {
        return null;
    }
}
