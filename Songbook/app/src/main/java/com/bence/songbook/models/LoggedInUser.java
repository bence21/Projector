package com.bence.songbook.models;

import com.j256.ormlite.field.DatabaseField;

public class LoggedInUser extends Base {

    @DatabaseField
    private String email;
    @DatabaseField
    private String password;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
