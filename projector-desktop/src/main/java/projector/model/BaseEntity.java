package projector.model;

import com.j256.ormlite.field.DatabaseField;

import java.io.Serial;
import java.io.Serializable;

public abstract class BaseEntity extends AbstractModel implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;
    @DatabaseField(generatedId = true, index = true)
    private Long id;

    public BaseEntity() {
        this((Long) null);
    }

    public BaseEntity(Long id) {
        super();
        this.id = id;
    }

    public BaseEntity(BaseEntity other) {
        super(other);
        this.id = other.id;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    protected boolean equivalent(BaseEntity other) {
        if (other != null) {
            Long id = getId();
            if (id != null) {
                return id.equals(other.getId());
            } else if (other.getId() != null) {
                return false;
            }
        }
        return super.equals(other);
    }
}
