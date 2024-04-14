package com.bence.projector.server.utils;

import com.bence.projector.server.backend.model.Language;
import com.bence.projector.server.backend.model.Song;
import com.bence.projector.server.backend.model.SongVerse;
import com.bence.projector.server.backend.repository.SongRepository;
import com.bence.projector.server.backend.service.LanguageService;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.Normalizer;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;

public class SetLanguages {
    private static final Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");

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

    public static void printLanguagesWords(SongRepository songRepository, LanguageService languageService) {
        List<Language> languages = languageService.findAll();
        Iterable<Song> songRepositoryAll = songRepository.findAll();
        printLanguageWords(songRepositoryAll, languages, languages.get(10));
    }

    public static void printLanguageWords(Iterable<Song> songs, List<Language> languages, Language language) {
        List<Song> allWithLanguage = filterSongsContainingLanguage(songs);
        Map<Language, Collection<String>> languageMap = getLanguageCollectionMap(languages);
        for (Song song : allWithLanguage) {
            if (!song.isDeleted()) {
                addWordByAlreadySettedLanguage(languageMap, song);
            }
        }
        printLanguageWords(language, languageMap);
    }

    private static void printLanguageWords(Language language, Map<Language, Collection<String>> languageMap) {
        Collection<String> wordsCollection = languageMap.get(language);
        List<String> sortedWords = getList(wordsCollection);
        Collections.sort(sortedWords);
        String previous = null;
        for (String word : sortedWords) {
            if (!word.equals(previous)) {
                System.out.println(word);
            }
            previous = word;
        }
    }

    private static List<String> getList(Collection<String> stringCollection) {
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
                addWordByAlreadySettedLanguage(languageMap, song1);
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

    private static void setAndSaveLanguage(SongRepository songRepository, Map<Language, Collection<String>> languageMap, Song song, Language language5) {
        song.setLanguage(language5);
        songRepository.save(song);
        //        addWordsInCollection(song, languageMap.get(song.getLanguage()));
    }

    private static void addWordByAlreadySettedLanguage(Map<Language, Collection<String>> languageMap, Song song1) {
        Language language = song1.getLanguage();
        Collection<String> words = languageMap.get(language);
        addWordsInCollection(song1, words);
    }

    private static void addWordsInCollection(Song song, Collection<String> words) {
        for (SongVerse songVerse : song.getVerses()) {
            String[] split = songVerse.getText().split("[\\s\\t\\n\\r]");
            for (String word : split) {
                word = stripAccents(word.toLowerCase());
                if (word.isEmpty()) {
                    continue;
                }
                words.add(word);
            }
        }
    }

    private static String stripAccents(String word) {
        String nfdNormalizedString = Normalizer.normalize(word, Normalizer.Form.NFD);
        word = pattern.matcher(nfdNormalizedString).replaceAll("");
        word = word.replaceAll("[^a-zA-Z0-9]", "");
        return word;
    }
}
