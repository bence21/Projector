package com.bence.projector.server.api.resources.seo;

import com.bence.projector.server.backend.model.Song;
import com.bence.projector.server.backend.model.SongVerse;
import com.bence.projector.server.backend.service.SongService;
import org.hibernate.HibernateException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.security.Principal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.bence.projector.server.api.resources.SiteMapController.getCanonicalUrlForSong;
import static com.bence.projector.server.utils.MemoryUtil.getEmptyList;

@Controller
public class SongResourceSeo {
    private final SongService songService;
    private List<Song> songs;

    @Autowired
    public SongResourceSeo(SongService songService) {
        this.songService = songService;
    }

    @GetMapping("/song/{id}")
    public String song(Model model, @PathVariable("id") String id, Principal principal) {
        songService.startThreadFindForSong(id);
        Song song = songService.findOneByUuid(id);
        if (song == null || song.isDeleted() || song.isBackUp()) {
            return "pageNotFound";
        }
        model.addAttribute("title", song.getTitle());
        if (principal == null) {
            song = setSongTextLines(song); // for lazy loading
            addExtraInfo(model, song);
        } else {
            setEmptyLines(song);
        }
        model.addAttribute("song", song);
        return "song";
    }

    private void addExtraInfo(Model model, Song song) {
        Song randomSong = songService.getRandomSong(song.getLanguage());
        if (randomSong != null && randomSong.isPublic()) {
            model.addAttribute("randomSongUrl", "/song/" + randomSong.getUuid());
        }
        HashMap<String, Integer> hashMap = new HashMap<>();
        String regex = "[.!?,;'\\\\:\"|<>{}\\[\\]_\\-=+0-9@#$%^&*()`~\\n]+";
        for (SongVerse songVerse : song.getVerses()) {
            for (String s : songVerse.getText().replaceAll(regex, " ").split(" ")) {
                String lowerCase = s.trim().toLowerCase();
                if (lowerCase.length() > 2) {
                    Integer integer = hashMap.get(lowerCase);
                    if (integer == null || integer == 0) {
                        hashMap.put(lowerCase, 1);
                    } else {
                        hashMap.replace(lowerCase, integer + 1);
                    }
                }
            }
        }
        List<Map.Entry<String, Integer>> list = new LinkedList<>(hashMap.entrySet());

        // Sorting the list based on values
        list.sort((o1, o2) -> o2.getValue().compareTo(o1.getValue()));

        // Maintaining insertion order with the help of LinkedList
        Map<String, Integer> sortedMap = new LinkedHashMap<>();
        for (Map.Entry<String, Integer> entry : list) {
            sortedMap.put(entry.getKey(), entry.getValue());
        }
        Set<String> strings = sortedMap.keySet();
        StringBuilder keywords = new StringBuilder("lyrics");
        if (song.getLanguage() != null) {
            String englishName = song.getLanguage().getEnglishName();
            switch (englishName) {
                case "Hungarian" -> keywords.append(", dalszöveg, szöveg, ének");
                case "Romanian" -> keywords.append(", versuri, text, cântec, imn");
                case "German" -> keywords.append(", text, lied, hymne");
                default -> //if (englishName.equals("English"))
                        keywords.append(", text, hymn, song");
            }
        }
        Iterator<String> iterator = strings.iterator();
        for (int i = 0; iterator.hasNext() && i < 4; ++i) {
            keywords.append(", ").append(iterator.next());
        }
        keywords.append(", ").append(song.getTitle().replaceAll(regex, ""));

        model.addAttribute("keywords", keywords.toString());
        model.addAttribute("youtubeUrl", "http://img.youtube.com/vi/" + song.getYoutubeUrl() + "/0.jpg");
        model.addAttribute("description", getDescriptionText(song));
        model.addAttribute("canonicalUrl", getCanonicalUrlForSong(song));
    }

    private void setEmptyLines(Song song) {
        ArrayList<String> emptyList = getEmptyList();
        for (SongVerse songVerse : song.getVerses()) {
            songVerse.setLines(emptyList);
        }
    }

    private Song setSongTextLines_(Song song) {
        for (SongVerse songVerse : song.getVerses()) {
            songVerse.setTextLines();
        }
        return song;
    }

    private Song setSongTextLines(Song song) {
        try {
            return setSongTextLines_(song);
        } catch (NullPointerException e) {
            return setSongTextLines_(songService.reloadSong(song));
        } catch (HibernateException e) {
            if (e.getMessage().contains("collection was evicted")) {
                return setSongTextLines_(songService.reloadSong(song));
            } else {
                e.printStackTrace();
            }
        }
        return song;
    }

    private String getDescriptionText(Song song) {
        List<SongVerse> verses = song.getVerses();
        if (verses == null || verses.size() <= 0) {
            return "";
        }
        SongVerse songVerse = verses.get(0);
        return songVerse.getText();
    }

    @GetMapping("/song")
    public String songs(Model model) {
        List<Song> songs = getSongs();
        model.addAttribute("songs", songs);
        return "songs";
    }

    private List<Song> getSongs() {
        if (songs == null) {
            songs = songService.findAll();
            for (Song song : songs) {
                song.setBeforeId("../song/");
            }
        }
        return songs;
    }
}
