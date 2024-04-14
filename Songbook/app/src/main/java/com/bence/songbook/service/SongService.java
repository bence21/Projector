package com.bence.songbook.service;

import com.bence.songbook.models.Song;
import com.bence.songbook.models.SongVerse;
import com.bence.songbook.ui.utils.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SongService {
    public static List<Song> findAllSimilar(Song song, List<Song> all) {
        List<Song> similar = new ArrayList<>();
        String text = getText(song);
        String songId = song.getUuid();
        String regex = "[.,;?_\"'\\n!:/|\\\\ ]";
        String[] split = text.split(regex);
        int wordsLength = split.length;
        HashMap<String, Boolean> wordHashMap = new HashMap<>(wordsLength);
        for (String word : split) {
            wordHashMap.put(word.toLowerCase(), true);
        }
        int size = wordHashMap.keySet().size();
        HashMap<String, Boolean> hashMap = new HashMap<>(size);
        for (Song databaseSong : all) {
            if ((songId != null && databaseSong.getUuid().equals(songId)) || databaseSong.isDeleted()) {
                continue;
            }
            String secondText = getText(databaseSong);
            String[] words = secondText.split(regex);
            hashMap.clear();
            int count = 0;
            for (String word : words) {
                hashMap.put(word.toLowerCase(), true);
            }
            for (String word : hashMap.keySet()) {
                if (wordHashMap.containsKey(word)) {
                    ++count;
                }
            }
            double x = count;
            x /= size;
            if (x > 0.5) {
                x = StringUtils.highestCommonStringInt(text, secondText);
                x = x / text.length();
                if (x > 0.55) {
                    double y;
                    y = StringUtils.highestCommonStringInt(text, secondText);
                    y = y / secondText.length();
                    if (y > 0.55) {
                        similar.add(databaseSong);
                    }
                }
            }
        }
        return similar;
    }

    private static String getText(Song song) {
        ArrayList<SongVerse> verseList = new ArrayList<>(song.getVerses().size());
        final List<SongVerse> verses = song.getVerses();
        SongVerse chorus = null;
        int size = verses.size();
        for (int i = 0; i < size; ++i) {
            SongVerse songVerse = verses.get(i);
            verseList.add(songVerse);
            if (songVerse.isChorus()) {
                chorus = songVerse;
            } else if (chorus != null) {
                if (i + 1 < size) {
                    if (!verses.get(i + 1).isChorus()) {
                        verseList.add(chorus);
                    }
                } else {
                    verseList.add(chorus);
                }
            }
        }
        StringBuilder text = new StringBuilder();
        for (SongVerse songVerse : verseList) {
            text.append(songVerse.getText()).append(" ");
        }
        return text.toString();
    }

}
