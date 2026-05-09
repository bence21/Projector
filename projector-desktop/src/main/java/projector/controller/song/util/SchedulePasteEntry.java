package projector.controller.song.util;

/**
 * One row in a pasted service plan: either a section heading or a song title candidate.
 */
public final class SchedulePasteEntry {

    public enum Kind {
        SECTION,
        SONG
    }

    private final Kind kind;
    /**
     * Section label, or raw pasted line for a song (before library match).
     */
    private final String text;

    private SchedulePasteEntry(Kind kind, String text) {
        this.kind = kind;
        this.text = text;
    }

    public static SchedulePasteEntry section(String label) {
        return new SchedulePasteEntry(Kind.SECTION, label);
    }

    public static SchedulePasteEntry song(String candidateTitle) {
        return new SchedulePasteEntry(Kind.SONG, candidateTitle);
    }

    public Kind getKind() {
        return kind;
    }

    public String getText() {
        return text;
    }
}
