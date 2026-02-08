package projector.utils;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class StringUtils {

    private static final int N = 2000;
    private static final Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
    private static int[][] t = null;
    private static int i, j;

    public static String stripAccents(String s) {
        String nfdNormalizedString = Normalizer.normalize(s, Normalizer.Form.NFD);
        s = pattern.matcher(nfdNormalizedString).replaceAll("");
        s = s.replaceAll("[^a-zA-Z0-9]", "");
        return s;
    }

    /**
     * Strips accents from a string while preserving all other characters (structure).
     * This is useful for file names, paths, and other cases where structure should be maintained.
     *
     * @param s the string to strip accents from
     * @return the string with accents removed, or null if input is null
     */
    public static String stripAccentsPreservingStructure(String s) {
        if (s == null) {
            return null;
        }
        try {
            String normalized = Normalizer.normalize(s, Normalizer.Form.NFD);
            // Remove combining diacritical marks (accents) but keep everything else
            normalized = normalized.replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
            return normalized;
        } catch (Exception e) {
            // Return original string if normalization fails
            return s;
        }
    }

    public static synchronized int highestCommonSubStringInt(String a, String b) {
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
        int aLength = a.length();
        if (aLength > N - 1) {
            aLength = N - 1;
        }
        int bLength = b.length();
        if (bLength > N - 1) {
            bLength = N - 1;
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
        return t[a.length()][b.length()];
    }

    public synchronized static String highestCommonSubString(String a, String b) {
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
        int aLength = a.length();
        if (aLength > N - 1) {
            aLength = N - 1;
        }
        int bLength = b.length();
        if (bLength > N - 1) {
            bLength = N - 1;
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
        StringBuilder r = new StringBuilder();
        i = a.length();
        j = b.length();
        while (i != 0 && j != 0) {
            if (t[i][j] - 1 == t[i - 1][j - 1]) {
                r.append(a.charAt(i - 1));
                --i;
                --j;
            } else if (t[i][j - 1] > t[i - 1][j]) {
                --j;
            } else {
                --i;
            }
        }
        return r.toString();
    }

    public static synchronized List<String> highestCommonStrings(String a, String b) {
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
        int aLength = a.length();
        if (aLength > N - 1) {
            aLength = N - 1;
        }
        int bLength = b.length();
        if (bLength > N - 1) {
            bLength = N - 1;
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
        StringBuilder r = new StringBuilder();
        i = a.length();
        j = b.length();
        ArrayList<String> strings = new ArrayList<>();
        while (i != 0 && j != 0) {
            if (t[i - 1][j] + 1 == t[i][j] && t[i][j] == t[i][j - 1] + 1) {
                r.append(a.charAt(i - 1));
                --i;
                --j;
            } else {
                if (r.length() > 0) {
                    strings.add(r.reverse().toString());
                    r = new StringBuilder();
                }
                if (t[i][j - 1] > t[i - 1][j]) {
                    --j;
                } else {
                    --i;
                }
            }
        }
        if (r.length() > 0) {
            strings.add(r.reverse().toString());
        }
        return strings;
    }

    public static void main(String[] args) {
        String a = "Pământul era pustiu și gol, întuneric era peste fața adâncului și Duhul lui Dumnezeu se mișca pe deasupra apelor. ";
        String b = "Pământul era pustiu și gol; peste fața adâncului de ape era întuneric, și Duhul lui Dumnezeu Se mișca pe deasupra apelor.";
        System.out.println(highestCommonSubString(a, b));
        System.out.println(a.length());
        System.out.println(b.length());
    }

    public static List<String> copyStringList(List<String> strings) {
        if (strings == null) {
            return null;
        }
        ArrayList<String> copiedList = new ArrayList<>(strings.size());
        copiedList.addAll(strings);
        return copiedList;
    }
}
