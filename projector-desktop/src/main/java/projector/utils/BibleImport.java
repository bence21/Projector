package projector.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import projector.MainDesktop;
import projector.api.BibleApiBean;
import projector.application.Settings;
import projector.controller.IndicesForBibleController;
import projector.model.Bible;
import projector.model.BibleVerse;
import projector.model.Book;
import projector.model.Chapter;
import projector.model.Language;
import projector.model.VerseIndex;
import projector.model.assembler.BibleAssembler;
import projector.model.sqlite.Books;
import projector.model.sqlite.Verses;
import projector.repository.sqlite.DatabaseHelper;
import projector.service.BibleService;
import projector.service.ServiceManager;
import projector.service.sqlite.BooksService;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static projector.utils.SceneUtils.addStylesheetToSceneBySettings;
import static projector.utils.SceneUtils.getAStage;

@SuppressWarnings("unused")
public class BibleImport {

    private static final Settings settings = Settings.getInstance();
    private static final Logger LOG = LoggerFactory.getLogger(BibleImport.class);

    public static void main(String[] args) {
        //bibleImportFromJson();
        //bibleImport();
    }

    public static Bible bibleImportFromSQLite(String databasePath) {
        DatabaseHelper databaseHelper = DatabaseHelper.getInstance();
        databaseHelper.connect(databasePath);
        BooksService booksService = ServiceManager.getBooksService();
        List<Books> books = booksService.findAll();
        List<Verses> verses = ServiceManager.getVersesService().findAll();
        String bibleName = ServiceManager.getInfoService().getDescription();
        String bibleShortName = getBibleShortNameFromPath(databasePath);
        Bible bible = BibleAssembler.getInstance().createBible(books, verses, bibleName, bibleShortName);
        databaseHelper.disconnect();
        return bible;
    }

    private static String getBibleShortNameFromPath(String path) {
        try {
            String[] split = path.split("[/\\\\]");
            String s = split[split.length - 1];
            String fileName = s.split(".SQLite3")[0];
            if (!fileName.isEmpty()) {
                return fileName;
            }
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
        return "Imported";
    }

    private static void bibleImportFromXml(@SuppressWarnings("SameParameterValue") String fileName) {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        try {
            dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(new File(fileName));
            doc.getDocumentElement().normalize();
            System.out.println("Root Element :" + doc.getDocumentElement().getNodeName());
            System.out.println("------");
            NodeList list = doc.getElementsByTagName("XMLBIBLE");
            Bible bible = new Bible();
            bible.setName("Indonesia Terjemahan Baru");
            bible.setShortName("ITB");
            List<Language> languages = ServiceManager.getLanguageService().findAll();
            bible.setLanguage(languages.get(9));
            for (int temp = 0; temp < list.getLength(); temp++) {
                Node node = list.item(temp);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element element = (Element) node;
                    NodeList bibleBooks = element.getElementsByTagName("BIBLEBOOK");
                    List<Book> books = readBibleBooks(bibleBooks);
                    bible.setBooks(books);
                }
            }
            bible.setCreatedDate(new Date());
            createIndices(bible);
            ServiceManager.getBibleService().create(bible);
        } catch (ParserConfigurationException | SAXException | IOException e) {
            e.printStackTrace();
        }
    }

