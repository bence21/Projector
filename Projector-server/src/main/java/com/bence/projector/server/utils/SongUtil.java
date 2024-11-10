package com.bence.projector.server.utils;

import com.bence.projector.common.model.SectionType;
import com.bence.projector.server.backend.model.Language;
import com.bence.projector.server.backend.model.Song;
import com.bence.projector.server.backend.model.SongLink;
import com.bence.projector.server.backend.model.SongVerse;
import com.bence.projector.server.backend.model.User;
import com.bence.projector.server.backend.repository.SongLinkRepository;
import com.bence.projector.server.backend.service.LanguageService;
import com.bence.projector.server.backend.service.SongLinkService;
import com.bence.projector.server.backend.service.SongService;
import com.bence.projector.server.backend.service.UserService;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import static com.bence.projector.server.api.resources.SongResource.createBackUpSongWithoutSave;
import static com.bence.projector.server.api.resources.SongResource.mergeSongVersionGroup;
import static com.bence.projector.server.utils.StringUtils.formatSongAndCheckChange;
import static com.bence.projector.server.utils.StringUtils.isSongVerseChanged;
import static com.bence.projector.server.utils.StringUtils.replaceAllOtherThenLetterAndNumber2;
import static com.bence.projector.server.utils.StringUtils.stripAccents;

public class SongUtil {

    private static final String refEnding = " *\n?";
    private static final String endingWithColon = ":" + refEnding;

