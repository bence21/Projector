package projector.utils.compare;

import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

import java.util.ArrayList;
import java.util.List;

public final class CompareDiffHighlighter {

    public static final String MATCH_STYLE_CLASS = "compare-match";
    public static final String DIFF_STYLE_CLASS = "compare-diff";

    private CompareDiffHighlighter() {
    }

    public static void render(TextFlow textFlow, String text, List<String> commonSubstrings) {
        textFlow.getChildren().clear();
        if (text == null || text.isEmpty()) {
            return;
        }
        String remaining = text;
        for (int i = commonSubstrings.size() - 1; i >= 0; i--) {
            String common = commonSubstrings.get(i);
            if (common.isEmpty()) {
                continue;
            }
            int endIndex = remaining.indexOf(common);
            if (endIndex < 0) {
                continue;
            }
            Text diffText = new Text(remaining.substring(0, endIndex));
            diffText.getStyleClass().add(DIFF_STYLE_CLASS);
            textFlow.getChildren().add(0, diffText);

            Text matchText = new Text(common);
            matchText.getStyleClass().add(MATCH_STYLE_CLASS);
            textFlow.getChildren().add(0, matchText);
            remaining = remaining.substring(endIndex + common.length());
        }
        if (!remaining.isEmpty()) {
            Text diffText = new Text(remaining);
            diffText.getStyleClass().add(DIFF_STYLE_CLASS);
            textFlow.getChildren().add(0, diffText);
        }
    }

    public static void renderAligned(TextFlow textFlow, String text, String other, CompareNormalizeOptions options) {
        textFlow.getChildren().clear();
        if (text == null || text.isEmpty()) {
            return;
        }
        if (other == null) {
            other = "";
        }
        CompareNormalizeUtil.NormalizedTextIndex left = CompareNormalizeUtil.buildNormalizedTextIndex(text, options);
        CompareNormalizeUtil.NormalizedTextIndex right = CompareNormalizeUtil.buildNormalizedTextIndex(other, options);
        boolean[] matchedOriginal = buildMatchedOriginalIndices(text, left, right, options);
        appendSegments(textFlow, text, matchedOriginal);
    }

    private static boolean[] buildMatchedOriginalIndices(String text,
                                                         CompareNormalizeUtil.NormalizedTextIndex left,
                                                         CompareNormalizeUtil.NormalizedTextIndex right,
                                                         CompareNormalizeOptions options) {
        boolean[] matchedOriginal = new boolean[text.length()];
        int n = left.length();
        int m = right.length();
        if (n == 0) {
            return matchedOriginal;
        }
        int[][] dp = new int[n + 1][m + 1];
        for (int i = 1; i <= n; i++) {
            for (int j = 1; j <= m; j++) {
                if (CompareNormalizeUtil.charactersEqual(left.normalized().charAt(i - 1),
                        right.normalized().charAt(j - 1), options)) {
                    dp[i][j] = dp[i - 1][j - 1] + 1;
                } else {
                    dp[i][j] = Math.max(dp[i - 1][j], dp[i][j - 1]);
                }
            }
        }
        int i = n;
        int j = m;
        while (i > 0 && j > 0) {
            if (CompareNormalizeUtil.charactersEqual(left.normalized().charAt(i - 1),
                    right.normalized().charAt(j - 1), options) && dp[i - 1][j - 1] + 1 == dp[i][j]) {
                matchedOriginal[left.getOriginalIndex(i - 1)] = true;
                i--;
                j--;
            } else if (dp[i - 1][j] >= dp[i][j - 1]) {
                i--;
            } else {
                j--;
            }
        }
        return matchedOriginal;
    }

    private static void appendSegments(TextFlow textFlow, String text, boolean[] matchedOriginal) {
        List<Text> segments = new ArrayList<>();
        if (text.isEmpty()) {
            return;
        }
        boolean currentMatch = matchedOriginal[0];
        int segmentStart = 0;
        for (int i = 1; i <= text.length(); i++) {
            boolean nextMatch = i < text.length() && matchedOriginal[i];
            if (i == text.length() || nextMatch != currentMatch) {
                Text segment = new Text(text.substring(segmentStart, i));
                segment.getStyleClass().add(currentMatch ? MATCH_STYLE_CLASS : DIFF_STYLE_CLASS);
                segments.add(segment);
                segmentStart = i;
                currentMatch = nextMatch;
            }
        }
        textFlow.getChildren().addAll(segments);
    }
}
