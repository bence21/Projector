package projector.model;

import com.google.gson.annotations.Expose;
import com.j256.ormlite.field.DatabaseField;

public abstract class AbstractModel {

    @Expose
    @DatabaseField(index = true, width = 36)
    private String uuid;

    public AbstractModel(AbstractModel other) {
        this.uuid = other.uuid;
    }

    public AbstractModel() {

    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }
}
