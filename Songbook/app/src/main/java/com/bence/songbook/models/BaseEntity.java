package com.bence.songbook.models;

import com.j256.ormlite.field.DatabaseField;

public class BaseEntity extends AbstractModel {
    @DatabaseField(generatedId = true, index = true)
    private Long id;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