    private static List<Book> readBibleBooks(NodeList bibleBooks) {
        ArrayList<Book> books = new ArrayList<>();
        for (int i = 0; i < bibleBooks.getLength(); ++i) {
            Node node = bibleBooks.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Book book = new Book();
                Element element = (Element) node;
                String bookName = element.getAttribute("bname");
                book.setTitle(bookName);
                System.out.println(element.getAttribute("bnumber") + " " + bookName);
                NodeList chaptersList = element.getElementsByTagName("CHAPTER");
                List<Chapter> chapters = readChapters(chaptersList);
                book.setChapters(chapters);
                books.add(book);
            }
        }
        return books;
    }

    private static List<Chapter> readChapters(NodeList chaptersList) {
        ArrayList<Chapter> chapters = new ArrayList<>();
        for (int i = 0; i < chaptersList.getLength(); ++i) {
            Node node = chaptersList.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Chapter chapter = new Chapter();
                Element element = (Element) node;
                NodeList versesList = element.getElementsByTagName("VERS");
                List<BibleVerse> verses = readVerses(versesList);
                chapter.setNumber((short) (i + 1));
                chapter.setVerses(verses);
                chapters.add(chapter);
            }
        }
        return chapters;
    }

    private static List<BibleVerse> readVerses(NodeList verses) {
        ArrayList<BibleVerse> bibleVerses = new ArrayList<>();
        for (int i = 0; i < verses.getLength(); ++i) {
            Node node = verses.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                BibleVerse bibleVerse = new BibleVerse();
                Element element = (Element) node;
                String vNumber = element.getAttribute("vnumber");
                bibleVerse.setNumber((short) (i + 1));
                if (!((i + 1) + "").equals(vNumber)) {
                    System.out.println("!!!" + vNumber);
                }
                String verse = element.getTextContent();
                bibleVerse.setText(verse);
                bibleVerses.add(bibleVerse);
            }
        }
        return bibleVerses;
    }

    /* Should be called from the main application
     */
    public static void bibleImporting(Class<?> aClass) {
        List<Bible> bibles = ServiceManager.getBibleService().findAll();
        Bible bible = bibles.get(4);
//        updateBibleWithDelete(bible);

        setIndicesForBible(bible, aClass);
        //bibleImportFromJson();
    }

    private static void updateBibleWithDelete(Bible bible) {
        BibleService bibleService = ServiceManager.getBibleService();
        try {
            bibleService.delete(bible);
        } catch (Exception e) {
            e.printStackTrace();
        }
        bibleService.create(bible);
    }

    public static void bibleImportFromJson(Class<?> aClass) {
        FileInputStream inputStream;
        try {
            inputStream = new FileInputStream("booktitle.txt");
            BufferedReader br = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
            String line = br.readLine();
            List<String> titles = new ArrayList<>();
            while (line != null) {
                titles.add(line.trim());
                line = br.readLine();
            }
            br.close();
            inputStream = new FileInputStream("bible.txt");
            br = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
            List<String> verses = new ArrayList<>();
            line = br.readLine();
            List<Book> books = new ArrayList<>();
            int previousBookNr = 0;
            int previousPartNr = 0;
            int bookNr;
            int partNr;
            Book book = null;
            Chapter chapter = null;
            while (line != null) {
                String[] split = line.split("\\|\\|");
                bookNr = Integer.parseInt(split[0].replaceAll("[^0-9]", ""));
                partNr = Integer.parseInt(split[1]);
                int verseNr = Integer.parseInt(split[2]);
                boolean newBook = bookNr != previousBookNr;
                if (newBook) {
                    previousBookNr = bookNr;
                    book = new Book();
                    book.setTitle(titles.get(bookNr - 1));
                    books.add(book);
                }
                if (newBook || partNr != previousPartNr) {
                    previousPartNr = partNr;
                    chapter = new Chapter();
                    chapter.setBook(book);
                    chapter.setNumber((short) partNr);
                    if (book != null) {
                        List<Chapter> chapters = book.getChapters();
                        if (chapters == null) {
                            chapters = new ArrayList<>();
                            book.setChapters(chapters);
                        }
                        chapters.add(chapter);
                    }
                }
                if (chapter != null) {
                    List<BibleVerse> bibleVerses = chapter.getVerses();
                    if (bibleVerses == null) {
                        bibleVerses = new ArrayList<>();
                        chapter.setVerses(bibleVerses);
                    }
                    BibleVerse verse = new BibleVerse();
                    bibleVerses.add(verse);
                    verse.setNumber((short) verseNr);
                    verse.setText(split[3].trim());
                    verse.setChapter(chapter);
                }
                line = br.readLine();
            }
            System.out.println(books.size());

            Bible bible = new Bible();
            bible.setBooks(books);
            List<Language> languages = ServiceManager.getLanguageService().findAll();
            bible.setLanguage(languages.get(4));
            bible.setCreatedDate(new Date());
            bible.setModifiedDate(bible.getCreatedDate());
            bible.setName("TLAB Ang Biblia");
            bible.setShortName("TLAB");
            createIndices(bible);
            //ServiceManager.getBibleService().create(bible);
            setIndicesForBible(bible, aClass);

            //verseImport(bible);
        } catch (IOException ignored) {
        }
    }

    private static char getCharFromX(int x) {
        return switch (x) {
            case 81 -> 'N';
            case 127 -> 'g';
            case 129 -> 'a';
            case 163 -> 'n';
            case 7 -> ' ';
            default -> '?';
        };
    }

    public static void bibleImport() {
        FileInputStream inputStream;
        try {
            inputStream = new FileInputStream("books.json");
            BufferedReader br = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
            StringBuilder s = new StringBuilder();
            String readLine = br.readLine();
            while (readLine != null) {
                s.append(readLine);
                readLine = br.readLine();
            }
            Gson gson = new GsonBuilder().serializeNulls().create();
            ArrayList<Book> bookArrayList;
            Type listType = new TypeToken<ArrayList<Book>>() {
            }.getType();
            bookArrayList = gson.fromJson(s.toString(), listType);
            System.out.println(bookArrayList.size());

            Bible bible = new Bible();
            bible.setBooks(bookArrayList);
            List<Language> languages = ServiceManager.getLanguageService().findAll();
            bible.setLanguage(languages.get(2));
            bible.setCreatedDate(new Date());
            bible.setModifiedDate(bible.getCreatedDate());
            bible.setName("New International Version");
            bible.setShortName("NIV");
            createIndices(bible);

            //verseImport(bible); // for not complete json
            ServiceManager.getBibleService().create(bible);
            //createIndices(bible);
            //setIndicesForBible(bible);
        } catch (IOException ignored) {
        }
    }

    @SuppressWarnings("ConstantConditions")
    private static void verseImport(Bible bible) {
        FileInputStream inputStream;
        try {
            inputStream = new FileInputStream("verses.json");
            BufferedReader br = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
            StringBuilder s = new StringBuilder();
            String readLine = br.readLine();
            while (readLine != null) {
                s.append(readLine);
                readLine = br.readLine();
            }
            Gson gson = new GsonBuilder().serializeNulls().create();
            ArrayList<Verse> verseArrayList;
            Type listType = new TypeToken<ArrayList<Verse>>() {
            }.getType();
            verseArrayList = gson.fromJson(s.toString(), listType);
            System.out.println(verseArrayList.size());
            String previousBookNumber = "";
            String previousChapter = "";
            List<Book> books = bible.getBooks();
            int bookI = -1;
            Book book = null;
            Chapter chapter = null;
            for (Verse verse : verseArrayList) {
                if (!verse.getBookNumber().equals(previousBookNumber)) {
                    ++bookI;
                    book = books.get(bookI);
                    previousChapter = "";
                }
                if (!verse.getChapter().equals(previousChapter)) {
                    chapter = new Chapter();
                    chapter.setNumber((short) Integer.parseInt(verse.getChapter()));
                    chapter.setBook(book);
                    List<Chapter> chapters = book.getChapters();
                    if (chapters == null) {
                        chapters = new ArrayList<>();
                        book.setChapters(chapters);
                    }
                    chapters.add(chapter);
                }
                previousBookNumber = verse.getBookNumber();
                previousChapter = verse.getChapter();
                BibleVerse bibleVerse = new BibleVerse();
                bibleVerse.setChapter(chapter);
                bibleVerse.setText(verse.getText());
                bibleVerse.setNumber((short) Integer.parseInt(verse.getVerse()));
                List<BibleVerse> verses = chapter.getVerses();
                if (verses == null) {
                    verses = new ArrayList<>(50);
                    chapter.setVerses(verses);
                }
                verses.add(bibleVerse);
            }
            System.out.println("Done");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unused")
    public static void uploadBible(Bible bible) {
        BibleApiBean bibleApiBean = new BibleApiBean();
        Bible uploadedBible = bibleApiBean.uploadBible(bible);
        System.out.println("accomplished");
    }

    @SuppressWarnings("unused")
    private static void createIndices(Bible bible) {
        int k = 1;
        for (Book book : bible.getBooks()) {
            book.setBible(bible);
            short chapterNr = 1;
            for (Chapter chapter : book.getChapters()) {
                chapter.setNumber(chapterNr++);
                chapter.setBook(book);
                short verseNr = 1;
                for (BibleVerse bibleVerse : chapter.getVerses()) {
                    bibleVerse.setNumber(verseNr++);
                    ArrayList<VerseIndex> verseIndices = new ArrayList<>();
                    VerseIndex verseIndex = new VerseIndex();
                    verseIndex.setIndexNumber(k++ * 1000L);
                    verseIndices.add(verseIndex);
                    bibleVerse.setVerseIndices(verseIndices);
                    bibleVerse.setChapter(chapter);
                }
            }
        }
    }

    public static void setIndicesForBible(Bible otherBible, Class<?> aClass) {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(MainDesktop.class.getResource("/view/IndicesForBibleView.fxml"));
            loader.setResources(settings.getResourceBundle());
            Pane root = loader.load();
            IndicesForBibleController controller = loader.getController();
            List<Bible> bibles = ServiceManager.getBibleService().findAll();
            controller.setLeftBible(bibles.get(0));
            controller.setOtherBible(otherBible);
            Scene scene = new Scene(root);
            addStylesheetToSceneBySettings(scene, aClass);
            Stage stage = getAStage(aClass);
            stage.setScene(scene);
            stage.setTitle("Indices");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static class Verse {
        private String bookNumber;
        private String chapter;
        private String text;
        private String verse;

        String getBookNumber() {
            return bookNumber;
        }

        public void setBookNumber(String bookNumber) {
            this.bookNumber = bookNumber;
        }

        public String getChapter() {
            return chapter;
        }

        public void setChapter(String chapter) {
            this.chapter = chapter;
        }

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }

        public String getVerse() {
            return verse;
        }

        public void setVerse(String verse) {
            this.verse = verse;
        }
    }
}