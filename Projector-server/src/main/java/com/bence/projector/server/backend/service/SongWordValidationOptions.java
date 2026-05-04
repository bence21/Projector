package com.bence.projector.server.backend.service;

public record SongWordValidationOptions(boolean includeWordSuggestions, boolean includeMixedLanguageAnalysis) {

    /**
     * Word suggestions on; mixed-language metrics off.
     */
    public static final SongWordValidationOptions DEFAULT = new SongWordValidationOptions(true, true);

    /**
     * Skips similarity-based suggestions (and the full reviewed-word list scan); still runs mixed-language
     * probing for unreviewed words. Still fills banned / rejected / unreviewed lists and {@code hasIssues}.
     */
    public static final SongWordValidationOptions FAST_ISSUE_SCAN = new SongWordValidationOptions(false, true);

}
