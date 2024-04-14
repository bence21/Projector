package com.bence.projector.server.utils;

import com.bence.projector.server.backend.model.Song;
import com.bence.projector.server.backend.repository.SongRepository;

import java.util.List;

public class ChangeInSong {

    public static void changeCreatedByEmailToOther(String oldCreatedByEmail, String newCreatedByEmail, SongRepository songRepository) {
        List<Song> songs = songRepository.findAllByCreatedByEmail(oldCreatedByEmail);
        for (Song song : songs) {
            song.setCreatedByEmail(newCreatedByEmail);
        }
        songRepository.saveAll(songs);
        System.out.println("Changed in " + songs.size() + " songs the from " + oldCreatedByEmail + " to " + newCreatedByEmail);
    }
}
