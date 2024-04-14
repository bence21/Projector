package projector.model.sqlite;

import com.j256.ormlite.field.DatabaseField;

public class Info {

    @DatabaseField
    private String value;
    @DatabaseField
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
