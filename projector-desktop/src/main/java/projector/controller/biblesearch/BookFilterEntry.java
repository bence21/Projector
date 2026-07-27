package projector.controller.biblesearch;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class BookFilterEntry {

    private final int bookIndex;
    private final StringProperty name = new SimpleStringProperty();
    private final BooleanProperty selected = new SimpleBooleanProperty(true);
    private final IntegerProperty matchCount = new SimpleIntegerProperty(-1);

    public BookFilterEntry(int bookIndex, String name, boolean selected) {
        this.bookIndex = bookIndex;
        this.name.set(name);
        this.selected.set(selected);
    }

    public int getBookIndex() {
        return bookIndex;
    }

    public StringProperty nameProperty() {
        return name;
    }

    public BooleanProperty selectedProperty() {
        return selected;
    }

    public IntegerProperty matchCountProperty() {
        return matchCount;
    }

    public String getCountLabel() {
        int count = matchCount.get();
        if (count < 0) {
            return "";
        }
        return "(" + count + ")";
    }
}
