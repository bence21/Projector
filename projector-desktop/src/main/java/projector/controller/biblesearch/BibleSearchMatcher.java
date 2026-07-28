package projector.controller.biblesearch;

import projector.model.BibleVerse;

import java.util.Locale;

import static projector.utils.StringUtils.stripAccents;

public final class BibleSearchMatcher {

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

    public static String verseTextForSearch(BibleVerse bibleVerse, boolean withAccents, boolean caseSensitive) {
        if (bibleVerse == null) {
            return null;
        }
        String text;
        if (withAccents) {
            text = bibleVerse.getText();
        } else if (caseSensitive) {
            String raw = bibleVerse.getText();
            text = raw != null ? projector.utils.StringUtils.stripAccentsPreservingStructure(raw) : null;
        } else {
            text = bibleVerse.getStrippedText();
            if (text == null) {
                String raw = bibleVerse.getText();
                if (raw != null) {
                    text = stripAccents(raw.toLowerCase(Locale.US));
                }
            }
        }
        if (text == null) {
            return null;
        }
        if (!caseSensitive) {
            text = text.toLowerCase(Locale.US);
        }
        if (!withAccents) {
            text = text.replaceAll("[^a-zA-Z]", "");
        }
        return text;
    }

    public static boolean matches(String haystack, String needle, boolean wholeWord) {
        if (haystack == null || needle == null || needle.isEmpty()) {
            return false;
        }
        if (!wholeWord) {
            return haystack.contains(needle);
        }
        int from = 0;
        while (from <= haystack.length() - needle.length()) {
            int index = haystack.indexOf(needle, from);
            if (index < 0) {
                return false;
            }
            if (isWholeWordAt(haystack, index, needle.length())) {
                return true;
            }
            from = index + 1;
        }
        return false;
    }

    private static boolean isWholeWordAt(String text, int index, int length) {
        boolean startOk = index == 0 || !Character.isLetter(text.charAt(index - 1));
        int end = index + length;
        boolean endOk = end >= text.length() || !Character.isLetter(text.charAt(end));
        return startOk && endOk;
    }
}
