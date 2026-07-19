package projector.utils.compare;

import javafx.scene.text.TextFlow;
import projector.model.Song;
import projector.model.SongVerse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class SongCompareEngine {

    /** Minimum word-overlap score to treat verses as the same content despite split/whitespace differences. */
    static final double WORD_SIMILARITY_THRESHOLD = 0.65;
    /** Whole-song wording below this is treated as truly different (hint-worthy). */
    static final double WHOLE_SONG_SIMILARITY_THRESHOLD = 0.55;
    private static final int MAX_CONCAT_VERSES = 4;

    private SongCompareEngine() {
    }

    public static void renderDiff(TextFlow textFlow, String originalText, String otherNormalizedSource,
                                  CompareSongsSettings settings) {
        if (originalText == null) {
            textFlow.getChildren().clear();
            return;
        }
        CompareNormalizeOptions options = settings.getEffectiveNormalizeOptions();
        String left = CompareNormalizeUtil.repeatChorusText(originalText, settings.isRepeatChorus());
        String right = CompareNormalizeUtil.repeatChorusText(otherNormalizedSource, settings.isRepeatChorus());
        CompareDiffHighlighter.renderAligned(textFlow, left, right, options);
    }

    public static List<VerseCompareEntry> buildVerseEntries(Song originalSong, Song changedSong,
                                                            CompareSongsSettings settings) {
        List<VerseCompareEntry> entries = new ArrayList<>();
        if (originalSong == null || changedSong == null) {
            return entries;
        }
        List<SongVerse> leftVerses = originalSong.getVerses();
        List<SongVerse> rightVerses = changedSong.getVerses();
        int maxLen = Math.max(leftVerses.size(), rightVerses.size());
        for (int i = 0; i < maxLen; i++) {
            SongVerse leftVerse = i < leftVerses.size() ? leftVerses.get(i) : null;
            SongVerse rightVerse = i < rightVerses.size() ? rightVerses.get(i) : null;
            String leftText = leftVerse != null ? leftVerse.getText() : "";
            String rightText = rightVerse != null ? rightVerse.getText() : "";
            VerseDiffKind diffKind = classifyVersePair(leftText, rightText, leftVerses, rightVerses, i, settings);
            entries.add(new VerseCompareEntry(i, diffKind));
        }
        return entries;
    }

    public static int countContentDifferences(List<VerseCompareEntry> entries) {
        int count = 0;
        for (VerseCompareEntry entry : entries) {
            if (entry.getDiffKind() == VerseDiffKind.CONTENT_DIFF
                    || entry.getDiffKind() == VerseDiffKind.ONLY_LEFT
                    || entry.getDiffKind() == VerseDiffKind.ONLY_RIGHT) {
                ++count;
            }
        }
        return count;
    }

    public static int countOrderOnlyDifferences(List<VerseCompareEntry> entries) {
        int count = 0;
        for (VerseCompareEntry entry : entries) {
            if (entry.isOrderOnly()) {
                ++count;
            }
        }
        return count;
    }

    public static boolean hasVerseOrderListDifference(Song originalSong, Song changedSong) {
        if (originalSong == null || changedSong == null) {
            return false;
        }
        List<Short> leftOrder = originalSong.getVerseOrderList();
        List<Short> rightOrder = changedSong.getVerseOrderList();
        return !Objects.equals(leftOrder, rightOrder);
    }

    public static boolean isSectionOrderOnlyDifference(Song originalSong, Song changedSong,
                                                       List<VerseCompareEntry> entries) {
        if (countContentDifferences(entries) > 0) {
            return false;
        }
        return countOrderOnlyDifferences(entries) > 0 || hasVerseOrderListDifference(originalSong, changedSong);
    }

    private static VerseDiffKind classifyVersePair(String leftText, String rightText,
                                                   List<SongVerse> leftVerses, List<SongVerse> rightVerses,
                                                   int index, CompareSongsSettings settings) {
        boolean hasLeft = index < leftVerses.size();
        boolean hasRight = index < rightVerses.size();
        if (!hasLeft && hasRight) {
            return findMatchingVerseIndex(rightText, leftVerses, -1, settings) >= 0
                    ? VerseDiffKind.ORDER_ONLY : VerseDiffKind.ONLY_RIGHT;
        }
        if (hasLeft && !hasRight) {
            return findMatchingVerseIndex(leftText, rightVerses, -1, settings) >= 0
                    ? VerseDiffKind.ORDER_ONLY : VerseDiffKind.ONLY_LEFT;
        }
        if (!hasLeft) {
            return VerseDiffKind.MATCH;
        }
        if (textsEqual(leftText, rightText, settings)) {
            return VerseDiffKind.MATCH;
        }
        int partnerRight = findMatchingVerseIndex(leftText, rightVerses, index, settings);
        if (partnerRight >= 0) {
            return VerseDiffKind.ORDER_ONLY;
        }
        int partnerLeft = findMatchingVerseIndex(rightText, leftVerses, index, settings);
        if (partnerLeft >= 0) {
            return VerseDiffKind.ORDER_ONLY;
        }
        return VerseDiffKind.CONTENT_DIFF;
    }

    /**
     * Finds a matching verse by exact normalized text equality.
     * Prefer {@link #findBestMatchingVerseIndex} for swap focus when verse splits may differ.
     */
    public static int findMatchingVerseIndex(String text, List<SongVerse> verses, int excludeIndex,
                                             CompareSongsSettings settings) {
        if (text == null || text.isEmpty() || verses == null) {
            return -1;
        }
        for (int i = 0; i < verses.size(); i++) {
            if (i == excludeIndex) {
                continue;
            }
            SongVerse verse = verses.get(i);
            if (verse == null) {
                continue;
            }
            if (textsEqual(text, verse.getText(), settings)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Finds the best matching verse by exact text, then by word/content similarity,
     * including matches across consecutive verses that only differ by split boundaries.
     * Returns -1 when no good match is found (caller should fall back to index).
     */
    public static int findBestMatchingVerseIndex(String text, List<SongVerse> verses, int excludeIndex,
                                                 CompareSongsSettings settings) {
        return findBestMatchingVerseIndexFrom(text, verses, 0, excludeIndex, settings);
    }

    /**
     * Prefer the occurrence of {@code currentVerseText} that continues reading after
     * {@code alreadyReadText} (so a later chorus is chosen over an earlier identical one).
     */
    public static int findContinueReadingVerseIndex(String alreadyReadText, String currentVerseText,
                                                    List<SongVerse> verses, CompareSongsSettings settings) {
        if (verses == null || verses.isEmpty()) {
            return -1;
        }
        int fromIndex = estimateVerseIndexAfterAlreadyRead(alreadyReadText, verses, settings);
        int match = findBestMatchingVerseIndexFrom(currentVerseText, verses, fromIndex, -1, settings);
        if (match >= 0) {
            return match;
        }
        // Soft fallback: still prefer later occurrences when scores tie.
        return findBestMatchingVerseIndexFrom(currentVerseText, verses, 0, -1, settings);
    }

    /**
     * Estimates the first counterpart verse index at/after the already-sung content.
     */
    static int estimateVerseIndexAfterAlreadyRead(String alreadyReadText, List<SongVerse> verses,
                                                  CompareSongsSettings settings) {
        List<String> alreadyWords = words(alreadyReadText, settings);
        if (alreadyWords.isEmpty() || verses == null || verses.isEmpty()) {
            return 0;
        }
        StringBuilder prefix = new StringBuilder();
        double bestScore = -1.0;
        int bestEndExclusive = 0;
        for (int i = 0; i < verses.size(); i++) {
            SongVerse verse = verses.get(i);
            if (verse == null) {
                continue;
            }
            if (!prefix.isEmpty()) {
                prefix.append('\n');
            }
            prefix.append(verse.getText());
            double score = wordSimilarity(alreadyWords, words(prefix.toString(), settings));
            if (score >= bestScore) {
                bestScore = score;
                bestEndExclusive = i + 1;
            }
            // Once we have a strong cover of already-read words, stop extending.
            if (score >= WORD_SIMILARITY_THRESHOLD
                    && containmentOfLeftInRight(alreadyWords, words(prefix.toString(), settings)) >= 0.9) {
                bestEndExclusive = i + 1;
                break;
            }
        }
        return Math.min(bestEndExclusive, verses.size());
    }

    private static double containmentOfLeftInRight(List<String> leftWords, List<String> rightWords) {
        if (leftWords.isEmpty()) {
            return 1.0;
        }
        if (rightWords.isEmpty()) {
            return 0.0;
        }
        Map<String, Integer> rightCounts = wordCounts(rightWords);
        int covered = 0;
        Map<String, Integer> leftCounts = wordCounts(leftWords);
        for (Map.Entry<String, Integer> entry : leftCounts.entrySet()) {
            int available = rightCounts.getOrDefault(entry.getKey(), 0);
            covered += Math.min(entry.getValue(), available);
        }
        return (double) covered / leftWords.size();
    }

    static int findBestMatchingVerseIndexFrom(String text, List<SongVerse> verses, int fromIndex,
                                              int excludeIndex, CompareSongsSettings settings) {
        int exact = findMatchingVerseIndexFrom(text, verses, fromIndex, excludeIndex, settings);
        if (exact >= 0) {
            return exact;
        }
        if (text == null || text.isEmpty() || verses == null || verses.isEmpty()) {
            return -1;
        }
        List<String> focusWords = words(text, settings);
        if (focusWords.isEmpty()) {
            return -1;
        }
        int start = Math.max(0, fromIndex);
        VerseMatchBest best = bestSingleVerseSimilarityFrom(focusWords, verses, start, excludeIndex, settings);
        best = betterMatch(best, bestConcatenatedSpanSimilarityFrom(focusWords, verses, start, excludeIndex, settings));
        if (best.index < 0 || best.score < WORD_SIMILARITY_THRESHOLD) {
            return -1;
        }
        return best.index;
    }

    private static int findMatchingVerseIndexFrom(String text, List<SongVerse> verses, int fromIndex,
                                                  int excludeIndex, CompareSongsSettings settings) {
        if (text == null || text.isEmpty() || verses == null) {
            return -1;
        }
        for (int i = Math.max(0, fromIndex); i < verses.size(); i++) {
            if (i == excludeIndex) {
                continue;
            }
            SongVerse verse = verses.get(i);
            if (verse == null) {
                continue;
            }
            if (textsEqual(text, verse.getText(), settings)) {
                return i;
            }
        }
        return -1;
    }

    private static VerseMatchBest bestSingleVerseSimilarityFrom(List<String> focusWords, List<SongVerse> verses,
                                                                int fromIndex, int excludeIndex,
                                                                CompareSongsSettings settings) {
        VerseMatchBest best = VerseMatchBest.none();
        for (int i = Math.max(0, fromIndex); i < verses.size(); i++) {
            if (i == excludeIndex) {
                continue;
            }
            SongVerse verse = verses.get(i);
            if (verse == null) {
                continue;
            }
            double score = wordSimilarity(focusWords, words(verse.getText(), settings));
            best = betterMatch(best, new VerseMatchBest(i, score));
        }
        return best;
    }

    private static VerseMatchBest bestConcatenatedSpanSimilarityFrom(List<String> focusWords, List<SongVerse> verses,
                                                                     int fromIndex, int excludeIndex,
                                                                     CompareSongsSettings settings) {
        VerseMatchBest best = VerseMatchBest.none();
        for (int start = Math.max(0, fromIndex); start < verses.size(); start++) {
            if (start == excludeIndex) {
                continue;
            }
            StringBuilder concatenated = new StringBuilder();
            for (int end = start; end < verses.size() && end - start + 1 <= MAX_CONCAT_VERSES; end++) {
                if (end == excludeIndex) {
                    break;
                }
                SongVerse verse = verses.get(end);
                if (verse == null) {
                    break;
                }
                if (!concatenated.isEmpty()) {
                    concatenated.append('\n');
                }
                concatenated.append(verse.getText());
                if (end == start) {
                    continue; // single verse handled by bestSingleVerseSimilarityFrom
                }
                double score = wordSimilarity(focusWords, words(concatenated.toString(), settings));
                best = betterMatch(best, new VerseMatchBest(start, score));
            }
        }
        return best;
    }

    private static VerseMatchBest betterMatch(VerseMatchBest current, VerseMatchBest candidate) {
        if (candidate == null || candidate.index < 0) {
            return current;
        }
        if (current == null || current.index < 0 || candidate.score > current.score) {
            return candidate;
        }
        return current;
    }

    private static final class VerseMatchBest {
        private final int index;
        private final double score;

        private VerseMatchBest(int index, double score) {
            this.index = index;
            this.score = score;
        }

        private static VerseMatchBest none() {
            return new VerseMatchBest(-1, -1.0);
        }
    }

    public static boolean textsEqual(String leftText, String rightText, CompareSongsSettings settings) {
        CompareNormalizeOptions options = settings.getEffectiveNormalizeOptions();
        String normalizedLeft = CompareNormalizeUtil.buildComparisonString(
                CompareNormalizeUtil.repeatChorusText(leftText, settings.isRepeatChorus()), options);
        String normalizedRight = CompareNormalizeUtil.buildComparisonString(
                CompareNormalizeUtil.repeatChorusText(rightText, settings.isRepeatChorus()), options);
        return normalizedLeft.equals(normalizedRight);
    }

    /**
     * True only when whole-song wording differs substantially.
     * Verse-boundary / whitespace-only differences do not count.
     */
    public static boolean versionsLookVeryDifferent(Song left, Song right, CompareSongsSettings settings) {
        if (left == null || right == null) {
            return false;
        }
        String leftAll = joinVerseTexts(left.getVerses());
        String rightAll = joinVerseTexts(right.getVerses());
        if (leftAll.isEmpty() && rightAll.isEmpty()) {
            return false;
        }
        double similarity = wordSimilarity(words(leftAll, settings), words(rightAll, settings));
        return similarity < WHOLE_SONG_SIMILARITY_THRESHOLD;
    }

    public static double wordSimilarity(String leftText, String rightText, CompareSongsSettings settings) {
        return wordSimilarity(words(leftText, settings), words(rightText, settings));
    }

    static double wordSimilarity(List<String> leftWords, List<String> rightWords) {
        if (leftWords.isEmpty() && rightWords.isEmpty()) {
            return 1.0;
        }
        if (leftWords.isEmpty() || rightWords.isEmpty()) {
            return 0.0;
        }
        Map<String, Integer> leftCounts = wordCounts(leftWords);
        Map<String, Integer> rightCounts = wordCounts(rightWords);
        int intersection = 0;
        for (Map.Entry<String, Integer> entry : leftCounts.entrySet()) {
            Integer rightCount = rightCounts.get(entry.getKey());
            if (rightCount != null) {
                intersection += Math.min(entry.getValue(), rightCount);
            }
        }
        int leftSize = leftWords.size();
        int rightSize = rightWords.size();
        int union = leftSize + rightSize - intersection;
        double jaccard = union == 0 ? 0.0 : (double) intersection / union;
        // Containment catches split-boundary cases where one side is a subset of the other.
        double containment = (double) intersection / Math.min(leftSize, rightSize);
        return Math.max(jaccard, containment);
    }

    static List<String> words(String text, CompareSongsSettings settings) {
        CompareNormalizeOptions options = settings.getEffectiveNormalizeOptions();
        String normalized = CompareNormalizeUtil.buildComparisonString(
                CompareNormalizeUtil.repeatChorusText(text, settings.isRepeatChorus()), options);
        if (normalized == null || normalized.isEmpty()) {
            return List.of();
        }
        String[] parts = normalized.split(" ");
        List<String> words = new ArrayList<>(parts.length);
        for (String part : parts) {
            if (!part.isEmpty()) {
                words.add(part);
            }
        }
        return words;
    }

    private static Map<String, Integer> wordCounts(List<String> words) {
        Map<String, Integer> counts = new HashMap<>();
        for (String word : words) {
            counts.merge(word, 1, Integer::sum);
        }
        return counts;
    }

    private static String joinVerseTexts(List<SongVerse> verses) {
        if (verses == null || verses.isEmpty()) {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        for (SongVerse verse : verses) {
            if (verse == null || verse.getText() == null || verse.getText().isEmpty()) {
                continue;
            }
            if (!builder.isEmpty()) {
                builder.append('\n');
            }
            builder.append(verse.getText());
        }
        return builder.toString();
    }
}
