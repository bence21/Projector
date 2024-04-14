package com.bence.songbook.models;

import com.google.gson.annotations.Expose;
import com.j256.ormlite.field.DatabaseField;

import java.io.Serializable;

public abstract class AbstractModel implements Serializable {

    private static final long serialVersionUID = 1L;
    @Expose
    @DatabaseField(index = true)
    private String uuid;

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

        return uuid != null ? uuid.equals(that.uuid) : that.uuid == null;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(final String uuid) {
        this.uuid = uuid;
    }
}
