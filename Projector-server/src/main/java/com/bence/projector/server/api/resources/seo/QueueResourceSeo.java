package com.bence.projector.server.api.resources.seo;

import com.bence.projector.server.backend.model.Song;
import com.bence.projector.server.backend.model.SongList;
import com.bence.projector.server.backend.model.SongListElement;
import com.bence.projector.server.backend.repository.SongRepository;
import com.bence.projector.server.backend.service.SongListService;
import com.bence.projector.server.backend.service.SongService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.List;

@Controller
public class QueueResourceSeo {
    private final SongService songService;
    private final SongListService songListService;
    private final SongRepository songRepository;

    @Autowired
    public QueueResourceSeo(SongService songService, SongListService songListService, SongRepository songRepository) {
        this.songService = songService;
        this.songListService = songListService;
        this.songRepository = songRepository;
    }

    @GetMapping("/queue")
    public String song(Model model, @RequestParam("ids") String ids) {
        String[] split = ids.split(",");
        List<Song> songs = new ArrayList<>(split.length);
        for (String s : split) {
            Song song = songService.findOneByUuid(s);
            if (song != null) {
                song.setBeforeId("song/");
                songs.add(song);
            }
        }
        if (!songs.isEmpty()) {
            model.addAttribute("title", "List: " + songs.get(0).getTitle());
            StringBuilder description = new StringBuilder();
            for (int i = 1; i < songs.size(); ++i) {
                description.append(songs.get(i).getTitle()).append("\n");
            }
            model.addAttribute("description", description.toString());
        }
        model.addAttribute("songs", songs);
        return "queue";
    }

    @GetMapping("/songList/{id}")
    public String songList(Model model, @PathVariable("id") String id) {
        SongList songList = songListService.findOneByUuid(id);
        if (songList == null) {
            return "pageNotFound";
        }
        List<SongListElement> songListElements = songList.getSongListElements();
        List<Song> songs = new ArrayList<>(songListElements.size());
        for (SongListElement element : songListElements) {
            Song song = songRepository.findOneByUuid(element.getSongUuid());
            if (song != null) {
                song.setBeforeId("../song/");
                songs.add(song);
            }
        }
        model.addAttribute("title", songList.getTitle());
        model.addAttribute("description", songList.getDescription());
        model.addAttribute("songs", songs);
        return "songList";
    }
}
