package projector.model.sqlite;

import com.j256.ormlite.field.DatabaseField;

public class Books {

    @DatabaseField(generatedId = true, index = true)
    private Long book_number;
    @DatabaseField
    private String short_name;
    @DatabaseField
    private String long_name;
    @DatabaseField
    private String book_color;

    public Long getBook_number() {
        return book_number;
    }

    public void setBook_number(Long book_number) {
        this.book_number = book_number;
    }

    public String getShort_name() {
        return short_name;
    }

    public void setShort_name(String short_name) {
        this.short_name = short_name;
    }

    public String getLong_name() {
        return long_name;
    }

    public void setLong_name(String long_name) {
        this.long_name = long_name;
    }

    public String getBook_color() {
        return book_color;
    }

    public void setBook_color(String book_color) {
        this.book_color = book_color;
    }
}
