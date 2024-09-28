package com.bence.projector.server.utils;

import com.bence.projector.server.backend.model.Song;
import com.bence.projector.server.backend.model.SongVerse;

import java.text.Normalizer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringUtils {

    private static final int N = 2000;
    private static final Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
    public static final String WHITE_SPACES = " \\t\\f\\r\\u00A0";
    private static final String whiteSpace = "[" + WHITE_SPACES + "]"; // \s matches also to \n
    private static final String nonLetters = "\\P{L}";
    private static final String nonLettersExceptPeriod = "[^\\p{L}.]";
    private static final String nonLetters_saved = "([" + nonLetters + "])";
    private static final String letters_saved = "(\\p{L})";
    private static final String someSymbols_saved = "([.?!,:()])";
    private static final String dot = "\\.";
    private static final String simpleQuotationMarks = "\"'";
    private static final String simpleQuotationMarks_saved = getSaved(simpleQuotationMarks);
    private static final String whiteSpaceThanNonLetters_saved = "([" + WHITE_SPACES + nonLetters + "])";
    private static final char quotationMark = '"';
    private static final String enDash = "–";
    private static final String emDash = "—";
    private static final char rightDoubleQuotationMark = '”';
    private static final String endQuotationMark = "" + rightDoubleQuotationMark;
    private static final String endQuotationMark_saved = getSaved(endQuotationMark);
    private static final char leftDoubleQuotationMark = '“';
    private static final char doubleLowQuotationMark = '„';
    private static final String openingMark = "" + leftDoubleQuotationMark + doubleLowQuotationMark;
    private static final String withoutEndQuotationMark = openingMark + simpleQuotationMarks;
    private static final String quotationMarks = withoutEndQuotationMark + endQuotationMark;
    private static final String quotationMarks_saved = "([" + quotationMarks + "])";
    private static final String symbols = quotationMarks + ".?!,:)";
    private static final String otherThenSomeSymbols_saved = "([^ \t\n" + symbols + "|])";
    private static int[][] t = null;

    public static String stripAccents(String s) {
        String nfdNormalizedString = Normalizer.normalize(s, Normalizer.Form.NFD);
        s = pattern.matcher(nfdNormalizedString).replaceAll("");
        s = replaceAllOtherThenLetterAndNumber(s);
        return s;
    }

    private static String replaceAllOtherThenLetterAndNumber(String s) {
        s = s.replaceAll("[^a-zA-Z0-9]", "");
        return s;
    }

    public static String replaceAllOtherThenLetterAndNumber2(String s) {
        s = s.replaceAll(nonLettersExceptPeriod, "");
        return s;
    }

    public static String normalizeAccents(String s) {
        s = s.toLowerCase();
        s = s.replaceAll("í", "i");
        s = s.replaceAll("ó", "o");
        s = s.replaceAll("ő", "ö");
        s = s.replaceAll("ú", "u");
        s = s.replaceAll("ű", "ü");
        return s;
    }

    public synchronized static int highestCommonStringInt(String a, String b) {
        int aLength = a.length();
        if (aLength <= 0) {
            return 0;
        }
        int bLength = b.length();
        if (bLength <= 0) {
            return 0;
        }
        int i;
        int j = 0;
        if (t == null) {
            t = new int[N][];
            for (i = 0; i < N; ++i) {
                t[i] = new int[N];
                t[i][0] = 0;
            }
            for (j = 1; j < N; ++j) {
                t[0][j] = 0;
            }
        }
        char c;
        if (aLength >= N - 1) {
            aLength = N - 2;
        }
        if (bLength >= N - 1) {
            bLength = N - 2;
        }
        for (i = 0; i < aLength; ++i) {
            c = a.charAt(i);
            for (j = 0; j < bLength; ++j) {
                if (c == b.charAt(j)) {
                    t[i + 1][j + 1] = t[i][j] + 1;
                } else //noinspection ManualMinMaxCalculation
                    if (t[i + 1][j] > t[i][j + 1]) {
                        t[i + 1][j + 1] = t[i + 1][j];
                    } else {
                        t[i + 1][j + 1] = t[i][j + 1];
                    }
            }
        }
        return t[i][j];
    }

    public static int longestCommonSubString(String a, String b) {
        char[] X = a.toCharArray();
        char[] Y = b.toCharArray();
        int m = a.length();
        int n = b.length();
        int[][] LCStuff = new int[m + 1][n + 1];
        int result = 0;
        for (int i = 0; i <= m; i++) {
            for (int j = 0; j <= n; j++) {
                if (i == 0 || j == 0)
                    LCStuff[i][j] = 0;
                else if (X[i - 1] == Y[j - 1]) {
                    LCStuff[i][j] = LCStuff[i - 1][j - 1] + 1;
                    result = Integer.max(result, LCStuff[i][j]);
                } else
                    LCStuff[i][j] = 0;
            }
        }
        return result;
    }

    public static void formatSongs(List<Song> songs) {
        for (Song song : songs) {
            formatSong(song);
        }
    }

    private static void formatSong(Song song) {
        song.setTitle(format(song.getTitle()));
        formatSongVerses(song.getVerses());
    }

    public static boolean formatSongAndCheckChange(Song song) {
        String title = song.getTitle();
        song.setTitle(format(title));
        boolean changed = formatSongVersesCheckChange(song.getVerses());
        return isChanged(changed, title, song.getTitle());
    }

    private static boolean formatSongVersesCheckChange(List<SongVerse> songVerses) {
        SongVerse lastVerse = null;
        boolean changed = false;
        for (SongVerse songVerse : songVerses) {
            String text = songVerse.getText();
            songVerse.setText(format(text));
            changed = isSongVerseChanged(changed, songVerse, text);
            lastVerse = songVerse;
        }
        if (lastVerse != null) {
            String text = lastVerse.getText();
            lastVerse.setText(text.replaceAll("\nEnd$", ""));
            changed = isSongVerseChanged(changed, lastVerse, text);
        }
        return changed;
    }

    public static boolean isSongVerseChanged(boolean changed, SongVerse songVerse, String text) {
        return isChanged(changed, text, songVerse.getText());
    }

    private static boolean isChanged(boolean changed, String oldText, String newText) {
        return changed || !oldText.equals(newText);
    }

    private static void formatSongVerses(List<SongVerse> songVerses) {
        SongVerse lastVerse = null;
        for (SongVerse songVerse : songVerses) {
            songVerse.setText(format(songVerse.getText()));
            lastVerse = songVerse;
        }
        if (lastVerse != null) {
            String text = lastVerse.getText();
            lastVerse.setText(text.replaceAll("\nEnd$", ""));
        }
    }

    public static String format(String s) {
        String newValue = s.trim();
        newValue = fixQuotationMarks(newValue);
        newValue = newValue.replaceAll("([ \\t])([.?!,:])", "$2");
        newValue = newValue.replaceAll("´", "'");
        newValue = newValue.replaceAll("([" + openingMark + "]) +", "$1");
        newValue = newValue.replaceAll(someSymbols_saved + otherThenSomeSymbols_saved, "$1 $2");
        newValue = newValue.replaceAll(someSymbols_saved + "([" + withoutEndQuotationMark + "].+)", "$1 $2");
        String s1 = "([" + withoutEndQuotationMark + "])" + whiteSpace + "*";
        newValue = newValue.replaceAll("^" + s1, "$1");
        newValue = newValue.replaceAll("\n" + s1, "\n$1");
        newValue = newValue.replaceAll(someSymbols_saved + " +([" + endQuotationMark + "])", "$1$2");
        newValue = newValue.replaceAll(letters_saved + "\\(", "$1 (");
        newValue = newValue.replaceAll("\\) +" + someSymbols_saved, ")$1");
        newValue = dividerReplace(newValue, "/");
        newValue = dividerReplace(newValue, "\\|");
        newValue = dividerReplaceLeft(newValue);
        newValue = newValue.replaceAll(" {2,}", " ");
        newValue = newValue.replaceAll("\t{2,}", "\t");
        newValue = newValue.replaceAll(dot + " " + dot + " " + dot, "…");
        newValue = newValue.replaceAll(dot + dot + dot, "…");
        newValue = newValue.replaceAll(dot + otherThenSomeSymbols_saved, ". $1");
        newValue = newValue.replaceAll(dot + " +([" + quotationMarks + "])" + whiteSpaceThanNonLetters_saved, ". $1$2");
        newValue = newValue.replaceAll(" \\)", ")");
        newValue = newValue.replaceAll("\\( ", "(");
        newValue = newValue.replaceAll(dot + " " + quotationMarks_saved + "(" + whiteSpace + "+)([\\n$" + nonLetters + "])", ".$1$2$3");
        newValue = newValue.replaceAll(whiteSpace + "*" + endQuotationMark_saved, "$1");
        newValue = newValue.replaceAll("!" + whiteSpace + "*" + simpleQuotationMarks_saved + whiteSpaceThanNonLetters_saved, "!$1$2");
        newValue = removeSpaceAtEndLineForQuotationMarks(newValue);
        newValue = replaceDashType(newValue, "-", enDash);
        newValue = replaceDashType(newValue, enDash, enDash);
        newValue = replaceDashType(newValue, emDash, emDash);
        newValue = newValue.replaceAll("\r *\n?", "\n");
        newValue = newValue.replaceAll("\n\n", "\n");
        newValue = newValue.replaceAll("\t \n", "\n");
        newValue = newValue.replaceAll(" \t", " ");
        newValue = newValue.replaceAll("\t ", " ");
        newValue = newValue.replaceAll(" \n", "\n");
        newValue = newValue.replaceAll("\n ", "\n");
        newValue = newValue.replaceAll("\t\n", "\n");
        newValue = newValue.replaceAll("Ş", "Ș");
        newValue = newValue.replaceAll("ş", "ș");
        newValue = newValue.replaceAll("Ţ", "Ț");
        newValue = newValue.replaceAll("ţ", "ț");
        newValue = newValue.replaceAll("ã", "ă");
        newValue = newValue.replaceAll("ā", "ă");
        newValue = newValue.replaceAll("à", "á");
        newValue = newValue.replaceAll("è", "é");
        newValue = newValue.replaceAll("È", "É");
        newValue = newValue.replaceAll("õ", "ő");
        newValue = newValue.replaceAll("ō", "ő");
        newValue = newValue.replaceAll("ô", "ő");
        newValue = newValue.replaceAll("Õ", "Ő");
        newValue = newValue.replaceAll("û", "ű");
        return newValue;
    }

    private static String removeSpaceAtEndLineForQuotationMarks(String newValue) {
        String s = whiteSpace + "*" + quotationMarks_saved + whiteSpace + "*";
        newValue = newValue.replaceAll(s + "\\n", "$1\n");
        newValue = newValue.replaceAll(s + "$", "$1");
        return newValue;
    }

    private static String replaceAsEnDashFromADash(String newValue, String dashType, String toDash) {
        String enDashReplacement = toDash + " ";
        newValue = newValue.replaceAll("^" + whiteSpace + "*" + dashType + " *", enDashReplacement);
        newValue = newValue.replaceAll("\n" + whiteSpace + "*" + dashType + " *", "\n" + enDashReplacement);
        String toDashSaved = getSaved(toDash);
        String s = toDashSaved + "(.*) *" + dashType;
        String s2 = "$1$2 " + toDash;
        newValue = replaceAsEndLineDash2(newValue, s + nonLetters_saved, s2 + "$3");
        newValue = replaceAsEndLineDash2(newValue, s, s2);
        newValue = newValue.replaceAll(" {2}", " ");
        return newValue;
    }

    private static String replaceAsEndLineDash2(String newValue, String s, String endLineReplacement) {
        newValue = newValue.replaceAll(s + "\n", endLineReplacement + "\n");
        newValue = newValue.replaceAll(s + "$", endLineReplacement);
        return newValue;
    }

    private static String replaceDashType(String newValue, String dashType, String toDash) {
        String s1 = "(?!" + whiteSpace + ")" + nonLetters_saved;
        String replacement = "$1 " + toDash + " $2";
        newValue = replaceDashTypeIfOneSpaceNear(newValue, dashType, s1, replacement);
        s1 = "(?!" + whiteSpace + ")" + letters_saved;
        newValue = replaceDashTypeIfOneSpaceNear(newValue, dashType, s1, replacement);
        newValue = replaceAsEnDashFromADash(newValue, dashType, toDash);
        return newValue;
    }

    private static String replaceDashTypeIfOneSpaceNear(String newValue, String dashType, String s1, String replacement) {
        newValue = newValue.replaceAll(s1 + " +" + dashType + " *" + letters_saved, replacement);
        newValue = newValue.replaceAll(s1 + " *" + dashType + " +" + letters_saved, replacement);
        return newValue;
    }

    private static String getSaved(String s) {
        return "([" + s + "])";
    }

    public static String fixQuotationMarks(String input) {
        char preferredClosingQuote = getPreferredClosingQuote(input);
        if (preferredClosingQuote == 0) {
            return input;
        }
        char preferredOpeningQuote = getPreferredOpeningQuote(input, preferredClosingQuote);
        StringBuilder builder = new StringBuilder(input);
        boolean foundClosingQuote = false;
        boolean inQuote = false;
        char openingQuote = 0;
        int openingQuoteIndex = -1;
        for (int i = 0; i < builder.length(); i++) {
            char currentChar = builder.charAt(i);
            if (inQuote) {
                if (isAQuote(currentChar)) {
                    inQuote = false;
                    if (!isOpeningQuote(openingQuote)) {
                        builder.setCharAt(openingQuoteIndex, preferredOpeningQuote);
                    }
                    if (!isClosingQuote(currentChar)) {
                        builder.setCharAt(i, preferredClosingQuote);
                    }
                    foundClosingQuote = !foundClosingQuote;
                }
            } else {
                if (isAQuote(currentChar)) {
                    inQuote = true;
                    openingQuote = currentChar;
                    openingQuoteIndex = i;
                }
            }
        }

        return builder.toString();
    }

    private static HashMap<Character, Integer> getOpeningMap(String s) {
        HashMap<Character, Integer> hashMap = new HashMap<>();
        StringBuilder builder = new StringBuilder(s);
        for (int i = 0; i < builder.length(); i++) {
            char currentChar = builder.charAt(i);
            if (isOpeningQuote(currentChar)) {
                incInMap(hashMap, currentChar);
            }
        }
        return hashMap;
    }

    private static HashMap<Character, Integer> getClosingMap(String s) {
        HashMap<Character, Integer> closingHashMap = new HashMap<>();
        StringBuilder builder = new StringBuilder(s);
        for (int i = 0; i < builder.length(); i++) {
            char currentChar = builder.charAt(i);
            if (isClosingQuote(currentChar)) {
                incInMap(closingHashMap, currentChar);
            }
        }
        return closingHashMap;
    }

    private static char getPreferredClosingQuote(String s) {
        HashMap<Character, Integer> closingHashMap = getClosingMap(s);
        char closingMaxChar = getMaxChar(closingHashMap);
        if (closingMaxChar != 0) {
            return closingMaxChar;
        }
        HashMap<Character, Integer> hashMap = getOpeningMap(s);
        char maxChar = getMaxChar(hashMap);
        if (maxChar == 0) {
            return 0;
        }
        if (maxChar == leftDoubleQuotationMark) {
            return rightDoubleQuotationMark;
        }
        if (maxChar == doubleLowQuotationMark) {
            return rightDoubleQuotationMark;
        }
        return maxChar;
    }

    private static char getPreferredOpeningQuote(String s, char preferredClosingQuote) {
        HashMap<Character, Integer> hashMap = getOpeningMap(s);
        char preferredOpeningQuote = getMaxChar(hashMap);
        if (preferredOpeningQuote != 0) {
            if (preferredOpeningQuote == quotationMark && preferredClosingQuote != quotationMark) {
                preferredOpeningQuote = getOpeningQuoteByClosing(preferredClosingQuote, preferredOpeningQuote);
            }
            return preferredOpeningQuote;
        }
        if (preferredClosingQuote != 0) {
            return getOpeningQuoteByClosing(preferredClosingQuote, quotationMark);
        }
        return 0;
    }

    private static char getOpeningQuoteByClosing(char closingQuote, char defaultQuote) {
        if (closingQuote == rightDoubleQuotationMark) {
            return leftDoubleQuotationMark;
        }
        return defaultQuote;
    }

    private static void incInMap(HashMap<Character, Integer> hashMap, char currentChar) {
        Integer count = hashMap.get(currentChar);
        if (count == null) {
            count = 1;
        } else {
            ++count;
        }
        hashMap.put(currentChar, count);
    }

    private static boolean isOpeningQuote(char c) {
        return isAQuote(c) && !isClosingQuote(c) && (c != quotationMark);
    }

    private static char getMaxChar(HashMap<Character, Integer> hashMap) {
        int maxCount = 0;
        char maxChar = 0;

        for (Map.Entry<Character, Integer> entry : hashMap.entrySet()) {
            int count = entry.getValue();
            if (count > maxCount) {
                maxCount = count;
                maxChar = entry.getKey();
            }
        }
        return maxChar;
    }

    private static boolean isClosingQuote(char c) {
        return c == rightDoubleQuotationMark;
    }

    private static boolean isAQuote(char c) {
        return c == rightDoubleQuotationMark ||
                c == leftDoubleQuotationMark ||
                c == doubleLowQuotationMark ||
                c == quotationMark;
    }

    private static String dividerReplace(String newValue, String divider) {
        newValue = newValue.replaceAll(" *:+ *" + divider + "+", " :" + divider);
        newValue = newValue.replaceAll(divider + "+ *:+", divider + ":");
        newValue = newValue.replaceAll(": +" + divider, " :" + divider);
        newValue = newValue.replaceAll(divider + " +:", divider + ": ");
        return newValue;
    }

    private static String dividerReplaceLeft(String newValue) {
        String divider = "\\\\";
        newValue = newValue.replaceAll(" *:+ *" + divider + "+", " :/");
        return newValue;
    }

    public static int countMatches(String s, String regex) {
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(s);
        int matchCount = 0;
        while (matcher.find()) {
            matchCount++;
        }
        return matchCount;
    }
}