    public static Song getLastModifiedSong(List<Song> songs) {
        if (songs == null || songs.isEmpty()) {
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

    private static Result getHungarianSongsResult(LanguageService languageService, UserService userService) {
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
        return new Result(admin, songs);
    }

    @SuppressWarnings("unused")
    public static void checkSongTexts(LanguageService languageService, UserService userService, SongService songService) {
        Result result = getHungarianSongsResult(languageService, userService);
        List<Song> modifiedSongs = checkSongTextsForSongs(result.songs());
        saveModifiedSongsWithBackups(modifiedSongs, result.admin(), songService);
    }

    @SuppressWarnings("unused")
    public static void deleteSectionTextsFromHungarianSongTexts(LanguageService languageService, UserService userService, SongService songService) {
        Result result = getHungarianSongsResult(languageService, userService);
        List<Song> modifiedSongs = deleteSectionTextsFromHungarianSongs(result.songs());
        saveModifiedSongsWithBackups(modifiedSongs, result.admin(), songService);
    }

    @SuppressWarnings("unused")
    public static void markSimilarSongsAndSet(SongService songService, LanguageService languageService, SongLinkRepository songLinkRepository, SongLinkService songLinkService) {
        List<Language> languages = languageService.findAll();
        for (Language language : languages) {
            // if (language.getEnglishName().equals("Hungarian")) {
            markSimilarSongsAndSetForLanguage(songService, language, songLinkRepository, songLinkService);
            // }
        }
    }

    private static void markSimilarSongsAndSetForLanguage(SongService songService, Language language, SongLinkRepository songLinkRepository, SongLinkService songLinkService) {
        Date startDate = new Date();
        long startDateTime = startDate.getTime();
        Collection<Song> songs = songService.getSongsByLanguageForSimilarWithVersionGroup(language);
        // songs = new ArrayList<>(songs).subList(0, 4000);
        int n = songs.size();
        int i = 0;
        int p = 0;
        for (Song songForSimilar : songs) {
            ++i;
            if (!songForSimilar.isPublic()) {
                continue;
            }
            List<Song> similarSongs = songService.findAllSimilarSongsForSong(songForSimilar, false, songs);
            markSimilarSongsAndSetForSong(songForSimilar, similarSongs, songLinkRepository, songService, songLinkService);
            p = logProgress(i, n, p, startDateTime);
        }
        System.out.println("Elapsed seconds: " + (new Date().getTime() - startDateTime) / 1000);
    }

    private static int logProgress(int i, int n, int p, long startDateTime) {
        double progress = (double) (i * 100) / n;
        if ((int) progress != p) {
            p = (int) progress;
            Date date = new Date();
            long dateTime = date.getTime();
            double remainingMs = (n - i);
            remainingMs = remainingMs / i * (dateTime - startDateTime);
            double remainingSeconds = (long) (remainingMs / 100);
            remainingSeconds /= 10;
            System.out.println(p + "%\tRemaining seconds: " + remainingSeconds + "\t Estimated finish: " + new Date((long) (dateTime + remainingMs)) + "\t" + date);
        }
        return p;
    }

    private static void markSimilarSongsAndSetForSong(Song songForSimilar, List<Song> similarSongs, SongLinkRepository songLinkRepository, SongService songService, SongLinkService songLinkService) {
        if (similarSongs == null || similarSongs.isEmpty()) {
            return;
        }
        List<SongLink> songLinks = null;
        for (Song similarSong : similarSongs) {
            //noinspection ConstantValue
            handleSimilarSong(songForSimilar, similarSong, songLinks, songService, songLinkService, songLinkRepository);
        }
    }

    private static void handleSimilarSong(Song songForSimilar, Song similarSong, List<SongLink> songLinks, SongService songService, SongLinkService songLinkService, SongLinkRepository songLinkRepository) {
        Song loadedSongForSimilar = songService.findOneByUuid(songForSimilar.getUuid());
        Song loadedSimilarSong = songService.findOneByUuid(similarSong.getUuid());
        handleSimilarLoadedSong(loadedSongForSimilar, loadedSimilarSong, songLinks, songService, songLinkService, similarSong.getSimilarRatio(), songLinkRepository);
    }

    private static void handleSimilarLoadedSong(Song loadedSongForSimilar, Song loadedSimilarSong, List<SongLink> songLinks, SongService songService, SongLinkService songLinkService, double percentage, SongLinkRepository songLinkRepository) {
        if (loadedSongForSimilar.isSameVersionGroup(loadedSimilarSong)) {
            return;
        }
        if (percentage > 0.9 && percentage < 0.99) {
            mergeSongVersionGroup(loadedSongForSimilar, loadedSimilarSong, songService);
        } else {
            songLinkForSimilar(loadedSongForSimilar, loadedSimilarSong, songLinks, songLinkService, songLinkRepository);
        }
    }

    private static void songLinkForSimilar(Song songForSimilar, Song similarSong, List<SongLink> songLinks, SongLinkService songLinkService, SongLinkRepository songLinkRepository) {
        if (containsSongLink(songForSimilar, similarSong, songLinks, songLinkRepository)) {
            return;
        }
        SongLink songLink = new SongLink();
        songLink.setApplied(false);
        songLink.setCreatedDate(new Date());
        songLink.setSong1(songForSimilar);
        songLink.setSong2(similarSong);
        songLinkService.save(songLink);
    }

    private static boolean containsSongLink(Song songForSimilar, Song similarSong, List<SongLink> songLinks, SongLinkRepository songLinkRepository) {
        songLinks = ensureSongLinks(songLinks, songForSimilar, songLinkRepository);
        for (SongLink songLink : songLinks) {
            if (songLink.isSameSongs(songForSimilar, similarSong)) {
                return true;
            }
        }
        return false;
    }

    private static List<SongLink> ensureSongLinks(List<SongLink> songLinks, Song songForSimilar, SongLinkRepository songLinkRepository) {
        if (songLinks == null) {
            songLinks = songLinkRepository.findAllBySong1OrSong2(songForSimilar, songForSimilar);
        }
        return songLinks;
    }

    private static void saveModifiedSongsWithBackups(List<Song> modifiedSongs, User result, SongService songService) {
        List<Song> backUpSongs = new ArrayList<>();
        for (Song song : modifiedSongs) {
            song.setModifiedDate(new Date());
            song.setLastModifiedBy(result);
            backUpSongs.add(song.getBackUp());
        }
        songService.save(backUpSongs);
        songService.save(modifiedSongs);
        System.out.println("Done! " + modifiedSongs.size());
    }

    private record Result(User admin, List<Song> songs) {
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

    private static List<Song> deleteSectionTextsFromHungarianSongs(List<Song> songs) {
        List<Song> modifiedSongs = new ArrayList<>();
        try {
            FileOutputStream fileOutputStream = new FileOutputStream("beforeSongs.txt");
            BufferedWriter beforeWriter = new BufferedWriter(new OutputStreamWriter(fileOutputStream, StandardCharsets.UTF_8));
            FileOutputStream afterFileOutputStream = new FileOutputStream("afterSongs.txt");
            BufferedWriter afterWriter = new BufferedWriter(new OutputStreamWriter(afterFileOutputStream, StandardCharsets.UTF_8));
            for (Song song : songs) {
                if (song.isPublic()) {
                    if (deleteSectionTextsFromHungarianSong(song, beforeWriter, afterWriter)) {
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

    private static boolean deleteSectionTextsFromHungarianSong(Song song, BufferedWriter beforeWriter, BufferedWriter afterWriter) {
        String text = song.getText();
        createBackUpSongWithoutSave(song);
        if (deleteSectionTextsFromHungarianSongAndCheckChange(song)) {
            try {
                beforeWriter.write(text + "\n");
                afterWriter.write(song.getText() + "\n");
            } catch (IOException ignored) {
            }
            return true;
        }
        return false;
    }

    public static boolean deleteSectionTextsFromHungarianSongAndCheckChange(Song song) {
        boolean change = deleteSectionTextsFromHungarianSongVersesAndCheckChange(song.getVerses());
        change = deleteTitleSectionAndCheckChange(song) || change;
        return change;
    }

    private static boolean deleteTitleSectionAndCheckChange(Song song) {
        List<SongVerse> songVerses = song.getVerses();
        if (!songVerses.isEmpty()) {
            SongVerse firstSongVerse = songVerses.get(0);
            if (firstSongVerse.getSectionType().equals(SectionType.CHORUS)) {
                return false;
            }
            String firstSongVerseText = firstSongVerse.getText();
            if (firstSongVerseText.split("\n").length > 1) {
                return false;
            }
            String songTitle = song.getTitle();
            if (Math.abs(songTitle.length() - firstSongVerseText.length()) > 2) {
                return false;
            }
            if (stripAccents(firstSongVerseText).equalsIgnoreCase(stripAccents(songTitle))) {
                List<SongVerse> songVersesByVerseOrder = song.getSongVersesByVerseOrder();
                List<SongVerse> newSongVerses = new ArrayList<>();
                for (SongVerse songVerse : songVersesByVerseOrder) {
                    if (!songVerse.equals(firstSongVerse)) {
                        newSongVerses.add(songVerse);
                    }
                }
                if (newSongVerses.size() == songVersesByVerseOrder.size() - 1) {
                    song.setVersesAndCheckVerseOderList(newSongVerses);
                    return true;
                } else {
                    System.out.println("newSongVerses.size() == songVersesByVerseOrder.size() - 1");
                    System.out.println(newSongVerses.size() + " == " + (songVersesByVerseOrder.size() - 1));
                }
            }
        }
        return false;
    }

    private static boolean deleteSectionTextsFromHungarianSongVersesAndCheckChange(List<SongVerse> songVerses) {
        boolean changed = false;
        for (SongVerse songVerse : songVerses) {
            String text = songVerse.getText();
            String newText = deleteChorusSectionIdentifierLine(text);
            songVerse.setText(newText);
            if (!newText.equals(text)) {
                songVerse.setSectionType(SectionType.CHORUS);
            }
            changed = isSongVerseChanged(changed, songVerse, text);
        }
        return changed;
    }

    private static String deleteChorusSectionIdentifierLine(String text) {
        text = deleteChorusesInLine(text);
        String[] lines = text.split("\n");
        StringBuilder newText = new StringBuilder();
        boolean firstLine = true;
        for (String line : lines) {
            String lowerCaseLetters = replaceAllOtherThenLetterAndNumber2(line).toLowerCase();
            if (isNotChorusSection(lowerCaseLetters)) {
                if (firstLine) {
                    firstLine = false;
                } else {
                    newText.append("\n");
                }
                newText.append(line);
            }
        }
        return newText.toString();
    }

    private static String deleteChorusesInLine(String text) {
        text = deleteChorusInLine(text, "refr?");
        text = deleteChorusInLine(text, "kar");
        return text;
    }

    private static String deleteChorusInLine(String text, String chorusText) {
        String noCase = "(?i)";
        String left = "^" + noCase;
        text = text.replaceAll(left + chorusText + "\\." + endingWithColon, "");
        text = text.replaceAll(left + chorusText + "\\." + refEnding, "");
        text = text.replaceAll(left + chorusText + endingWithColon, "");
        return text;
    }

    private static boolean isNotChorusSection(String lowerCaseLetters) {
        return switch (lowerCaseLetters) {
            case "kar", "refren", "refrén", "ref.", "refr.", "chorus" -> false;
            default -> true;
        };
    }

}
