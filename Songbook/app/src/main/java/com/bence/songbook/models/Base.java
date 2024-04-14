package com.bence.songbook.models;

import com.j256.ormlite.field.DatabaseField;

public class Base {
    @DatabaseField(generatedId = true)
    private Long id;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
