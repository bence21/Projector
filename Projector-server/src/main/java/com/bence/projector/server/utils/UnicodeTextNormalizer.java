package com.bence.projector.server.utils;

import java.text.Normalizer;
import java.util.regex.Pattern;

/**
 * Utility class for Unicode text normalization.
 * Handles NFC normalization and removal of zero-width characters (U+200D, U+200C).
 * <p>
 * This ensures consistent text handling across the application:
 * - Text comparison treats variants equivalently
 * - Zero-width characters are stripped before processing
 * - All text is normalized to Unicode NFC (Canonical Composition) form
 */
public class UnicodeTextNormalizer {

    /**
     * Zero Width Joiner (U+200D)
     */
    private static final char ZERO_WIDTH_JOINER = '\u200D';

    /**
     * Zero Width Non-Joiner (U+200C)
     */
    private static final char ZERO_WIDTH_NON_JOINER = '\u200C';

    /**
     * Pattern to match zero-width characters
     */
    private static final Pattern ZERO_WIDTH_PATTERN = Pattern.compile("[\u200D\u200C]");

    /**
     * Normalizes text for display purposes.
     * Applies NFC normalization and removes zero-width characters.
     *
     * @param text The text to normalize, may be null
     * @return Normalized text, or null if input was null
     */
    public static String normalizeForDisplay(String text) {
        if (text == null) {
            return null;
        }
        return normalizeForComparison(text);
    }

    /**
     * Normalizes text for comparison purposes.
     * Applies NFC normalization and removes zero-width characters.
     * This ensures that variants like "Angyalkórus" and "Angyal‍‍kórus" are treated as equivalent.
     *
     * @param text The text to normalize, may be null
     * @return Normalized text, or null if input was null
     */
    public static String normalizeForComparison(String text) {
        if (text == null) {
            return null;
        }
        String normalized = normalizeToNFC(text);
        return stripZeroWidthCharacters(normalized);
    }

    /**
     * Normalizes text before persistence to database.
     * Applies NFC normalization and removes zero-width characters.
     *
     * @param text The text to normalize, may be null
     * @return Normalized text, or null if input was null
     */
    public static String normalizeForPersistence(String text) {
        if (text == null) {
            return null;
        }
        return normalizeForComparison(text);
    }

    /**
     * Strips zero-width characters (U+200D and U+200C) from text.
     *
     * @param text The text to process, may be null
     * @return Text with zero-width characters removed, or null if input was null
     */
    public static String stripZeroWidthCharacters(String text) {
        if (text == null) {
            return null;
        }
        return ZERO_WIDTH_PATTERN.matcher(text).replaceAll("");
    }

    /**
     * Normalizes text to Unicode NFC (Canonical Composition) form.
     * This ensures that characters like "é" are represented as a single code point (U+00E9)
     * rather than decomposed form (U+0065 + U+0301).
     *
     * @param text The text to normalize, may be null
     * @return Text in NFC form, or null if input was null
     */
    public static String normalizeToNFC(String text) {
        if (text == null) {
            return null;
        }
        return Normalizer.normalize(text, Normalizer.Form.NFC);
    }

    /**
     * Checks if text is already normalized to NFC form.
     *
     * @param text The text to check, may be null
     * @return true if text is in NFC form or null, false otherwise
     */
    public static boolean isNormalizedToNFC(String text) {
        if (text == null) {
            return true;
        }
        String normalized = normalizeToNFC(text);
        return text.equals(normalized);
    }

    /**
     * Checks if text contains zero-width characters.
     *
     * @param text The text to check, may be null
     * @return true if text contains zero-width characters, false otherwise
     */
    public static boolean containsZeroWidthCharacters(String text) {
        if (text == null) {
            return false;
        }
        return text.indexOf(ZERO_WIDTH_JOINER) >= 0 || text.indexOf(ZERO_WIDTH_NON_JOINER) >= 0;
    }
}
