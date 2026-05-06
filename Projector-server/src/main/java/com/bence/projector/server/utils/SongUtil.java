package com.bence.projector.server.utils;

import com.bence.projector.common.model.SectionType;
import com.bence.projector.server.backend.model.Language;
import com.bence.projector.server.backend.model.Song;
import com.bence.projector.server.backend.model.SongCollection;
import com.bence.projector.server.backend.model.SongCollectionElement;
import com.bence.projector.server.backend.model.SongLink;
import com.bence.projector.server.backend.model.SongVerse;
import com.bence.projector.server.backend.model.User;
import com.bence.projector.server.backend.repository.SongLinkRepository;
import com.bence.projector.server.backend.service.LanguageService;
import com.bence.projector.server.backend.service.SongCollectionElementService;
import com.bence.projector.server.backend.service.SongCollectionService;
import com.bence.projector.server.backend.service.SongLinkService;
import com.bence.projector.server.backend.service.SongPublicScope;
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

    /**
     * One language’s songs for the mark-similar batch: version-group pool from {@link SongService}, filtered by visibility in SQL.
     */
    private static List<Song> loadSongsForSimilarBatch(SongService songService, Language language, SongPublicScope visibility) {
        Collection<Song> raw = songService.getSongsByLanguageForSimilarWithVersionGroup(language, visibility);
        return raw == null ? new ArrayList<>() : new ArrayList<>(raw);
    }

    /**
     * Automatic similarity merge skips merging when the two version groups would form a larger combined
     * cluster than this (a SongLink is created instead for human review).
     */
    private static final int MAX_COMBINED_VERSION_GROUP_SIZE_FOR_AUTO_MERGE = 50;

    /**
     * Near-duplicate path only moves collection rows off the loser when it is this young (avoids touching long-lived catalog songs).
     */
    private static final long MAX_LOSER_AGE_MS_FOR_COLLECTION_TRANSFER = 24L * 60 * 60 * 1000;

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
    public static void markSimilarSongsAndSet(SongService songService, LanguageService languageService, SongLinkRepository songLinkRepository, SongLinkService songLinkService,
                                              SongCollectionService songCollectionService, SongCollectionElementService songCollectionElementService) {
        markSimilarSongsAndSet(songService, languageService, songLinkRepository, songLinkService, songCollectionService, songCollectionElementService, SongPublicScope.PUBLIC, null);
    }

    @SuppressWarnings("unused")
    public static void markSimilarSongsAndSet(SongService songService, LanguageService languageService, SongLinkRepository songLinkRepository, SongLinkService songLinkService,
                                              SongCollectionService songCollectionService, SongCollectionElementService songCollectionElementService,
                                              SongPublicScope visibility) {
        markSimilarSongsAndSet(songService, languageService, songLinkRepository, songLinkService, songCollectionService, songCollectionElementService, visibility, null);
    }

    @SuppressWarnings("unused")
    public static void markSimilarSongsAndSet(SongService songService, LanguageService languageService, SongLinkRepository songLinkRepository, SongLinkService songLinkService,
                                              SongCollectionService songCollectionService, SongCollectionElementService songCollectionElementService,
                                              SongPublicScope visibility, Date nearDuplicateCutoffDayStart) {
        List<Language> languages = languageService.findAll();
        int languageCount = languages.size();
        int[] songTotalPerLanguage = new int[languageCount];
        int grandTotalSongs = 0;
        List<List<Song>> songsPerLanguage = new ArrayList<>(languageCount);
        for (int li = 0; li < languageCount; li++) {
            List<Song> batch = loadSongsForSimilarBatch(songService, languages.get(li), visibility);
            songsPerLanguage.add(batch);
            int c = batch.size();
            songTotalPerLanguage[li] = c;
            grandTotalSongs += c;
        }
        long jobStartMs = System.currentTimeMillis();
        int songsCompletedBeforeLanguage = 0;
        for (int li = 0; li < languageCount; li++) {
            Language language = languages.get(li);
            markSimilarSongsAndSetForLanguage(songService, language, songLinkRepository, songLinkService, songCollectionService, songCollectionElementService,
                    li + 1, languageCount, songsCompletedBeforeLanguage, grandTotalSongs, jobStartMs, songsPerLanguage.get(li), visibility, nearDuplicateCutoffDayStart);
            songsCompletedBeforeLanguage += songTotalPerLanguage[li];
        }
    }

    /**
     * Runs {@link #markSimilarSongsAndSetForLanguage} for a single language (admin / per-language batch).
     */
    public static void markSimilarSongsAndSet(SongService songService, Language language,
                                              SongLinkRepository songLinkRepository, SongLinkService songLinkService,
                                              SongCollectionService songCollectionService,
                                              SongCollectionElementService songCollectionElementService) {
        markSimilarSongsAndSet(songService, language, songLinkRepository, songLinkService, songCollectionService, songCollectionElementService, SongPublicScope.PUBLIC, null);
    }

    public static void markSimilarSongsAndSet(SongService songService, Language language,
                                              SongLinkRepository songLinkRepository, SongLinkService songLinkService,
                                              SongCollectionService songCollectionService,
                                              SongCollectionElementService songCollectionElementService,
                                              SongPublicScope visibility) {
        markSimilarSongsAndSet(songService, language, songLinkRepository, songLinkService, songCollectionService, songCollectionElementService, visibility, null);
    }

    public static void markSimilarSongsAndSet(SongService songService, Language language,
                                              SongLinkRepository songLinkRepository, SongLinkService songLinkService,
                                              SongCollectionService songCollectionService,
                                              SongCollectionElementService songCollectionElementService,
                                              SongPublicScope visibility, Date nearDuplicateCutoffDayStart) {
        List<Song> songs = loadSongsForSimilarBatch(songService, language, visibility);
        long jobStartMs = System.currentTimeMillis();
        markSimilarSongsAndSetForLanguage(songService, language, songLinkRepository, songLinkService, songCollectionService, songCollectionElementService,
                1, 1, 0, songs.size(), jobStartMs, songs, visibility, nearDuplicateCutoffDayStart);
    }

    private static void markSimilarSongsAndSetForLanguage(SongService songService, Language language, SongLinkRepository songLinkRepository, SongLinkService songLinkService,
                                                          SongCollectionService songCollectionService, SongCollectionElementService songCollectionElementService,
                                                          int languageIndex1Based, int languageCount, int songsBeforeThisLanguage, int grandTotalSongs, long jobStartMs,
                                                          List<Song> songs, SongPublicScope batchVisibility, Date nearDuplicateCutoffDayStart) {
        Date startDate = new Date();
        long languageStartMs = startDate.getTime();
        String languageLabel = describeLanguageForProgress(language, languageIndex1Based, languageCount);
        int n = songs.size();
        List<Song> similaritySearchPool = batchVisibility == SongPublicScope.PUBLIC
                ? songs
                : loadSongsForSimilarBatch(songService, language, SongPublicScope.PUBLIC);
        System.out.println("[markSimilarSongs] " + languageLabel + " — " + n + " songs in batch (grand total across languages: " + grandTotalSongs + "); "
                + "similarity pool: " + similaritySearchPool.size() + " public songs");
        SimilarBatchProgress progress = new SimilarBatchProgress();
        int i = 0;
        for (Song songForSimilar : songs) {
            ++i;
            List<Song> similarSongs = songService.findAllSimilarSongsForSong(songForSimilar, false, similaritySearchPool, true);
            markSimilarSongsAndSetForSong(songForSimilar, similarSongs, songLinkRepository, songService, songLinkService, songCollectionService, songCollectionElementService, nearDuplicateCutoffDayStart);
            logSimilarBatchProgress(progress, i, n, songsBeforeThisLanguage, grandTotalSongs, languageStartMs, jobStartMs, languageLabel);
        }
        long now = System.currentTimeMillis();
        System.out.println("[markSimilarSongs] " + languageLabel + " — done. Elapsed seconds (this language): " + (now - languageStartMs) / 1000);
    }

    private static String describeLanguageForProgress(Language language, int languageIndex1Based, int languageCount) {
        String english = language != null && language.getEnglishName() != null ? language.getEnglishName() : "?";
        String suffix = "";
        if (language != null && language.getNativeName() != null && !language.getNativeName().isBlank()
                && !language.getNativeName().equalsIgnoreCase(english)) {
            suffix = " (" + language.getNativeName() + ")";
        }
        return "Language " + languageIndex1Based + "/" + languageCount + ": " + english + suffix;
    }

    private static final class SimilarBatchProgress {
        private int lastPrintedLangPercent = -1;
        private int lastPrintedOverallPercent = -1;
    }

    private static void logSimilarBatchProgress(SimilarBatchProgress state, int i, int n, int songsBeforeThisLanguage, int grandTotalSongs,
                                                long languageStartMs, long jobStartMs, String languageLabel) {
        if (n <= 0) {
            return;
        }
        int langPct = (int) ((double) (i * 100) / n);
        int overallPct = grandTotalSongs <= 0 ? 0 : (int) ((double) ((songsBeforeThisLanguage + i) * 100) / grandTotalSongs);
        if (langPct == state.lastPrintedLangPercent && overallPct == state.lastPrintedOverallPercent) {
            return;
        }
        state.lastPrintedLangPercent = langPct;
        state.lastPrintedOverallPercent = overallPct;
        Date date = new Date();
        long dateTime = date.getTime();
        double remainingLangMs = (double) (n - i) / i * (dateTime - languageStartMs);
        double remainingLangSeconds = (long) (remainingLangMs / 100) / 10.0;
        double remainingOverallMs = 0;
        double remainingOverallSeconds = 0;
        int globalIndex = songsBeforeThisLanguage + i;
        if (grandTotalSongs > 0 && globalIndex > 0) {
            remainingOverallMs = (double) (grandTotalSongs - globalIndex) / globalIndex * (dateTime - jobStartMs);
            remainingOverallSeconds = (long) (remainingOverallMs / 100) / 10.0;
        }
        System.out.println("[" + languageLabel + "] "
                + "this language " + langPct + "% | overall " + overallPct + "%\t"
                + "rem. s (this lang): " + remainingLangSeconds + "\t rem. s (all langs): " + remainingOverallSeconds + "\t"
                + "est. finish this lang: " + new Date((long) (dateTime + remainingLangMs)) + "\t"
                + "est. finish job: " + (grandTotalSongs > 0 && globalIndex > 0 ? new Date((long) (dateTime + remainingOverallMs)) : "—") + "\t"
                + date);
    }

    private static void markSimilarSongsAndSetForSong(Song songForSimilar, List<Song> similarSongs, SongLinkRepository songLinkRepository, SongService songService, SongLinkService songLinkService,
                                                      SongCollectionService songCollectionService, SongCollectionElementService songCollectionElementService, Date nearDuplicateCutoffDayStart) {
        if (similarSongs == null || similarSongs.isEmpty()) {
            return;
        }
        List<SongLink> songLinks = null;
        for (Song similarSong : similarSongs) {
            //noinspection ConstantValue
            handleSimilarSong(songForSimilar, similarSong, songLinks, songService, songLinkService, songLinkRepository, songCollectionService, songCollectionElementService, nearDuplicateCutoffDayStart);
        }
    }

    private static void handleSimilarSong(Song songForSimilar, Song similarSong, List<SongLink> songLinks, SongService songService, SongLinkService songLinkService, SongLinkRepository songLinkRepository,
                                          SongCollectionService songCollectionService, SongCollectionElementService songCollectionElementService, Date nearDuplicateCutoffDayStart) {
        Song loadedSongForSimilar = songService.findOneByUuid(songForSimilar.getUuid());
        Song loadedSimilarSong = songService.findOneByUuid(similarSong.getUuid());
        handleSimilarLoadedSong(loadedSongForSimilar, loadedSimilarSong, songLinks, songService, songLinkService, similarSong.getSimilarRatio(), songLinkRepository, songCollectionService, songCollectionElementService, nearDuplicateCutoffDayStart);
    }

    /**
     * Package-visible for tests in {@code com.bence.projector.server.utils}.
     */
    static void handleSimilarLoadedSong(Song loadedSongForSimilar, Song loadedSimilarSong, List<SongLink> songLinks, SongService songService, SongLinkService songLinkService, double percentage, SongLinkRepository songLinkRepository,
                                        SongCollectionService songCollectionService, SongCollectionElementService songCollectionElementService, Date nearDuplicateCutoffDayStart) {
        if (loadedSongForSimilar == null || loadedSimilarSong == null) {
            return;
        }
        if (loadedSongForSimilar.isSameVersionGroup(loadedSimilarSong)) {
            return;
        }
        boolean pub1 = loadedSongForSimilar.isPublic();
        boolean pub2 = loadedSimilarSong.isPublic();

        if (percentage >= 0.99) {
            if (pub1 != pub2) {
                Song survivor = pub1 ? loadedSongForSimilar : loadedSimilarSong;
                Song loser = pub1 ? loadedSimilarSong : loadedSongForSimilar;
                Song survivorManaged = songService.findOneByUuid(survivor.getUuid());
                if (survivorManaged != null) {
                    survivor = survivorManaged;
                }
                if (isLoserEligibleForNearDuplicateCollectionTransfer(loser, nearDuplicateCutoffDayStart)) {
                    transferSongCollectionElements(loser, survivor, songCollectionService, songCollectionElementService);
                    removeNearDuplicateLoserSong(loser, songService);
                } else {
                    warnSkippingNearDuplicateMergeForOldLoser(loser, survivor, nearDuplicateCutoffDayStart);
                }
                return;
            }
            if (pub1) {
                mergeSongVersionGroupOrSongLink(loadedSongForSimilar, loadedSimilarSong, songService, songLinks, songLinkService, songLinkRepository);
                return;
            }
            songLinkForSimilar(loadedSongForSimilar, loadedSimilarSong, songLinks, songLinkService, songLinkRepository);
        } else if (percentage > 0.9) {
            mergeSongVersionGroupOrSongLink(loadedSongForSimilar, loadedSimilarSong, songService, songLinks, songLinkService, songLinkRepository);
        } else {
            songLinkForSimilar(loadedSongForSimilar, loadedSimilarSong, songLinks, songLinkService, songLinkRepository);
        }
    }

    private static void mergeSongVersionGroupOrSongLink(Song loadedSongForSimilar, Song loadedSimilarSong, SongService songService, List<SongLink> songLinks, SongLinkService songLinkService, SongLinkRepository songLinkRepository) {
        if (!mergeSongVersionGroup(loadedSongForSimilar, loadedSimilarSong, songService, MAX_COMBINED_VERSION_GROUP_SIZE_FOR_AUTO_MERGE)) {
            songLinkForSimilar(loadedSongForSimilar, loadedSimilarSong, songLinks, songLinkService, songLinkRepository);
        }
    }

    /**
     * Eligibility for transferring collection memberships and deleting the non-public loser in the near-duplicate path ({@literal >=} 99%).
     * When {@code nearDuplicateCutoffDayStart} is non-null, Loser{@code createdDate} must be {@code >=} that instant (start of the chosen calendar day in the JVM default zone).
     * When null, the previous rule applies: loser must be younger than {@link #MAX_LOSER_AGE_MS_FOR_COLLECTION_TRANSFER}.
     */
    static boolean isLoserEligibleForNearDuplicateCollectionTransfer(Song loser, Date nearDuplicateCutoffDayStart) {
        if (loser == null) {
            return false;
        }
        Date created = loser.getCreatedDate();
        if (created == null) {
            return false;
        }
        if (nearDuplicateCutoffDayStart != null) {
            return !created.before(nearDuplicateCutoffDayStart);
        }
        return new Date().getTime() - created.getTime() < MAX_LOSER_AGE_MS_FOR_COLLECTION_TRANSFER;
    }

    static boolean isLoserRecentEnoughForCollectionTransfer(Song loser) {
        return isLoserEligibleForNearDuplicateCollectionTransfer(loser, null);
    }

    private static void warnSkippingNearDuplicateMergeForOldLoser(Song loser, Song survivor, Date nearDuplicateCutoffDayStart) {
        String loserTitle = loser.getTitle() != null ? loser.getTitle() : "";
        String survivorTitle = survivor.getTitle() != null ? survivor.getTitle() : "";
        String reason = nearDuplicateCutoffDayStart != null
                ? "(loser created before cutoff " + nearDuplicateCutoffDayStart + "; no collection transfer / delete). "
                : "(loser older than 1 day; no collection transfer / delete). ";
        System.out.println("[SongUtil] Skipped near-duplicate merge " + reason
                + "loser=" + loser.getUuid() + " \"" + loserTitle + "\" created=" + loser.getCreatedDate()
                + " survivor=" + survivor.getUuid() + " \"" + survivorTitle + "\"");
    }

    /**
     * Reassigns every {@link SongCollectionElement} that points at {@code loser} so it points at {@code survivor}
     * (same rows, new song FK). A song may appear in many collections; each membership row is updated in place.
     * Package-visible for tests.
     */
    static void transferSongCollectionElements(Song loser, Song survivor, SongCollectionService songCollectionService, SongCollectionElementService songCollectionElementService) {
        if (loser == null || survivor == null || loser.getId() == null || loser.getUuid() == null || survivor.getUuid() == null) {
            return;
        }
        String loserUuid = loser.getUuid();
        List<SongCollection> collections = songCollectionService.findAllBySong(loser);
        if (collections == null || collections.isEmpty()) {
            return;
        }
        Date touch = new Date();
        for (SongCollection collection : collections) {
            for (SongCollectionElement loserEl : collection.getElementsForSongUuid(loserUuid)) {
                loserEl.setSong(survivor);
                songCollectionElementService.save(loserEl);
            }
            collection.setModifiedDate(touch);
            songCollectionService.saveWithoutForeign(collection);
        }
    }

    /**
     * After collection membership was moved off the loser, permanently delete it (including queue-style soft-deleted uploads).
     */
    private static void removeNearDuplicateLoserSong(Song loser, SongService songService) {
        if (loser == null || loser.getUuid() == null) {
            return;
        }
        songService.deleteByUuid(loser.getUuid());
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
