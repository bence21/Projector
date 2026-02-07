package com.bence.projector.server.backend.model;

/**
 * Distinguishes how a word from another language is used in the song's language.
 * BORROWED: Word borrowed from another language but written in song's language style (e.g., English word written Hungarian-style).
 * FOREIGN: Word borrowed from another language, OK in source language but not in song language.
 */
public enum ForeignLanguageType {
    BORROWED,
    FOREIGN
}
