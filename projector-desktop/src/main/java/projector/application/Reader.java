package projector.application;

import projector.model.BibleVerse;
import projector.model.Book;
import projector.model.Chapter;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Reader {

    private static final int numberOfBooks = 66;
    private static List<Book> books;
    private static boolean booksRead = false;

    public static void setBooksRead(boolean booksRead) {
        Reader.booksRead = booksRead;
    }

    public static List<Book> getBooks(String filePath) {
        if (!booksRead) {
            readBooksB(filePath);
        }
        return books;
    }

    public static void readBooksB(String filePath) {
        if (!booksRead) {
            booksRead = true;
            FileInputStream fstream;
            try {
                fstream = new FileInputStream(filePath);
                BufferedReader br = new BufferedReader(new InputStreamReader(fstream, "UTF-8"));
                br.mark(4);
                if ('\ufeff' != br.read()) {
                    br.reset(); // not the BOM marker
                }
                String strLine;
                String[] a;
                int b;
                books = new ArrayList<>(numberOfBooks);
                Chapter[][] tmpChapters = new Chapter[numberOfBooks][];

                for (int i = 0; i < numberOfBooks; ++i) {
                    strLine = br.readLine();
                    if (strLine == null) {
                        break;
                    }
                    a = strLine.split(":");
                    b = Integer.parseInt(a[1].substring(1));
                    tmpChapters[i] = new Chapter[b];
                    String title = a[0];
                    Book book = new Book();
                    book.setTitle(title);
                    books.add(book);
                    strLine = br.readLine();
                    if (strLine == null) {
                        break;
                    }
                    String[] split = strLine.split(" ");
                    for (int k = 0; k < split.length; k++) {
                        int j = Integer.parseInt(split[k]);
                        tmpChapters[i][k] = new Chapter();
                        tmpChapters[i][k].setLength(j);
                    }
                }

                for (int book = 0; book < numberOfBooks; ++book) {
                    for (int part = 0; part < tmpChapters[book].length; ++part) {
                        List<BibleVerse> tmpVerses = new ArrayList<>(tmpChapters[book][part].getLength());
                        for (int verse = 0; verse < tmpChapters[book][part].getLength(); ++verse) {
                            br.readLine();
                            String s = br.readLine();
                            BibleVerse bibleVerse = new BibleVerse();
                            bibleVerse.setText(s);
                            tmpVerses.add(bibleVerse);
                        }
                        Chapter chapter = tmpChapters[book][part];
                        chapter.setVerses(tmpVerses);
                    }
                }
                for (int i = 0; i < numberOfBooks; ++i) {
                    books.get(i).setChapters(Arrays.asList(tmpChapters[i]));
                }
                br.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
