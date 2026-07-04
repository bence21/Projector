package projector.utils.compare;

import javafx.scene.text.TextFlow;
import projector.application.Settings;
import projector.model.Song;
import projector.model.SongVerse;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.ResourceBundle;

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
        ResourceBundle resourceBundle = Settings.getInstance().getResourceBundle();
        for (int i = 0; i < maxLen; i++) {
            SongVerse leftVerse = i < leftVerses.size() ? leftVerses.get(i) : null;
            SongVerse rightVerse = i < rightVerses.size() ? rightVerses.get(i) : null;
            String leftText = leftVerse != null ? leftVerse.getText() : "";
            String rightText = rightVerse != null ? rightVerse.getText() : "";
            VerseDiffKind diffKind = classifyVersePair(leftText, rightText, leftVerses, rightVerses, i, settings);
            int matchedRight = findMatchingVerseIndex(leftText, rightVerses, i, settings);
            int matchedLeft = findMatchingVerseIndex(rightText, leftVerses, i, settings);
            entries.add(new VerseCompareEntry(
                    i,
                    leftText,
                    rightText,
                    buildVerseLabel(leftVerse, i, resourceBundle),
                    buildVerseLabel(rightVerse, i, resourceBundle),
                    diffKind,
                    matchedLeft,
                    matchedRight));
        }
        return entries;
    }

    public static int countDifferingVerses(List<VerseCompareEntry> entries) {
        int count = 0;
        for (VerseCompareEntry entry : entries) {
            if (entry.isDiffers()) {
                ++count;
            }
        }
        return count;
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

    private static int findMatchingVerseIndex(String text, List<SongVerse> verses, int excludeIndex,
                                              CompareSongsSettings settings) {
        if (text == null || text.isEmpty()) {
            return -1;
        }
        for (int i = 0; i < verses.size(); i++) {
            if (i == excludeIndex) {
                continue;
            }
            if (textsEqual(text, verses.get(i).getText(), settings)) {
                return i;
            }
        }
        return -1;
    }

    private static boolean textsEqual(String leftText, String rightText, CompareSongsSettings settings) {
        CompareNormalizeOptions options = settings.getEffectiveNormalizeOptions();
        String normalizedLeft = CompareNormalizeUtil.buildComparisonString(
                CompareNormalizeUtil.repeatChorusText(leftText, settings.isRepeatChorus()), options);
        String normalizedRight = CompareNormalizeUtil.buildComparisonString(
                CompareNormalizeUtil.repeatChorusText(rightText, settings.isRepeatChorus()), options);
        return normalizedLeft.equals(normalizedRight);
    }

    private static String buildVerseLabel(SongVerse verse, int index, ResourceBundle resourceBundle) {
        if (verse != null && verse.isChorus()) {
            return resourceBundle.getString("chorus");
        }
        return resourceBundle.getString("Verse") + " " + (index + 1);
    }
}
