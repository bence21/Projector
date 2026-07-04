package projector.utils.compare;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public final class CompareNormalizeUtil {

    private static final Set<Character> PUNCTUATION_CHARS = Set.of(
            '.', ',', ';', ':', '!', '?', '…', '·', '•', '-', '–', '—', '\'', '"', '«', '»', '¿', '¡'
    );
    private static final Set<Character> SLASH_PIPE_BACKSLASH = Set.of('/', '|', '\\');
    private static final String CHORUS = "chorus";
    private static final String CHORUS_PREFIX = CHORUS + ":";
    private static final char LEFT_SINGLE_QUOTATION_MARK = '‘';
    private static final char RIGHT_SINGLE_QUOTATION_MARK = '’';
    private static final char ACUTE_ACCENT = '´';
    private static final char GRAVE_ACCENT = '`';
    private static final char LEFT_DOUBLE_QUOTATION_MARK = '“';
    private static final char RIGHT_DOUBLE_QUOTATION_MARK = '”';
    private static final char APOSTROPHE = '\'';
    private static final char QUOTATION_MARK = '"';
    private static final char COMBINING_DIACRITICAL_MARKS_MIN = '\u0300';
    private static final char COMBINING_DIACRITICAL_MARKS_MAX = '\u036f';

    private CompareNormalizeUtil() {
    }

    public record NormalizedTextIndex(String normalized, int[] originalIndices) {
        public int getOriginalIndex(int normalizedIndex) {
            return originalIndices[normalizedIndex];
        }

        public int length() {
            return normalized.length();
        }
    }

    public static NormalizedTextIndex buildNormalizedTextIndex(String text, CompareNormalizeOptions options) {
        if (text == null || text.isEmpty()) {
            return new NormalizedTextIndex("", new int[0]);
        }
        StringBuilder out = new StringBuilder(text.length());
        List<Integer> indexMap = new ArrayList<>(text.length());
        boolean lastWasSpace = false;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (options.isNormalizeQuotes()) {
                c = normalizeQuoteChar(c);
            }
            if (options.isIgnoreAccents()) {
                String stripped = stripAccentsChar(String.valueOf(c));
                if (stripped.isEmpty()) {
                    continue;
                }
                c = stripped.charAt(0);
            }
            if (options.isIgnorePunctuation() && PUNCTUATION_CHARS.contains(c)) {
                continue;
            }
            if (options.isIgnoreSlashPipeBackslash() && SLASH_PIPE_BACKSLASH.contains(c)) {
                continue;
            }
            if (options.isIgnoreNumbers() && Character.isDigit(c)) {
                continue;
            }
            if (options.isIgnoreAnnotations() && c == '[') {
                int end = text.indexOf(']', i);
                if (end > i) {
                    i = end;
                    continue;
                }
            }
            if (options.isNormalizeWhitespace() && Character.isWhitespace(c)) {
                if (!lastWasSpace && out.length() > 0) {
                    out.append(' ');
                    indexMap.add(i);
                    lastWasSpace = true;
                }
                continue;
            }
            lastWasSpace = false;
            if (options.isIgnoreCase()) {
                c = Character.toLowerCase(c);
            }
            out.append(c);
            indexMap.add(i);
        }
        if (options.isNormalizeWhitespace() && out.length() > 0 && out.charAt(out.length() - 1) == ' ') {
            out.setLength(out.length() - 1);
            if (!indexMap.isEmpty()) {
                indexMap.remove(indexMap.size() - 1);
            }
        }
        int[] indices = new int[indexMap.size()];
        for (int i = 0; i < indexMap.size(); i++) {
            indices[i] = indexMap.get(i);
        }
        return new NormalizedTextIndex(out.toString(), indices);
    }

    public static String buildComparisonString(String text, CompareNormalizeOptions options) {
        return buildNormalizedTextIndex(text, options).normalized();
    }

    public static boolean charactersEqual(char a, char b, CompareNormalizeOptions options) {
        if (options.isIgnoreCase()) {
            return Character.toLowerCase(a) == Character.toLowerCase(b);
        }
        return a == b;
    }

    private static String charsToString(List<Character> chars) {
        StringBuilder sb = new StringBuilder(chars.size());
        for (int k = chars.size() - 1; k >= 0; k--) {
            sb.append(chars.get(k));
        }
        return sb.toString();
    }

    private static char normalizeQuoteChar(char c) {
        return switch (c) {
            case LEFT_SINGLE_QUOTATION_MARK, RIGHT_SINGLE_QUOTATION_MARK, ACUTE_ACCENT, GRAVE_ACCENT -> APOSTROPHE;
            case LEFT_DOUBLE_QUOTATION_MARK, RIGHT_DOUBLE_QUOTATION_MARK -> QUOTATION_MARK;
            default -> c;
        };
    }

    private static String stripAccentsChar(String c) {
        String normalized = Normalizer.normalize(c, Normalizer.Form.NFD);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < normalized.length(); i++) {
            char ch = normalized.charAt(i);
            if (ch < COMBINING_DIACRITICAL_MARKS_MIN || ch > COMBINING_DIACRITICAL_MARKS_MAX) {
                sb.append(ch);
            }
        }
        return sb.toString();
    }

    public static String repeatChorusText(String text, boolean repeatChorus) {
        if (!repeatChorus || text == null || text.isEmpty()) {
            return text;
        }
        String[] lines = text.split("\n", -1);
        StringBuilder result = new StringBuilder();
        String lastChorusBlock = null;
        for (String line : lines) {
            if (result.length() > 0) {
                result.append('\n');
            }
            String trimmed = line.trim().toLowerCase(Locale.ROOT);
            if (trimmed.startsWith(CHORUS_PREFIX) || trimmed.equals(CHORUS)) {
                lastChorusBlock = line;
            }
            result.append(line);
        }
        if (lastChorusBlock != null) {
            result.append('\n').append(lastChorusBlock);
        }
        return result.toString();
    }
}
