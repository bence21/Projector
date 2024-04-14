package com.bence.songbook.ui.utils;

import java.text.Normalizer;
import java.util.regex.Pattern;

public class StringUtils {

    private static final int N = 2000;
    private static final Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
    private static int[][] t = null;

    public static String stripAccents(String s) {
        String nfdNormalizedString = Normalizer.normalize(s, Normalizer.Form.NFD);
        s = pattern.matcher(nfdNormalizedString).replaceAll("");
        s = s.replaceAll("[^a-zA-Z0-9]", "");
        return s;
    }

    public synchronized static int highestCommonStringInt(String a, String b) {
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
                } else if (t[i + 1][j] > t[i][j + 1]) {
                    t[i + 1][j + 1] = t[i + 1][j];
                } else {
                    t[i + 1][j + 1] = t[i][j + 1];
                }
            }
        }
        return t[i][j];
    }

}
