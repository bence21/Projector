package com.bence.projector.server.backend.model;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.persistence.PrePersist;
import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@MappedSuperclass
public abstract class AbstractModel extends BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;
    @Column(name = "uuid", length = 36)
    private String uuid;

    public AbstractModel() {
    }

    public AbstractModel(AbstractModel songCollection) {
        this.uuid = songCollection.uuid;
    }

    @Override
    public int hashCode() {
        int prime = 31;
        int result = 1;
        result = prime * result + ((this.getUuid() == null) ? 0 : this.getUuid().hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final AbstractModel that = (AbstractModel) o;

        return Objects.equals(uuid, that.uuid);
    }

    public String getUuid() {
        ensureUuid();
        return uuid;
    }

    public void setUuid(final String uuid) {
        this.uuid = uuid;
    }

    private void ensureUuid() {
        if (uuid == null) {
            setUuid(UUID.randomUUID().toString());
        }
    }

    @PrePersist
    public void onPrePersist() {
        ensureUuid();
    }
}
