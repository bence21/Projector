package projector.controller.song.util;

import projector.model.Song;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import static projector.utils.StringUtils.stripAccents;

public final class TitleSimilarity {

    private TitleSimilarity() {
    }

    public static String normalize(String input) {
        if (input == null) {
            return "";
        }
        return stripAccents(input).toLowerCase(Locale.ROOT).trim().replaceAll("\\s+", " ");
    }

    public static double score(String candidate, Song song) {
        if (song == null) {
            return 0;
        }
        String songTitle = song.getStrippedTitle();
        if (songTitle == null || songTitle.isBlank()) {
            songTitle = song.getTitle();
        }
        return score(candidate, songTitle);
    }

    public static double score(String candidate, String title) {
        String left = normalize(candidate);
        String right = normalize(title);
        if (left.isEmpty() || right.isEmpty()) {
            return 0;
        }
        if (left.equals(right)) {
            return 1;
        }
        int distance = levenshtein(left, right);
        int maxLen = Math.max(left.length(), right.length());
        //noinspection ConstantValue
        if (maxLen == 0) {
            return 0;
        }
        double similarity = 1.0 - ((double) distance / (double) maxLen);
        if (similarity < 0) {
            return 0;
        }
        if (similarity > 1) {
            return 1;
        }
        return similarity;
    }

    public static List<Song> rankSongs(String candidate, List<Song> songs) {
        List<Song> ranked = new ArrayList<>();
        for (Song song : songs) {
            if (song != null) {
                ranked.add(song);
            }
        }
        ranked.sort(Comparator
                .comparingDouble((Song s) -> score(candidate, s)).reversed()
                .thenComparingInt(s -> {
                    String title = s.getTitle();
                    return title != null ? title.length() : Integer.MAX_VALUE;
                })
                .thenComparing(s -> {
                    String title = s.getTitle();
                    return title != null ? title : "";
                }));
        return ranked;
    }

    private static int levenshtein(String a, String b) {
        int aLen = a.length();
        int bLen = b.length();
        int[] prev = new int[bLen + 1];
        int[] curr = new int[bLen + 1];
        for (int j = 0; j <= bLen; j++) {
            prev[j] = j;
        }
        for (int i = 1; i <= aLen; i++) {
            curr[0] = i;
            char ca = a.charAt(i - 1);
            for (int j = 1; j <= bLen; j++) {
                int cost = (ca == b.charAt(j - 1)) ? 0 : 1;
                int insert = curr[j - 1] + 1;
                int delete = prev[j] + 1;
                int replace = prev[j - 1] + cost;
                curr[j] = Math.min(Math.min(insert, delete), replace);
            }
            int[] tmp = prev;
            prev = curr;
            curr = tmp;
        }
        return prev[bLen];
    }
}
