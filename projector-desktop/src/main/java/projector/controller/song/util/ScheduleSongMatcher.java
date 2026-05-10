package projector.controller.song.util;

import projector.model.Language;
import projector.model.Song;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
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

    public record Result(Song song, MatchStatus status, List<Song> alternatives, double confidence,
                         boolean selectedFavourite, boolean overriddenToFavourite) {
    }

    private ScheduleSongMatcher() {
    }

    public static Result match(String candidate, List<Song> songs) {
        if (candidate == null || candidate.isBlank()) {
            return new Result(null, MatchStatus.NOT_FOUND, List.of(), 0, false, false);
        }
        String c = TitleSimilarity.normalize(candidate);
        if (c.isEmpty()) {
            return new Result(null, MatchStatus.NOT_FOUND, List.of(), 0, false, false);
        }

        List<Song> exact = songs.stream()
                .filter(s -> s != null && exactMatchByStrippedTitle(c, s))
                .collect(Collectors.toList());
        List<Song> pool = exact.isEmpty()
                ? songs.stream()
                .filter(s -> s != null && substringMatch(c, s))
                .collect(Collectors.toList())
                : exact;

        Result baseResult = resolveFromPool(candidate, songs, exact, pool);
        return applyFavouriteVersionGroupOverride(candidate, songs, baseResult);
    }

    private static Result resolveFromPool(String candidate, List<Song> songs, List<Song> exact, List<Song> pool) {
        if (pool.isEmpty()) {
            List<Song> ranked = TitleSimilarity.rankSongs(candidate, songs);
            if (ranked.isEmpty()) {
                return new Result(null, MatchStatus.NOT_FOUND, List.of(), 0, false, false);
            }
            Song best = ranked.get(0);
            double bestScore = TitleSimilarity.score(candidate, best);
            if (bestScore >= AUTO_MATCH_THRESHOLD) {
                return new Result(best, MatchStatus.MATCHED, ranked, bestScore, false, false);
            }
            return new Result(null, MatchStatus.NOT_FOUND, ranked, bestScore, false, false);
        }
        if (pool.size() == 1) {
            Song only = pool.get(0);
            double confidence = exact.isEmpty() ? TitleSimilarity.score(candidate, only) : 1.0;
            return new Result(only, MatchStatus.MATCHED, pool, confidence, false, false);
        }

        List<Song> favourites = pool.stream().filter(Song::isFavourite).toList();
        if (favourites.size() == 1) {
            Song favourite = favourites.get(0);
            double confidence = exact.isEmpty() ? TitleSimilarity.score(candidate, favourite) : 1.0;
            return new Result(favourite, MatchStatus.MATCHED, pool, confidence, false, false);
        }

        List<Song> rankedPool = TitleSimilarity.rankSongs(candidate, pool);
        Song best = rankedPool.get(0);
        return new Result(best, MatchStatus.AMBIGUOUS, rankedPool, TitleSimilarity.score(candidate, best), false, false);
    }

    private static Result applyFavouriteVersionGroupOverride(String candidate, List<Song> songs, Result baseResult) {
        Song selected = baseResult.song();
        if (selected == null) {
            return new Result(null, baseResult.status(), baseResult.alternatives(), baseResult.confidence(), false, false);
        }
        Song favouriteOverride = findFavouriteInSameVersionGroupAndLanguage(candidate, songs, selected);
        if (favouriteOverride == null || favouriteOverride == selected) {
            return new Result(selected, baseResult.status(), baseResult.alternatives(), baseResult.confidence(),
                    selected.isFavourite(), false);
        }
        List<Song> mergedAlternatives = withSelectedFirst(favouriteOverride, baseResult.alternatives());
        return new Result(favouriteOverride, baseResult.status(), mergedAlternatives,
                TitleSimilarity.score(candidate, favouriteOverride), true, true);
    }

    private static Song findFavouriteInSameVersionGroupAndLanguage(String candidate, List<Song> songs, Song selected) {
        String versionGroup = SongVersionGroupUtil.getVersionGroupOrUuid(selected);
        if (versionGroup == null) {
            return null;
        }
        Language selectedLanguage = selected.getLanguage();
        List<Song> candidates = songs.stream()
                .filter(Objects::nonNull)
                .filter(Song::isFavourite)
                .filter(song -> versionGroup.equals(SongVersionGroupUtil.getVersionGroupOrUuid(song)))
                .filter(song -> sameLanguage(selectedLanguage, song.getLanguage()))
                .toList();
        if (candidates.isEmpty()) {
            return null;
        }
        return TitleSimilarity.rankSongs(candidate, candidates).get(0);
    }

    private static boolean sameLanguage(Language left, Language right) {
        if (left == right) {
            return true;
        }
        if (left == null || right == null) {
            return false;
        }
        Long leftId = left.getId();
        Long rightId = right.getId();
        if (leftId != null && rightId != null) {
            return leftId.equals(rightId);
        }
        String leftUuid = left.getUuid();
        String rightUuid = right.getUuid();
        return leftUuid != null && !leftUuid.isBlank() && leftUuid.equals(rightUuid);
    }

    private static List<Song> withSelectedFirst(Song selected, List<Song> alternatives) {
        List<Song> merged = new ArrayList<>();
        merged.add(selected);
        for (Song alternative : alternatives) {
            if (alternative != selected) {
                merged.add(alternative);
            }
        }
        return merged;
    }

    private static boolean substringMatch(String candidateNorm, Song song) {
        String t = TitleSimilarity.normalize(song.getStrippedTitle());
        if (t.isEmpty()) {
            return false;
        }
        return t.contains(candidateNorm) || candidateNorm.contains(t);
    }

    private static boolean exactMatchByStrippedTitle(String candidateNorm, Song song) {
        String stripped = song.getStrippedTitle();
        if (stripped == null || stripped.isEmpty()) {
            return false;
        }
        return stripped.equals(candidateNorm);
    }

}
