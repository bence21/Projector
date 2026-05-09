package projector.controller.song.util;

import projector.model.Song;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Matches a pasted title string to songs in the current library slice (same idea as song search).
 */
public final class ScheduleSongMatcher {
    public static final double AUTO_MATCH_THRESHOLD = 0.82;

    public enum MatchStatus {
        MATCHED,
        AMBIGUOUS,
        NOT_FOUND
    }

    public record Result(Song song, MatchStatus status, List<Song> alternatives, double confidence) {
    }

    private ScheduleSongMatcher() {
    }

    public static Result match(String candidate, List<Song> songs) {
        if (candidate == null || candidate.isBlank()) {
            return new Result(null, MatchStatus.NOT_FOUND, List.of(), 0);
        }
        String c = TitleSimilarity.normalize(candidate);
        if (c.isEmpty()) {
            return new Result(null, MatchStatus.NOT_FOUND, List.of(), 0);
        }

        List<Song> exact = songs.stream()
                .filter(s -> s != null && TitleSimilarity.normalize(s.getStrippedTitle()).equals(c))
                .collect(Collectors.toList());
        List<Song> pool = exact.isEmpty()
                ? songs.stream()
                .filter(s -> s != null && substringMatch(c, s))
                .collect(Collectors.toList())
                : exact;

        if (pool.isEmpty()) {
            List<Song> ranked = TitleSimilarity.rankSongs(candidate, songs);
            if (ranked.isEmpty()) {
                return new Result(null, MatchStatus.NOT_FOUND, List.of(), 0);
            }
            Song best = ranked.get(0);
            double bestScore = TitleSimilarity.score(candidate, best);
            if (bestScore >= AUTO_MATCH_THRESHOLD) {
                return new Result(best, MatchStatus.MATCHED, ranked, bestScore);
            }
            return new Result(null, MatchStatus.NOT_FOUND, ranked, bestScore);
        }
        if (pool.size() == 1) {
            Song only = pool.get(0);
            double confidence = exact.isEmpty() ? TitleSimilarity.score(candidate, only) : 1.0;
            return new Result(only, MatchStatus.MATCHED, pool, confidence);
        }

        List<Song> favourites = pool.stream().filter(Song::isFavourite).toList();
        if (favourites.size() == 1) {
            Song favourite = favourites.get(0);
            double confidence = exact.isEmpty() ? TitleSimilarity.score(candidate, favourite) : 1.0;
            return new Result(favourite, MatchStatus.MATCHED, pool, confidence);
        }

        Song shortest = pickShortestTitle(pool);
        return new Result(shortest, MatchStatus.AMBIGUOUS, new ArrayList<>(pool), TitleSimilarity.score(candidate, shortest));
    }

    private static boolean substringMatch(String candidateNorm, Song song) {
        String t = TitleSimilarity.normalize(song.getStrippedTitle());
        if (t.isEmpty()) {
            return false;
        }
        return t.contains(candidateNorm) || candidateNorm.contains(t);
    }

    private static Song pickShortestTitle(List<Song> pool) {
        Song best = pool.get(0);
        int min = titleLen(best);
        for (int i = 1; i < pool.size(); i++) {
            Song s = pool.get(i);
            int len = titleLen(s);
            if (len < min) {
                min = len;
                best = s;
            }
        }
        return best;
    }

    private static int titleLen(Song s) {
        String t = s.getStrippedTitle();
        return t != null ? t.length() : 0;
    }
}
