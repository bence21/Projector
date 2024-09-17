package com.bence.projector.server.utils;

import com.bence.projector.server.backend.model.Language;
import com.bence.projector.server.backend.model.Song;
import com.bence.projector.server.backend.model.SongVerse;
import com.bence.projector.server.backend.repository.SongRepository;
import com.bence.projector.server.backend.service.LanguageService;
import com.bence.projector.server.utils.models.NormalizedWordBunch;
import com.bence.projector.server.utils.models.WordBunch;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import static com.bence.projector.server.utils.StringUtils.WHITE_SPACES;

public class SetLanguages {

    private static HashMap<String, Song> getStringSongHashMap(List<Song> allWithLanguage) {
        HashMap<String, Song> songHashMap = new HashMap<>();
        for (Song song : allWithLanguage) {
            songHashMap.put(song.getUuid(), song);
        }
        return songHashMap;
    }

    private static Map<Language, Collection<String>> getLanguageCollectionMap(List<Language> languages) {
        Map<Language, Collection<String>> languageMap = new HashMap<>();
        for (Language language : languages) {
            TreeSet<String> value = new TreeSet<>();
            languageMap.put(language, value);
        }
        return languageMap;
    }

    private static Map<Language, Map<String, WordBunch>> getLanguageCollectionMapWordBunch(List<Language> languages) {
        Map<Language, Map<String, WordBunch>> languageMaps = new HashMap<>();
        for (Language language : languages) {
            languageMaps.put(language, new HashMap<>());
        }
        return languageMaps;
    }

    public static void setLanguagesForUnknown(SongRepository songRepository, LanguageService languageService) {
        List<Language> languages = languageService.findAll();
        Iterable<Song> songs = songRepository.findAll();
        setLanguagesForSongs(songRepository, languages, songs);
    }

    public static void setLanguagesForSongs(SongRepository songRepository, List<Language> languages, Iterable<Song> songs) {
        List<Song> allWithLanguage = filterSongsContainingLanguage(songs);
        HashMap<String, Song> songHashMap = getStringSongHashMap(allWithLanguage);
        Map<Language, Collection<String>> languageMap = getLanguageCollectionMap(languages);
        setLanguagesForUnknownSongs(songRepository, languages, songs, songHashMap, languageMap);
    }

    @SuppressWarnings("unused")
    public static void printLanguagesWords(SongRepository songRepository, LanguageService languageService) {
        List<Language> languages = languageService.findAll();
        Iterable<Song> songRepositoryAll = songRepository.findAll();
        getLanguageWords(songRepositoryAll, languages, languages.get(10), false);
    }

    public static String getLanguageWords(Iterable<Song> songs, List<Language> languages, Language language, boolean table) {
        List<Song> allWithLanguage = filterSongsContainingLanguage(songs);
        Map<Language, Map<String, WordBunch>> languageMap = getLanguageCollectionMapWordBunch(languages);
        for (Song song : allWithLanguage) {
            if (!song.isDeleted()) {
                addWordByAlreadySetLanguage_wordBunch(languageMap, song);
            }
        }
        return getLanguageWords_wordBunch(language, languageMap, table);
    }

    @SuppressWarnings("unused")
    private static String getLanguageWords(Language language, Map<Language, Collection<String>> languageMap) {
        Collection<String> wordsCollection = languageMap.get(language);
        List<String> sortedWords = getList(wordsCollection);
        Collections.sort(sortedWords);
        String previous = null;
        StringBuilder s = new StringBuilder();
        for (String word : sortedWords) {
            if (!word.equals(previous)) {
                s.append(word).append("<br>");
            }
            previous = word;
        }
        return s.toString();
    }

    private static String getLanguageWords_wordBunch(Language language, Map<Language, Map<String, WordBunch>> languageMap, boolean table) {
        Map<String, WordBunch> wordsBunch = languageMap.get(language);
        List<WordBunch> wordBunches = getList(wordsBunch.values());
        wordBunches.sort(Comparator.comparing(WordBunch::getStripWordWord)
                .thenComparing(WordBunch::getNormalizedWord)
                .thenComparing(WordBunch::getWord));
        StringBuilder s = new StringBuilder();
        if (table) {
            s.append("<table>");
        }
        List<WordBunch> subList = wordBunches.subList(0, wordBunches.size());
        Map<String, NormalizedWordBunch> normalizedMap = calculateNormalizedWordBunchMap(subList);
        for (WordBunch wordBunch : subList) {
            boolean wordProblematic = false;
            if (table) {
                wordProblematic = wordBunch.isProblematic();
                s.append("<tr>");
                NormalizedWordBunch normalizedWordBunch = normalizedMap.get(wordBunch.getNormalizedWord());
                s.append("<td>").append(getProblematicStringForWordBunch(wordBunch, normalizedWordBunch)).append("</td>");
                s.append("<td>");
                if (wordProblematic) {
                    String colorHex = getRedIntesityColorString(normalizedWordBunch);
                    s.append("<span style=\"color: ").append(colorHex).append(";\">");
                }
            }
            s.append(wordBunch.getWord());
            if (table) {
                if (wordProblematic) {
                    s.append("</span>");
                }
                s.append("</td><td>");
            } else {
                s.append(" ");
            }
            s.append(wordBunch.getCount());
            if (table) {
                s.append("</td><td>").append(getHtmlSongLinkWithTitle(wordBunch.getSongs().get(0))).append("</td>");
                s.append("</tr>\n");
            } else {
                s.append("\n");
            }
        }
        if (table) {
            s.append("</table>");
        }
        return s.toString();
    }

