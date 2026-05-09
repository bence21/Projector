package com.bence.projector.server.mailsending;

/**
 * Row model for batch version-group ({@code SongLink}) notification emails.
 */
public class SongLinkRow {

    private String id;
    private String email;
    private String song1Uuid;
    private String song2Uuid;
    private String song1Title;
    private String song2Title;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSong1Uuid() {
        return song1Uuid;
    }

    public void setSong1Uuid(String song1Uuid) {
        this.song1Uuid = song1Uuid;
    }

    public String getSong2Uuid() {
        return song2Uuid;
    }

    public void setSong2Uuid(String song2Uuid) {
        this.song2Uuid = song2Uuid;
    }

    public String getSong1Title() {
        return song1Title;
    }

    public void setSong1Title(String song1Title) {
        this.song1Title = song1Title;
    }

    public String getSong2Title() {
        return song2Title;
    }

    public void setSong2Title(String song2Title) {
        this.song2Title = song2Title;
    }
}
