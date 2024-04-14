package com.bence.projector.server.utils;

import com.bence.projector.server.backend.model.Language;
import com.bence.projector.server.backend.model.Song;
import com.bence.projector.server.backend.model.User;
import com.bence.projector.server.backend.service.LanguageService;
import com.bence.projector.server.backend.service.SongService;
import com.bence.projector.server.backend.service.UserService;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import static com.bence.projector.server.api.resources.SongResource.createBackUpSongWithoutSave;
import static com.bence.projector.server.utils.StringUtils.formatSongAndCheckChange;

public class SongUtil {

    public static Song getLastModifiedSong(List<Song> songs) {
        if (songs == null || songs.size() == 0) {
            return null;
        }
        Song lastModifiedSong = songs.get(0);
        for (Song song : songs) {
            if (lastModifiedSong.getModifiedDate().before(song.getModifiedDate())) {
                lastModifiedSong = song;
            }
        }
        return lastModifiedSong;
    }

    public static void checkSongTexts(LanguageService languageService, UserService userService, SongService songService) {
        List<Language> languages = languageService.findAll();
        for (Language language : languages) {
            System.out.println(language.getUuid() + " " + language.getEnglishName());
        }
        System.out.println();
        Language language = languageService.findOneByUuid("5a2d253b8c270b37345af0c3");
        List<User> admins = userService.findAllAdmins();
        User admin = admins.get(0);
        List<Song> songs = language.getSongs();
        songs.sort(Comparator.comparing(Song::getModifiedDate));
        List<Song> modifiedSongs = checkSongTextsForSongs(songs);
        List<Song> backUpSongs = new ArrayList<>();
        for (Song song : modifiedSongs) {
            song.setModifiedDate(new Date());
            song.setLastModifiedBy(admin);
            backUpSongs.add(song.getBackUp());
        }
        songService.save(backUpSongs);
        songService.save(modifiedSongs);
        System.out.println("Done! " + modifiedSongs.size());
    }

    private static List<Song> checkSongTextsForSongs(List<Song> songs) {
        List<Song> modifiedSongs = new ArrayList<>();
        try {
            FileOutputStream fileOutputStream = new FileOutputStream("beforeSongs.txt");
            BufferedWriter beforeWriter = new BufferedWriter(new OutputStreamWriter(fileOutputStream, StandardCharsets.UTF_8));
            FileOutputStream afterFileOutputStream = new FileOutputStream("afterSongs.txt");
            BufferedWriter afterWriter = new BufferedWriter(new OutputStreamWriter(afterFileOutputStream, StandardCharsets.UTF_8));
            for (Song song : songs) {
                if (song.isPublic()) {
                    if (checkSongText(song, beforeWriter, afterWriter)) {
                        modifiedSongs.add(song);
                    }
                }
            }
            beforeWriter.close();
            afterWriter.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return modifiedSongs;
    }

    private static boolean checkSongText(Song song, BufferedWriter beforeWriter, BufferedWriter afterWriter) {
        String text = song.getText();
        createBackUpSongWithoutSave(song);
        if (formatSongAndCheckChange(song)) {
            try {
                beforeWriter.write(text + "\n");
                afterWriter.write(song.getText() + "\n");
            } catch (IOException ignored) {
            }
            return true;
        }
        return false;
    }

}
