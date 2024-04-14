package com.bence.projector.common.dto;

public class LoginSongDTO {
    private String username;
    private String password;
    private SongDTO songDTO;

    public LoginSongDTO() {
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public SongDTO getSongDTO() {
        return songDTO;
    }

    public void setSongDTO(SongDTO songDTO) {
        this.songDTO = songDTO;
    }
}
