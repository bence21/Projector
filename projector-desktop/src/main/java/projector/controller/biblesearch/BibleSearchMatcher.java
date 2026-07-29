package projector.controller.biblesearch;

import projector.model.BibleVerse;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class BibleSearchMatcher {

    public record MatchSpan(int start, int endExclusive) {
    }

    private BibleSearchMatcher() {
    }

    public static String normalizeQuery(String query, boolean withAccents, boolean caseSensitive) {
        if (query == null) {
            return "";
        }
        String normalized = query.trim();
        if (!withAccents) {
            normalized = projector.utils.StringUtils.stripAccentsPreservingStructure(normalized);
        }
        if (!caseSensitive) {
            normalized = normalized.toLowerCase(Locale.US);
        }
        if (!withAccents) {
            normalized = normalized.replaceAll("[^a-zA-Z]", "");
        }
        return normalized.replace("]", "").replace("[", "");
    }

    public static boolean matchesVerse(BibleVerse bibleVerse, String rawQuery, boolean withAccents,
                                       boolean caseSensitive, boolean wholeWord) {
        if (bibleVerse == null) {
            return false;
        }
        return !findMatchSpans(bibleVerse.getText(), rawQuery, withAccents, caseSensitive, wholeWord).isEmpty();
    }

    public static List<MatchSpan> findMatchSpans(String verse, String rawQuery, boolean withAccents,
                                                 boolean caseSensitive, boolean wholeWord) {
        if (verse == null || rawQuery == null) {
            return List.of();
        }
        String normalizedQuery = normalizeQuery(rawQuery, withAccents, caseSensitive);
        if (normalizedQuery.isEmpty()) {
            return List.of();
        }
        if (withAccents) {
            String haystack = caseSensitive ? verse : verse.toLowerCase(Locale.US);
            String needle = caseSensitive ? normalizedQuery : normalizedQuery.toLowerCase(Locale.US);
            return findSubstringSpans(verse, haystack, needle, wholeWord);
        }
        String preparedVerse = projector.utils.StringUtils.stripAccentsPreservingStructure(verse);
        if (!caseSensitive) {
            preparedVerse = preparedVerse.toLowerCase(Locale.US);
        }
        return findLetterSubsequenceSpans(verse, preparedVerse, normalizedQuery.toCharArray(), wholeWord);
    }

    private static List<MatchSpan> findSubstringSpans(String verse, String haystack, String needle, boolean wholeWord) {
        List<MatchSpan> spans = new ArrayList<>();
        int from = 0;
        while (from <= haystack.length() - needle.length()) {
            int index = haystack.indexOf(needle, from);
            if (index < 0) {
                break;
            }
            if (!wholeWord || isWholeWordSpan(verse, index, index + needle.length() - 1)) {
                spans.add(new MatchSpan(index, index + needle.length()));
            }
            from = index + 1;
        }
        return spans;
    }

    private static List<MatchSpan> findLetterSubsequenceSpans(String verse, String preparedVerse, char[] queryChars,
                                                              boolean wholeWord) {
        List<MatchSpan> spans = new ArrayList<>();
        int queryIndex = 0;
        int matchStart = -1;
        for (int i = 0; i < preparedVerse.length(); ++i) {
            char verseChar = preparedVerse.charAt(i);
            if (!Character.isLetter(verseChar)) {
                if (queryIndex != 0) {
                    i = matchStart;
                    queryIndex = 0;
                    matchStart = -1;
                }
                continue;
            }
            char queryChar = queryChars[queryIndex];
            if (verseChar == queryChar) {
                if (queryIndex == 0) {
                    matchStart = i;
                }
                ++queryIndex;
                if (queryIndex == queryChars.length) {
                    if (!wholeWord || isWholeWordSpan(verse, matchStart, i)) {
                        spans.add(new MatchSpan(matchStart, i + 1));
                        queryIndex = 0;
                        matchStart = -1;
                    } else {
                        i = matchStart;
                        queryIndex = 0;
                        matchStart = -1;
                    }
                }
            } else if (queryIndex != 0) {
                i = matchStart;
                queryIndex = 0;
                matchStart = -1;
            }
        }
        return spans;
    }

    private static boolean isWholeWordSpan(String text, int start, int endInclusive) {
        boolean startOk = start == 0 || !Character.isLetter(text.charAt(start - 1));
        boolean endOk = endInclusive + 1 >= text.length() || !Character.isLetter(text.charAt(endInclusive + 1));
        return startOk && endOk;
    }
}