    private static String getRedIntesityColorString(NormalizedWordBunch normalizedWordBunch) {
        double ratio = normalizedWordBunch.getRatio();
        int redIntensity = (int) (255 * (ratio / 100));
        return String.format("#%02X%02X%02X", redIntensity, 0, 0);
    }

    private static String getProblematicStringForWordBunch(WordBunch wordBunch, NormalizedWordBunch normalizedWordBunch) {
        if (wordBunch.isProblematic()) {
            return normalizedWordBunch.getRatioS() + "% !!";
        }
        return "";
    }

    private static Map<String, NormalizedWordBunch> calculateNormalizedWordBunchMap(List<WordBunch> subList) {
        Map<String, NormalizedWordBunch> normalizedMap = new HashMap<>();
        for (WordBunch wordBunch : subList) {
            String key = wordBunch.getNormalizedWord();
            NormalizedWordBunch normalizedWordBunch = normalizedMap.computeIfAbsent(key, k -> new NormalizedWordBunch());
            normalizedWordBunch.add(wordBunch);
        }
        for (NormalizedWordBunch normalizedWordBunch : normalizedMap.values()) {
            normalizedWordBunch.calculateBest();
        }
        return normalizedMap;
    }

    private static String getHtmlSongLinkWithTitle(Song song) {
        return "<a href=\"" + song.getSongLink() + "\" target=\"_blank\">" + song.getTitle() + "</a>";
    }

    private static <T> List<T> getList(Collection<T> stringCollection) {
        return new ArrayList<>(stringCollection);
    }

    private static List<Song> filterSongsContainingLanguage(Iterable<Song> songs) {
        ArrayList<Song> songArrayList = new ArrayList<>();
        for (Song song : songs) {
            if (song.getLanguage() != null && !song.isDeleted()) {
                songArrayList.add(song);
            }
        }
        return songArrayList;
    }

    private static void addWordByAlreadySettedLanguageSongs(Iterable<Song> songs, HashMap<String, Song> songHashMap, Map<Language, Collection<String>> languageMap) {
        for (Song song : songs) {
            Song song1 = songHashMap.get(song.getUuid());
            if (song1 != null) {
                addWordByAlreadySetLanguage(languageMap, song1);
            }
        }
    }

    private static void setLanguagesForUnknownSongs(SongRepository songRepository, List<Language> languages, Iterable<Song> songs, HashMap<String, Song> songHashMap, Map<Language, Collection<String>> languageMap) {
        addWordByAlreadySettedLanguageSongs(songs, songHashMap, languageMap);
        for (Song song : songs) {
            Song song1 = songHashMap.get(song.getUuid());
            if (song1 == null) {
                if (!(song.isUploaded() && song.isDeleted() && !song.isBackUp() && !song.isReviewerErased())) {
                    continue;
                }
                List<String> words = new ArrayList<>();
                addWordsInCollection(song, words);
                Map<Language, ContainsResult> countMap = new HashMap<>(languages.size());
                for (Language language1 : languages) {
                    Collection<String> wordsByLanguage = languageMap.get(language1);
                    ContainsResult containsResult = getContainsResult(words, wordsByLanguage);
                    countMap.put(language1, containsResult);
                }
                Map.Entry<Language, ContainsResult> max = getMax(countMap);
                printDetailsToConsoleAndSetLanguage(songRepository, languages, languageMap, song, max, countMap);
            }
        }
    }

    private static ContainsResult getContainsResult(List<String> words, Collection<String> wordsByLanguage) {
        Integer count = 0;
        Integer wordCount = 0;
        for (String word : words) {
            if (wordsByLanguage.contains(word)) {
                ++count;
            }
            ++wordCount;
        }
        ContainsResult containsResult = new ContainsResult();
        containsResult.setCount(count);
        containsResult.setWordCount(wordCount);
        return containsResult;
    }

