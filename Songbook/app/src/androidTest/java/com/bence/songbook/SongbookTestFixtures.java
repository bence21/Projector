package com.bence.songbook;

public final class SongbookTestFixtures {

    public static final String SEARCH_QUERY_SONG_A = "367";
    public static final String SEARCH_QUERY_SONG_A_TITLE = "Téged kereslek";
    public static final String SONG_A_ROW_TEXT =
            "Jézus, Téged kereslek\nBaptista Gyülekezeti Énekeskönyv 367";
    public static final String SONG_A_TITLE = "Jézus, Téged kereslek";
    public static final String SONG_A_ORDINAL_LABEL = "Baptista Gyülekezeti Énekeskönyv 367";
    public static final String SONG_A_COLLECTION_KEYWORD = "baptista";
    public static final String SONG_A_ORDINAL_NUMBER = "367";

    public static final String HUNGARIAN_LANGUAGE_NAME = "Hungarian";
    public static final String HUNGARIAN_NATIVE_NAME = "Magyar";

    public static final String SEARCH_QUERY_SONG_B = "baptista";
    public static final int MIN_SEARCH_RESULTS = 2;
    public static final int MIN_SEARCH_RESULTS_SONG_A = 5;
    /**
     * Minimum songs expected after a full Hungarian language download.
     */
    public static final int MIN_DOWNLOADED_HUNGARIAN_SONGS = 100;

    private SongbookTestFixtures() {
    }
}
