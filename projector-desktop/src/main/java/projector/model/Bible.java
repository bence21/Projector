package projector.model;

import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class Bible extends BaseEntity {

    @DatabaseField
    private String name;
    @DatabaseField
    private String shortName;
    @ForeignCollectionField
    private ForeignCollection<Book> bookForeignCollection;
    private List<Book> books;
    @DatabaseField(foreign = true, index = true)
    private Language language;

    @DatabaseField
    private int usage = 0;
    @DatabaseField
    private Date createdDate;
    @DatabaseField
    private Date modifiedDate;
    @DatabaseField
    private int parallelNumber = 0;
    private Color color;
    @DatabaseField
    private Double red;
    @DatabaseField
    private Double green;
    @DatabaseField
    private Double blue;
    @DatabaseField
    private Double opacity;
    @DatabaseField
    private Integer showAbbreviation;
    @DatabaseField
    private Integer preferredByRemote;
    private boolean hasVerseIndices;
    private boolean hasVerseIndicesChecked = false;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getBookIndex(String bookName) {
        List<Book> bookList = getBooks();
        for (int i = 0; i < bookList.size(); ++i) {
            Book book = bookList.get(i);
            if (book.getShortOrTitle().trim().equals(bookName)) {
                return i;
            }
        }
        return -1;
    }

    public int getUsage() {
        return usage;
    }

    public void setUsage(int usage) {
        this.usage = usage;
    }

    public List<Book> getBooks() {
        if (books == null && bookForeignCollection != null) {
            books = new ArrayList<>(bookForeignCollection.size());
            books.addAll(bookForeignCollection);
        }
        return books;
    }

    public void setBooks(List<Book> books) {
        for (Book book : books) {
            book.setBible(this);
        }
        this.books = books;
    }

    public String getShortName() {
        return shortName;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    public Language getLanguage() {
        return language;
    }

    public void setLanguage(Language language) {
        this.language = language;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    public Date getModifiedDate() {
        return modifiedDate;
    }

    public void setModifiedDate(Date modifiedDate) {
        this.modifiedDate = modifiedDate;
    }

    @Override
    public String toString() {
        if (shortName == null) {
            return name;
        }
        return shortName;
    }

    public int getParallelNumber() {
        return parallelNumber;
    }

    public void setParallelNumber(int parallelNumber) {
        this.parallelNumber = parallelNumber;
    }

    public boolean isParallelSelected() {
        return parallelNumber > 0;
    }

    public Color getColor() {
        if (color == null && red != null) {
            color = Color.color(red, green, blue, opacity);
        }
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
        red = color.getRed();
        green = color.getGreen();
        blue = color.getBlue();
        opacity = color.getOpacity();
    }

    public boolean isShowAbbreviation() {
        return showAbbreviation == null || showAbbreviation < 0;
    }

    public void setShowAbbreviation(boolean showAbbreviation) {
        if (showAbbreviation) {
            this.showAbbreviation = 1;
        } else {
            this.showAbbreviation = -1;
        }
    }

    public boolean isPreferredByRemote() {
        return preferredByRemote == null || preferredByRemote > 0;
    }

    public void setPreferredByRemote(boolean preferredByRemote) {
        if (preferredByRemote) {
            this.preferredByRemote = 1;
        } else {
            this.preferredByRemote = -1;
        }
    }

    public boolean equivalent(Bible other) {
        return super.equivalent(other);
    }

    public Book getBook(int index) {
        List<Book> bookList = getBooks();
        if (bookList == null || index < 0 || index >= bookList.size()) {
            return null;
        }
        return bookList.get(index);
    }

    public boolean hasVerseIndices() {
        return hasVerseIndices;
    }

    public void setHasVerseIndices(boolean hasVerseIndices) {
        this.hasVerseIndices = hasVerseIndices;
    }

    public void setHasVerseIndicesChecked(boolean hasVerseIndicesChecked) {
        this.hasVerseIndicesChecked = hasVerseIndicesChecked;
    }

    public boolean hasVerseIndicesChecked() {
        return hasVerseIndicesChecked;
    }

    @Override
    public String getUuid() {
        ensureUuid();
        return super.getUuid();
    }

    private void ensureUuid() {
        if (super.getUuid() == null) {
            setUuid(UUID.randomUUID().toString());
        }
    }
}
