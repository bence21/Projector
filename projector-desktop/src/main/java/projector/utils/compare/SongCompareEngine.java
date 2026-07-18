package projector.utils.compare;

import javafx.scene.text.TextFlow;
import projector.model.Song;
import projector.model.SongVerse;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class SongCompareEngine {

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
     * Finds the best matching verse by normalized text comparison.
     * Prefer an exact normalized match; returns -1 when none is found.
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

    public static boolean textsEqual(String leftText, String rightText, CompareSongsSettings settings) {
        CompareNormalizeOptions options = settings.getEffectiveNormalizeOptions();
        String normalizedLeft = CompareNormalizeUtil.buildComparisonString(
                CompareNormalizeUtil.repeatChorusText(leftText, settings.isRepeatChorus()), options);
        String normalizedRight = CompareNormalizeUtil.buildComparisonString(
                CompareNormalizeUtil.repeatChorusText(rightText, settings.isRepeatChorus()), options);
        return normalizedLeft.equals(normalizedRight);
    }

    /**
     * True when content differences cover at least half of the longer song's verse count.
     */
    public static boolean versionsLookVeryDifferent(Song left, Song right, CompareSongsSettings settings) {
        if (left == null || right == null) {
            return false;
        }
        List<VerseCompareEntry> entries = buildVerseEntries(left, right, settings);
        int contentDiffs = countContentDifferences(entries);
        int maxVerses = Math.max(left.getVerses().size(), right.getVerses().size());
        if (maxVerses <= 0) {
            return false;
        }
        return contentDiffs * 2 >= maxVerses;
    }
}
