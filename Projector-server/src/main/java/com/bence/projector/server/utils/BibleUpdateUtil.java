package com.bence.projector.server.utils;

import com.bence.projector.server.backend.model.Bible;
import com.bence.projector.server.backend.model.Book;
import com.bence.projector.server.backend.service.BibleService;

import java.util.Date;
import java.util.List;

public class BibleUpdateUtil {

    public static void updateWrongBookNames(BibleService bibleService) {
        List<Bible> bibles = bibleService.findAll();
        for (Bible bible : bibles) {
            if (fixForBible(bible)) {
                bible.setModifiedDate(new Date());
                bibleService.saveToBooks(bible);
            }
        }
    }

    private static boolean fixForBible(Bible bible) {
        for (Book book : bible.getBooks()) {
            if (book.getTitle().trim().equals("Énekek Énekeke")) {
                book.setTitle("Énekek Éneke");
                return true;
            }
        }
        return false;
    }
}
