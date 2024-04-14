package projector.controller;

import javafx.collections.ObservableList;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;
import javafx.scene.control.MultipleSelectionModel;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TextField;
import projector.api.BibleApiBean;
import projector.model.Bible;
import projector.model.BibleVerse;
import projector.model.Book;
import projector.model.Chapter;
import projector.model.Language;
import projector.model.VerseIndex;
import projector.service.BibleService;
import projector.service.ServiceManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class IndicesForBibleController {
    public TextField textField;
    public ListView<BibleVerse> leftListView;
    public ListView<BibleVerse> otherListView;
    public ListView<Book> bookListView;
    public ListView<Chapter> chapterListView;
    public ComboBox<Bible> bibleComboBox;
    private Bible otherBible;
    private HashMap<Long, List<BibleVerse>> verseHashMap;
    private Bible bible;
    private MultipleSelectionModel<BibleVerse> leftListViewSelectionModel;
    private MultipleSelectionModel<BibleVerse> otherListViewSelectionModel;
    private Chapter chapter;

    public void initialize() {
        leftListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        otherListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        leftListViewSelectionModel = leftListView.getSelectionModel();
        otherListViewSelectionModel = otherListView.getSelectionModel();
        List<Bible> bibles = ServiceManager.getBibleService().findAll();
        bibleComboBox.getItems().addAll(bibles);
        bibleComboBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            setLeftBible(newValue);
            reloadListViews();
        });
    }

    public void setLeftBible(Bible bible) {
        int bookSelectedIndex = bookListView.getSelectionModel().getSelectedIndex();
        int chapterSelectedIndex = chapterListView.getSelectionModel().getSelectedIndex();
        this.bible = bible;
        ObservableList<Book> bookListViewItems = bookListView.getItems();
        bookListViewItems.clear();
        bookListViewItems.addAll(bible.getBooks());
        MultipleSelectionModel<Book> bookSelectionModel = bookListView.getSelectionModel();
        MultipleSelectionModel<Chapter> chapterSelectionModel = chapterListView.getSelectionModel();
        bookSelectionModel.selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                ObservableList<Chapter> items = chapterListView.getItems();
                items.clear();
                items.addAll(newValue.getChapters());
            }
        });
        if (bookSelectedIndex >= 0) {
            bookSelectionModel.select(bookSelectedIndex);
        } else {
            bookSelectionModel.selectFirst();
        }
        if (chapterSelectedIndex >= 0) {
            chapterSelectionModel.select(chapterSelectedIndex);
        } else {
            chapterSelectionModel.selectedItemProperty().addListener((observable, oldValue, newValue) -> loadListViews(newValue));
        }
    }

    private void reloadListViews() {
        loadListViews(chapter);
    }

    private void loadListViews(Chapter chapter) {
        if (chapter == null) {
            return;
        }
        this.chapter = chapter;
        ObservableList<BibleVerse> items = leftListView.getItems();
        items.clear();
        items.addAll(chapter.getVerses());
        if (otherBible != null) {
            ObservableList<BibleVerse> items1 = otherListView.getItems();
            items1.clear();
            for (BibleVerse verse : chapter.getVerses()) {
                try {
                    List<VerseIndex> verseIndices = verse.getVerseIndices();
                    if (verseIndices != null && verseIndices.size() > 0) {
                        List<BibleVerse> bibleVerses = new ArrayList<>();
                        for (VerseIndex verseIndex : verseIndices) {
                            List<BibleVerse> c = verseHashMap.get(verseIndex.getIndexNumber());
                            if (c != null) {
                                bibleVerses.addAll(c);
                            }
                        }
                        if (bibleVerses.size() == 0) {
                            items1.add(new BibleVerse());
                        } else if (bibleVerses.size() == 1) {
                            items1.add(bibleVerses.get(0));
                        } else {
                            BibleVerse bibleVerse = bibleVerses.get(0);
                            BibleVerse verse1 = new BibleVerse();
                            verse1.setVerseIndices(bibleVerse.getVerseIndices());
                            verse1.setNumber(bibleVerse.getNumber());
                            verse1.setChapter(bibleVerse.getChapter());
                            StringBuilder text = new StringBuilder(bibleVerse.getText());
                            for (int i = 1; i < bibleVerses.size(); ++i) {
                                text.append("\n").append(bibleVerses.get(i).getText());
                            }
                            verse1.setText(text.toString());
                            items1.add(verse1);
                        }
                    } else {
                        System.out.println();
                    }
                } catch (IndexOutOfBoundsException | NullPointerException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void setOtherBible(Bible otherBible) {
        this.otherBible = otherBible;
        verseHashMap = new HashMap<>();
        fillVerseMap();
        chapterListView.getSelectionModel().selectFirst();
    }

    private void fillVerseMap() {
        verseHashMap.clear();
        for (Book book : otherBible.getBooks()) {
            for (Chapter chapter : book.getChapters()) {
                for (BibleVerse verse : chapter.getVerses()) {
                    if (verse.getText().isEmpty()) {
                        System.out.println(verse.getId());
                    }
                    for (VerseIndex index : verse.getVerseIndices()) {
                        if (verseHashMap.containsKey(index.getIndexNumber())) {
                            List<BibleVerse> bibleVerses = verseHashMap.get(index.getIndexNumber());
//                            BibleVerse bibleVerse = bibleVerses.get(0);
//                            bibleVerse.setText(bibleVerse.getText().replaceAll("\n" + verse.getText(), ""));
                            bibleVerses.add(verse);
                        } else {
                            ArrayList<BibleVerse> list = new ArrayList<>();
                            list.add(verse);
                            verseHashMap.put(index.getIndexNumber(), list);
                        }
                    }
                }
            }
        }
    }

    public void merge() {
        ObservableList<BibleVerse> selectedItems = leftListViewSelectionModel.getSelectedItems();
        BibleVerse selectedItem = otherListViewSelectionModel.getSelectedItem();
        List<VerseIndex> verseIndices = selectedItem.getVerseIndices();
        verseIndices.clear();
        for (BibleVerse verse : selectedItems) {
            for (VerseIndex index : verse.getVerseIndices()) {
                VerseIndex verseIndex = new VerseIndex();
                verseIndex.setIndexNumber(index.getIndexNumber());
                verseIndices.add(verseIndex);
            }
        }
        selectedItem.setVerseIndices(verseIndices);
        reload();
    }

    public void decreaseIndex() {
        BibleVerse selectedItem = otherListViewSelectionModel.getSelectedItem();
        if (selectedItem == null) {
            return;
        }
        Long indexNumber = selectedItem.getVerseIndices().get(0).getIndexNumber();
        changeIndex(indexNumber, -1000);
        reload();
    }

    public void increaseIndex() {
        BibleVerse selectedItem = otherListViewSelectionModel.getSelectedItem();
        if (selectedItem == null) {
            return;
        }
        Long indexNumber = selectedItem.getVerseIndices().get(0).getIndexNumber();
        changeIndex(indexNumber, 1000);
        reload();
    }

    private void changeIndex(Long indexNumber, int shift) {
        for (Map.Entry<Long, List<BibleVerse>> next : verseHashMap.entrySet()) {
            if (indexNumber == null || next.getKey() >= indexNumber) {
                for (BibleVerse bibleVerse : next.getValue()) {
                    for (VerseIndex verseIndex : bibleVerse.getVerseIndices()) {
                        verseIndex.setIndexNumber(verseIndex.getIndexNumber() + shift);
                    }
                }
            }
        }
    }

    public void save() {
        BibleService bibleService = ServiceManager.getBibleService();
//        bibleService.delete(bible);
        try {
            bibleService.delete(otherBible);
        } catch (Exception e) {
            e.printStackTrace();
        }
//        bibleService.create(bible);
        bibleService.create(otherBible);
    }

    private void copyIndicesFromOtherBible(Bible left, Bible rightBible) {
        BibleService bibleService = ServiceManager.getBibleService();
//        bibleService.delete(rightBible);
        int iBook = 0;
        for (Book book : rightBible.getBooks()) {
            Book otherBook = left.getBooks().get(iBook);
            int iChapter = 0;
            for (Chapter chapter : book.getChapters()) {
                List<Chapter> chapters = otherBook.getChapters();
                if (chapters.size() <= iChapter) {
                    continue;
                }
                Chapter otherChapter = chapters.get(iChapter);
                int iVerse = 0;
                for (BibleVerse bibleVerse : chapter.getVerses()) {
                    List<BibleVerse> verses = otherChapter.getVerses();
                    if (verses.size() <= iVerse) {
                        continue;
                    }
                    BibleVerse otherBibleVerse = verses.get(iVerse);
                    bibleVerse.setVerseIndices(copyVerseIndices(otherBibleVerse.getVerseIndices()));
                    ++iVerse;
                }
                ++iChapter;
            }
            ++iBook;
        }
//        bibleService.create(rightBible);
        reload();
    }

    private List<VerseIndex> copyVerseIndices(List<VerseIndex> verseIndices) {
        ArrayList<VerseIndex> copiedVerseIndices = new ArrayList<>(verseIndices.size());
        for (VerseIndex verseIndex : verseIndices) {
            copiedVerseIndices.add(new VerseIndex(verseIndex));
        }
        return copiedVerseIndices;
    }

    public void task() {
//        copyIndicesFromOtherBible(bible, otherBible); // if it's not the same then it's not good
//        checkIndexNumbersInHashMap();
        uploadBible();
    }

    private void checkIndexNumbersInHashMap() {
        for (Book book : bible.getBooks()) {
            for (Chapter chapter : book.getChapters()) {
                for (BibleVerse bibleVerse : chapter.getVerses()) {
                    List<VerseIndex> verseIndices = bibleVerse.getVerseIndices();
                    if (verseIndices.size() == 0) {
                        System.out.println("Missing indexNumber:" + " " + book.getTitle() + " " + chapter.getNumber() + ":" + bibleVerse.getNumber() + "     " + bibleVerse.getText());
                        continue;
                    }
                    Long indexNumber = verseIndices.get(0).getIndexNumber();
                    if (!verseHashMap.containsKey(indexNumber)) {
                        System.out.println(indexNumber + " " + book.getTitle() + " " + chapter.getNumber() + ":" + bibleVerse.getNumber() + "     " + bibleVerse.getText());
                    }
                }
            }
        }
    }

    public void uploadBible() {
        checkIndexNumbersInHashMap();
        BibleApiBean bibleApiBean = new BibleApiBean();
        setLanguageForBible(otherBible);
        bibleApiBean.uploadBible(otherBible);
        System.out.println("accomplished");
    }

    private void setLanguageForBible(Bible bible) {
        List<Language> languages = ServiceManager.getLanguageService().findAll();
        bible.setLanguage(languages.get(9));
    }

    public void merge1N() {
        ObservableList<BibleVerse> selectedItems = otherListViewSelectionModel.getSelectedItems();
        BibleVerse selectedItem = leftListViewSelectionModel.getSelectedItem();
        List<VerseIndex> verseIndices = selectedItem.getVerseIndices();
        VerseIndex first = verseIndices.get(0);
        verseIndices.clear();
        verseIndices.add(first);
        List<VerseIndex> otherSelectedVerseIndexList = selectedItems.get(0).getVerseIndices();
        long l = otherSelectedVerseIndexList.get(0).getIndexNumber() - first.getIndexNumber();
        if (l != 0) {
            changeIndex(otherSelectedVerseIndexList.get(0).getIndexNumber(), (int) -l);
        }
        for (int i = 1; i < selectedItems.size() && i < 10; ++i) {
            VerseIndex verseIndex = new VerseIndex();
            long newIndex = first.getIndexNumber() + 10L * i;
            verseIndex.setIndexNumber(newIndex);
            verseIndices.add(verseIndex);
            selectedItems.get(i).getVerseIndices().get(0).setIndexNumber(newIndex);
        }
        selectedItem.setVerseIndices(verseIndices);
        reload();
    }

    private void reload() {
        fillVerseMap();
        reloadListViews();
    }

    public void copyIndices() {
        BibleVerse rightVerse = otherListViewSelectionModel.getSelectedItem();
        BibleVerse leftVerse = leftListViewSelectionModel.getSelectedItem();
        List<VerseIndex> rightVerseVerseIndices = rightVerse.getVerseIndices();
        rightVerseVerseIndices.clear();
        List<VerseIndex> leftVerseVerseIndices = leftVerse.getVerseIndices();
        for (VerseIndex verseIndex : leftVerseVerseIndices) {
            VerseIndex index = new VerseIndex();
            index.setIndexNumber(verseIndex.getIndexNumber());
            rightVerseVerseIndices.add(index);
        }
        rightVerse.setVerseIndices(rightVerseVerseIndices);
        reload();
    }
}
