package com.bence.projector.server.utils.models;

/**
 * A word extracted from a song with context about its position in the text.
 */
public class SongWord {

    private final String word;
    private final boolean firstWordInLine;
    private final boolean firstWordInSentence;

    public SongWord(String word, boolean firstWordInLine, boolean firstWordInSentence) {
        this.word = word;
        this.firstWordInLine = firstWordInLine;
        this.firstWordInSentence = firstWordInSentence;
    }

    public String getWord() {
        return word;
    }

    public boolean isFirstWordInLine() {
        return firstWordInLine;
    }

    public boolean isFirstWordInSentence() {
        return firstWordInSentence;
    }
}