    private static Map.Entry<Language, ContainsResult> getMax(Map<Language, ContainsResult> countMap) {
        Set<Map.Entry<Language, ContainsResult>> entries = countMap.entrySet();
        Map.Entry<Language, ContainsResult> max = new AbstractMap.SimpleEntry<>(null, new ContainsResult());
        for (Map.Entry<Language, ContainsResult> entry : entries) {
            if (entry.getValue().getRatio() > max.getValue().getRatio()) {
                max = entry;
            }
        }
        return max;
    }

    private static void printDetailsToConsoleAndSetLanguage(
            SongRepository songRepository,
            List<Language> languages,
            Map<Language, Collection<String>> languageMap,
            Song song,
            Map.Entry<Language, ContainsResult> max,
            Map<Language, ContainsResult> countMap) {
        System.out.println("======================================");
        System.out.println(song.getTitle());
        System.out.println(song.getUuid());
        Integer wordCount = null;
        if (max.getKey() != null) {
            wordCount = max.getValue().getWordCount();
        }
        String s;
        try {
            ContainsResult maxValue = max.getValue();
            if (
                    (isGoodEnoughRatioByWordCount(maxValue, 0.5, 160) ||
                            isGoodEnoughRatioByWordCount(maxValue, 0.6, 140) ||
                            isGoodEnoughRatioByWordCount(maxValue, 0.7, 120) ||
                            isGoodEnoughRatioByWordCount(maxValue, 0.8, 80) ||
                            isGoodEnoughRatioByWordCount(maxValue, 0.9, 40) ||
                            isGoodEnoughRatioByWordCount(maxValue, 0.98, 15) ||
                            (
                                    isGoodEnoughRatioByWordCount(maxValue, 0.5, 25) &&
                                            max.getKey().isFilipino()
                            )
                    ) && isCzechSlovakRatioGood(max, countMap, song)) {
                s = "yes";
                printDetails(max);
            } else {
                if (isSameLanguage(song, max)) {
                    s = "x";
                } else if ((wordCount != null && wordCount > 200)) {
                    printVerses(song);
                    printDetails(max);
                    System.out.print(">");
                    BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
                    s = br.readLine();
                } else {
                    printDetails(max);
                    s = "x";
                }
            }
            System.out.println(s);
            setLanguageFromConsole(songRepository, languages, languageMap, song, max, s);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static boolean isSameLanguage(Song song, Map.Entry<Language, ContainsResult> max) {
        Language songLanguage = song.getLanguage();
        Language maxLanguage = max.getKey();
        return songLanguage != null && songLanguage.equals(maxLanguage);
    }

    private static boolean isCzechSlovakRatioGood(Map.Entry<Language, ContainsResult> max, Map<Language, ContainsResult> countMap, Song song) {
        Language language = max.getKey();
        String uuid = language.getUuid();
        String czechUuid = "5d7bbbc70ca23e000465e286";
        String slovakUuid = "f5f2fe72-6b74-414b-a9a8-5f26451eb1a1";
        boolean isCzech = uuid.equals(czechUuid);
        if (!isCzech && !uuid.equals(slovakUuid)) {
            return true;
        }
        Language songLanguage = song.getLanguage();
        if (songLanguage != null && (songLanguage.isCzech() || songLanguage.isSlovak())) {
            return true;
        }
        String otherUuid;
        if (isCzech) {
            otherUuid = slovakUuid;
        } else {
            otherUuid = czechUuid;
        }
        Map.Entry<Language, ContainsResult> entryByLanguage = findEntryByLanguage(countMap, otherUuid);
        if (entryByLanguage == null) {
            return true;
        }
        return max.getValue().getRatio() / entryByLanguage.getValue().getRatio() > 1.1;
    }

    private static boolean isCzechAndSlovak(Language songLanguage, Language maxLanguage) {
        if (songLanguage == null || maxLanguage == null) {
            return false;
        }
        return (songLanguage.isCzech() && maxLanguage.isSlovak()) || (songLanguage.isSlovak() && maxLanguage.isCzech());
    }

    private static Map.Entry<Language, ContainsResult> findEntryByLanguage(Map<Language, ContainsResult> countMap, String uuid) {
        Set<Map.Entry<Language, ContainsResult>> entries = countMap.entrySet();
        for (Map.Entry<Language, ContainsResult> entry : entries) {
            if (entry.getKey().getUuid().equals(uuid)) {
                return entry;
            }
        }
        return null;
    }

    private static boolean isGoodEnoughRatioByWordCount(ContainsResult maxValue, double minRatio, int minWordCount) {
        return maxValue.getRatio() > minRatio && maxValue.getCount() > minWordCount;
    }

    private static void printDetails(Map.Entry<Language, ContainsResult> max) {
        Language maxKey = max.getKey();
        if (maxKey != null) {
            System.out.println("Language:   " + maxKey.getEnglishName());
            ContainsResult maxValue = max.getValue();
            System.out.println("Ratio:  " + maxValue.getRatio());
            System.out.println("Match count:  " + maxValue.getCount());
            System.out.println("Words:  " + maxValue.getWordCount());
        }
    }

    private static void printVerses(Song song) {
        for (SongVerse songVerse : song.getVerses()) {
            System.out.println(songVerse.getText());
            System.out.println();
        }
    }

    private static void setLanguageFromConsole(SongRepository songRepository, List<Language> languages, Map<Language, Collection<String>> languageMap, Song song, Map.Entry<Language, ContainsResult> max, String s) {
        switch (s) {
            case "yes":
                Language songLanguage = song.getLanguage();
                Language maxLanguage = max.getKey();
                if (songLanguage != null && songLanguage.isCebuano()) { // because is similar to Filipino
                    break;
                }
                if (isSameLanguage(song, max)) {
                    break;
                }
                if (isCzechAndSlovak(songLanguage, maxLanguage)) {
                    break;
                }
                setAndSaveLanguage(songRepository, languageMap, song, maxLanguage);
                break;
            case "english":
                Language language1 = languages.get(10);
                if (language1.getEnglishName().equals("English")) {
                    setAndSaveLanguage(songRepository, languageMap, song, language1);
                }
                break;
            case "spanish":
                Language language4 = languages.get(0);
                if (language4.getEnglishName().equals("Spanish")) {
                    setAndSaveLanguage(songRepository, languageMap, song, language4);
                }
                break;
            case "roman":
                Language language3 = languages.get(3);
                if (language3.getEnglishName().equals("Romanian")) {
                    setAndSaveLanguage(songRepository, languageMap, song, language3);
                }
                break;
            case "hungarian":
                Language language2 = languages.get(4);
                if (language2.getEnglishName().equals("Hungarian")) {
                    setAndSaveLanguage(songRepository, languageMap, song, language2);
                }
                break;
            case "german":
                Language language5 = languages.get(8);
                if (language5.getEnglishName().equals("German")) {
                    setAndSaveLanguage(songRepository, languageMap, song, language5);
                }
                break;
            case "swahili":
                Language language6 = languages.get(18);
                if (language6.getEnglishName().equals("Swahili")) {
                    setAndSaveLanguage(songRepository, languageMap, song, language6);
                }
                break;
        }
    }

    @SuppressWarnings("unused")
    private static void setAndSaveLanguage(SongRepository songRepository, Map<Language, Collection<String>> languageMap, Song song, Language language5) {
        song.setLanguage(language5);
        songRepository.save(song);
        //        addWordsInCollection(song, languageMap.get(song.getLanguage()));
    }

    private static void addWordByAlreadySetLanguage(Map<Language, Collection<String>> languageMap, Song song1) {
        Language language = song1.getLanguage();
        Collection<String> words = languageMap.get(language);
        addWordsInCollection(song1, words);
    }

    private static void addWordByAlreadySetLanguage_wordBunch(Map<Language, Map<String, WordBunch>> languageMap, Song song) {
        Language language = song.getLanguage();
        Map<String, WordBunch> words = languageMap.get(language);
        addWordsInCollection_wordBunch(song, words);
    }

    private static void addWordsInCollection_wordBunch(Song song, Map<String, WordBunch> wordsBunch) {
        Collection<String> wordsCollection = new ArrayList<>();
        addWordsInCollection(song, wordsCollection);
        for (String word : wordsCollection) {
            WordBunch wordBunch = wordsBunch.get(word);
            if (wordBunch == null) {
                wordBunch = new WordBunch();
                wordsBunch.put(word, wordBunch);
                wordBunch.setWord(word);
            }
            wordBunch.incCount();
            wordBunch.addSong(song);
        }
    }

    private static void addWordsInCollection(Song song, Collection<String> words) {
        for (SongVerse songVerse : song.getVerses()) {
            String[] split = songVerse.getText().split("[\\s\\n" + WHITE_SPACES + "]");
            for (String word : split) {
                // word = stripAccents(word.toLowerCase());
                word = word.replaceAll("(^[^\\p{L}]+|[^\\p{L}]+$)", "");
                if (word.isEmpty()) {
                    continue;
                }
                words.add(word);
            }
        }
    }
}
