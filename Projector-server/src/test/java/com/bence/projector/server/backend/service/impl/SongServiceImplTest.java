package com.bence.projector.server.backend.service.impl;

import com.bence.projector.common.model.SectionType;
import com.bence.projector.server.backend.model.Language;
import com.bence.projector.server.backend.model.Song;
import com.bence.projector.server.backend.model.SongVerse;
import com.bence.projector.server.backend.service.LanguageService;
import com.bence.projector.server.backend.service.SongService;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class SongServiceImplTest extends BaseServiceTest {

    @Autowired
    private SongService songService;
    @Autowired
    private LanguageService languageService;

    public static Song getASong(LanguageService languageService) {
        Song song = new Song();
        song.setTitle("Test Title");
        song.setAuthor("Testing");
        song.setCreatedByEmail("test@site.com");
        song.setCreatedDate(new Date());
        song.setModifiedDate(song.getCreatedDate());
        song.setVerses(getSongVerses());
        song.setLanguage(getLanguage(languageService));
        return song;
    }

    private static Language getLanguage(LanguageService languageService) {
        Language language = new Language();
        language.setEnglishName("Test language");
        language.setNativeName("Teszt");
        languageService.save(language);
        return languageService.findAll().get(0);
    }

    public static List<SongVerse> getSongVerses() {
        ArrayList<SongVerse> songVerses = new ArrayList<>();
        SongVerse songVerse = new SongVerse();
        songVerse.setSectionType(SectionType.CHORUS);
        songVerse.setText("This is a test text");
        songVerses.add(songVerse);
        return songVerses;
    }

    @Test
    public void testFindAll() {
        List<Song> songs = songService.findAll();
        if (songs.size() > 0) {
            for (Song song : songs) {
                songService.delete(song.getId());
            }
            songs = songService.findAll();
        }
        Assert.assertEquals(0, songs.size());
    }

    public void testDeleteByUuid() {
    }

    public void testDelete() {
    }

    public void testFindOne() {
    }

    public void testFindOneByUuid() {
    }

    @Test
    public void testSave() {
        Song song = getASong(languageService);
        songService.save(song);
        Song song1 = songService.findAll().get(0);
        Assert.assertEquals(song.getTitle(), song1.getTitle());
    }
}