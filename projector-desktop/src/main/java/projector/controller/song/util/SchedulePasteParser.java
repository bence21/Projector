package projector.controller.song.util;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

import static projector.utils.StringUtils.stripAccents;

/**
 * Parses pasted worship service text into ordered {@link SchedulePasteEntry} items.
 */
public final class SchedulePasteParser {

    private static final Pattern CHORD_TOKEN = Pattern.compile(
            "^[A-G][#b]?(m|maj|min|dim|aug|sus|add)?[0-9]*(/[A-G][#b]?)?$",
            Pattern.CASE_INSENSITIVE);
    /**
     * Trailing chord(s) and optional punctuation at end of a title line.
     */
    private static final Pattern TRAILING_CHORD = Pattern.compile(
            "(?i)([.\\s]+)\\(?([A-G][#b]?(m|maj|min|dim|aug|sus|add)?[0-9]*(/[A-G][#b]?)?)\\)?\\s*$");
    private static final Pattern TRAILING_CHORD_PLAIN = Pattern.compile(
            "(?i)\\s+([A-G][#b]?(m|maj|min|dim|aug|sus|add)?[0-9]*(/[A-G][#b]?)?)\\s*$");

    private static final Set<String> SECTION_KEYWORDS_NORMALIZED = new LinkedHashSet<>();

    static {
        String[] raw = {
                "Imaóra", "Imaora",
                "Kenyértörés", "Kenyertores",
                "Szünet", "Szunet",
                "Szolgálat", "Szolgalat"
        };
        for (String s : raw) {
            SECTION_KEYWORDS_NORMALIZED.add(stripAccents(s).toLowerCase(Locale.ROOT));
        }
    }

    private SchedulePasteParser() {
    }

    public static List<SchedulePasteEntry> parse(String rawText) {
        if (rawText == null || rawText.isBlank()) {
            return List.of();
        }
        return parseLineMode(rawText);
    }

    private static List<SchedulePasteEntry> parseLineMode(String rawText) {
        List<SchedulePasteEntry> out = new ArrayList<>();
        for (String line : rawText.split("\\R")) {
            String t = line.trim();
            if (t.isEmpty()) {
                continue;
            }
            if (isSectionKeywordLine(t)) {
                out.add(SchedulePasteEntry.section(t));
                continue;
            }
            if (isChordOnlyLine(t)) {
                continue;
            }
            String candidate = normalizeSongCandidate(t);
            if (!candidate.isEmpty()) {
                out.add(SchedulePasteEntry.song(candidate));
            }
        }
        return out;
    }

    static boolean isSectionKeywordLine(String line) {
        if (line == null) {
            return false;
        }
        String n = stripAccents(line.trim()).toLowerCase(Locale.ROOT);
        return SECTION_KEYWORDS_NORMALIZED.contains(n);
    }

    static boolean isChordOnlyLine(String line) {
        if (line == null || line.isBlank()) {
            return false;
        }
        String t = line.trim();
        return CHORD_TOKEN.matcher(t).matches();
    }

    /**
     * Strip trailing chord annotations and extra spaces; keep readable title text.
     */
    public static String normalizeSongCandidate(String line) {
        if (line == null) {
            return "";
        }
        String s = line.trim().replaceAll("\\s+", " ");
        s = TRAILING_CHORD.matcher(s).replaceFirst("");
        s = TRAILING_CHORD_PLAIN.matcher(s).replaceFirst("");
        s = s.replaceAll("[.,;:\\s]+$", "").trim();
        return s;
    }
}
