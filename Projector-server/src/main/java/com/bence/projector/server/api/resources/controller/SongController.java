package com.bence.projector.server.api.resources.controller;

import com.bence.projector.server.backend.model.Song;
import com.bence.projector.server.backend.repository.SongRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.Comparator;
import java.util.List;

@Controller
public class SongController {

    private final SongRepository songRepository;

    @Autowired
    public SongController(SongRepository songRepository) {
        this.songRepository = songRepository;
    }

    @RequestMapping(method = RequestMethod.GET, value = "/admin/checkShortSongs")
    public String checkShortSongs(Model model) {
        List<Song> shortSongs = songRepository.findAllByVersesIsShort();
        System.out.println("shortSongs.size() = " + shortSongs.size());
        shortSongs.sort(Comparator.comparingInt(o -> o.getText().length()));
        model.addAttribute("songs", shortSongs);
        return "queue";
    }
}
