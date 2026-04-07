package com.bence.songbook.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class YouTubeUrlParser {
    private static final Pattern VIDEO_ID_PATTERN = Pattern.compile(
            "(?:youtube\\.com/(?:watch\\?v=|embed/|shorts/)|youtu\\.be/)([a-zA-Z0-9_-]{11})(?:[?&].*)?$"
    );
    private static final Pattern BARE_ID_PATTERN = Pattern.compile("^([a-zA-Z0-9_-]{11})$");

    private YouTubeUrlParser() {
    }

    public static String parseYoutubeUrl(String url) {
        try {
            if (url == null || url.trim().isEmpty()) {
                return "";
            }

            String trimmed = url.trim();
            Matcher matcher = VIDEO_ID_PATTERN.matcher(trimmed);
            if (matcher.find()) {
                return matcher.group(1);
            }

            matcher = BARE_ID_PATTERN.matcher(trimmed);
            return matcher.matches() ? matcher.group(1) : "";
        } catch (Exception e) {
            return "";
        }
    }
}
