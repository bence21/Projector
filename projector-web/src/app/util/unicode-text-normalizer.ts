/**
 * Utility functions for Unicode text normalization.
 * Handles NFC normalization and removal of zero-width characters (U+200D, U+200C).
 * 
 * This ensures consistent text handling across the application:
 * - Text comparison treats variants equivalently
 * - Zero-width characters are stripped before processing
 * - All text is normalized to Unicode NFC (Canonical Composition) form
 */

/**
 * Zero Width Joiner (U+200D)
 */
const ZERO_WIDTH_JOINER = '\u200D';

/**
 * Zero Width Non-Joiner (U+200C)
 */
const ZERO_WIDTH_NON_JOINER = '\u200C';

/**
 * Pattern to match zero-width characters
 */
const ZERO_WIDTH_PATTERN = /[\u200D\u200C]/g;

/**
 * Normalizes text for display purposes.
 * Applies NFC normalization and removes zero-width characters.
 * 
 * @param text The text to normalize, may be null or undefined
 * @returns Normalized text, or null/undefined if input was null/undefined
 */
export function normalizeForDisplay(text: string | null | undefined): string | null | undefined {
  if (text == null) {
    return text;
  }
  return normalizeForComparison(text);
}

/**
 * Normalizes text for comparison purposes.
 * Applies NFC normalization and removes zero-width characters.
 * This ensures that variants like "Angyalkórus" and "Angyal‍‍kórus" are treated as equivalent.
 * 
 * @param text The text to normalize, may be null or undefined
 * @returns Normalized text, or null/undefined if input was null/undefined
 */
export function normalizeForComparison(text: string | null | undefined): string | null | undefined {
  if (text == null) {
    return text;
  }
  const normalized = normalizeToNFC(text);
  return stripZeroWidthCharacters(normalized);
}

/**
 * Normalizes text before persistence to database.
 * Applies NFC normalization and removes zero-width characters.
 * 
 * @param text The text to normalize, may be null or undefined
 * @returns Normalized text, or null/undefined if input was null/undefined
 */
export function normalizeForPersistence(text: string | null | undefined): string | null | undefined {
  if (text == null) {
    return text;
  }
  return normalizeForComparison(text);
}

/**
 * Strips zero-width characters (U+200D and U+200C) from text.
 * 
 * @param text The text to process, may be null or undefined
 * @returns Text with zero-width characters removed, or null/undefined if input was null/undefined
 */
export function stripZeroWidthCharacters(text: string | null | undefined): string | null | undefined {
  if (text == null) {
    return text;
  }
  return text.replace(ZERO_WIDTH_PATTERN, '');
}

/**
 * Normalizes text to Unicode NFC (Canonical Composition) form.
 * This ensures that characters like "é" are represented as a single code point (U+00E9)
 * rather than decomposed form (U+0065 + U+0301).
 * 
 * @param text The text to normalize, may be null or undefined
 * @returns Text in NFC form, or null/undefined if input was null/undefined
 */
export function normalizeToNFC(text: string | null | undefined): string | null | undefined {
  if (text == null) {
    return text;
  }
  return text.normalize('NFC');
}

/**
 * Checks if text is already normalized to NFC form.
 * 
 * @param text The text to check, may be null or undefined
 * @returns true if text is in NFC form or null/undefined, false otherwise
 */
export function isNormalizedToNFC(text: string | null | undefined): boolean {
  if (text == null) {
    return true;
  }
  const normalized = normalizeToNFC(text);
  return text === normalized;
}

/**
 * Checks if text contains zero-width characters.
 * 
 * @param text The text to check, may be null or undefined
 * @returns true if text contains zero-width characters, false otherwise
 */
export function containsZeroWidthCharacters(text: string | null | undefined): boolean {
  if (text == null) {
    return false;
  }
  return text.indexOf(ZERO_WIDTH_JOINER) >= 0 || text.indexOf(ZERO_WIDTH_NON_JOINER) >= 0;
}
