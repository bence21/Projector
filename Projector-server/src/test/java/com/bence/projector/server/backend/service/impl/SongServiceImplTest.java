package com.bence.projector.server.backend.service.impl;

import com.bence.projector.common.model.SectionType;
import com.bence.projector.server.backend.model.Language;
import com.bence.projector.server.backend.model.Song;
import com.bence.projector.server.backend.model.SongVerse;
import com.bence.projector.server.backend.repository.SongRepository;
import com.bence.projector.server.backend.service.LanguageService;
import com.bence.projector.server.backend.service.SongService;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class SongServiceImplTest extends BaseServiceTest {

    @Autowired
    private SongService songService;
    @Autowired
    private SongRepository songRepository;
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
        String suffix = UUID.randomUUID().toString();
        language.setEnglishName("Test language " + suffix);
        language.setNativeName("Teszt " + suffix.substring(0, 8));
        return languageService.save(language);
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
        if (!songs.isEmpty()) {
            for (Song song : songs) {
                songService.delete(song.getId());
            }
            songs = songService.findAll();
        }
        Assert.assertEquals(0, songs.size());
    }

    @Test
    public void testDeleteByUuid() {
        Song song = getASong(languageService);
        songService.save(song);
        String uuid = song.getUuid();
        Assert.assertNotNull(uuid);
        songService.deleteByUuid(uuid);
        Assert.assertNull(songService.findOneByUuid(uuid));
    }

    @Test
    public void testDelete() {
        Song song = getASong(languageService);
        Song saved = songService.save(song);
        Long id = saved.getId();
        Assert.assertNotNull(id);
        songService.delete(id);
        Assert.assertNull(songService.findOne(id));
    }

    @Test
    public void testFindOne() {
        Song song = getASong(languageService);
        Song saved = songService.save(song);
        Song found = songService.findOne(saved.getId());
        Assert.assertNotNull(found);
        Assert.assertEquals(saved.getUuid(), found.getUuid());
        Assert.assertEquals(song.getTitle(), found.getTitle());
        songService.deleteByUuid(saved.getUuid());
    }

    @Test
    public void testFindOneByUuid() {
        Song song = getASong(languageService);
        Song saved = songService.save(song);
        String uuid = saved.getUuid();
        Song found = songService.findOneByUuid(uuid);
        Assert.assertNotNull(found);
        Assert.assertEquals(saved.getId(), found.getId());
        Assert.assertEquals(song.getTitle(), found.getTitle());
        songService.deleteByUuid(uuid);
    }

    @Test
    public void testSave() {
        Song song = getASong(languageService);
        songService.save(song);
        Song song1 = songService.findAll().get(0);
        Assert.assertEquals(song.getTitle(), song1.getTitle());
        Assert.assertNotNull(song1.getWordQualityScore());
        Assert.assertTrue(song1.getWordQualityScore() >= 0 && song1.getWordQualityScore() <= 100);
    }

    @Test
    public void evaluateLcsForSimilarTexts_preciseMode_recomputesUncappedForLongIdenticalPair() {
        String body = "\nór isten, kérlek, kegyelmezz nékem\n";
        String s = body.repeat(120);
        SongServiceImpl.LcsSongPairResult r = SongServiceImpl.evaluateLcsForSimilarTexts(s, s, true);
        Assert.assertTrue(r.usedUncappedLcs());
        Assert.assertTrue(r.combinedPassesLcsThreshold());
        Assert.assertEquals(1.0, (r.ratioAlongA() + r.ratioAlongB()) / 2, 1e-12);
    }

    @Test
    public void evaluateLcsForSimilarTexts_fastMode_returnsExactScoreForLongIdenticalPair() {
        String body = "\nór isten, kérlek, kegyelmezz nékem\n";
        String s = body.repeat(120);
        SongServiceImpl.LcsSongPairResult r = SongServiceImpl.evaluateLcsForSimilarTexts(s, s, false);
        Assert.assertFalse(r.usedUncappedLcs());
        Assert.assertEquals(1.0, r.ratioAlongA(), 1e-15);
        Assert.assertEquals(1.0, r.ratioAlongB(), 1e-15);
    }

    @Test
    public void delete_head_twoSongGroup_clearsSurvivorVersionGroup() {
        Song head = getASong(languageService);
        head.setTitle("Head " + UUID.randomUUID());
        songService.save(head);
        Song member = getASong(languageService);
        member.setTitle("Member " + UUID.randomUUID());
        songService.save(member);
        member.setVersionGroup(head);
        songRepository.save(member);

        songService.delete(head.getId());

        Song reloaded = songRepository.findOneByUuid(member.getUuid());
        Assert.assertNotNull(reloaded);
        Assert.assertNull(reloaded.getVersionGroupUuid());
        songService.delete(member.getId());
    }

    @Test
    public void delete_head_threeSongGroup_promotesMostRecentlyModified() {
        long t = System.currentTimeMillis();
        Song head = getASong(languageService);
        head.setTitle("Head " + UUID.randomUUID());
        head.setModifiedDate(new Date(t - 30_000));
        songService.save(head);

        Song mSlow = getASong(languageService);
        mSlow.setTitle("MSlow " + UUID.randomUUID());
        mSlow.setModifiedDate(new Date(t - 20_000));
        songService.save(mSlow);

        Song mNew = getASong(languageService);
        mNew.setTitle("MNew " + UUID.randomUUID());
        mNew.setModifiedDate(new Date(t));
        songService.save(mNew);

        mSlow.setVersionGroup(head);
        songRepository.save(mSlow);
        mNew.setVersionGroup(head);
        songRepository.save(mNew);

        songService.delete(head.getId());

        Song reSlow = songRepository.findOneByUuid(mSlow.getUuid());
        Song reNew = songRepository.findOneByUuid(mNew.getUuid());
        Assert.assertNotNull(reSlow);
        Assert.assertNotNull(reNew);
        Assert.assertNull(reNew.getVersionGroupUuid());
        Assert.assertEquals(reNew.getUuid(), reSlow.getVersionGroupUuid());

        songService.delete(mSlow.getId());
        songService.delete(mNew.getId());
    }

    @Test
    public void delete_nonHeadMember_doesNotBreakHead() {
        Song head = getASong(languageService);
        head.setTitle("Head " + UUID.randomUUID());
        songService.save(head);
        Song member = getASong(languageService);
        member.setTitle("Member " + UUID.randomUUID());
        songService.save(member);
        member.setVersionGroup(head);
        songRepository.save(member);

        songService.delete(member.getId());

        Song reHead = songRepository.findOneByUuid(head.getUuid());
        Assert.assertNotNull(reHead);
        Assert.assertTrue(songRepository.findAllByVersionGroup(reHead).isEmpty());

        songService.delete(head.getId());
    }
}